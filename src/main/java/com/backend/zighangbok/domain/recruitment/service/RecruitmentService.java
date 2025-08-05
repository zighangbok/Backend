package com.backend.zighangbok.domain.recruitment.service;

import com.backend.zighangbok.domain.recruitment.dto.RecruitmentListDto;
import com.backend.zighangbok.domain.recruitment.dto.RecruitmentSimpleDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;


@Slf4j
@Service
@RequiredArgsConstructor
public class RecruitmentService {

    private final RestHighLevelClient client;
    private final DynamoDbClient dynamoDbClient;
    private final LambdaClient lambdaClient; // LambdaClient 주입
    private final S3Client s3Client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<RecruitmentSimpleDto> getRecommendedRecruitments(String userId) {
        log.info("추천 채용 정보 조회 요청 - userId: {}", userId);

        // 1. DynamoDB에서 추천 목록 조회
        List<String> recommendationUuids = getRecommendationsFromDynamoDB(userId);
        if (recommendationUuids.isEmpty()) {
            log.info("사용자에게 해당하는 추천 채용 정보가 없습니다. userId: {}", userId);
            return Collections.emptyList();
        }

        // 2. OpenSearch에서 추천된 공고 상세 정보 조회
        return getRecruitmentsFromOpenSearch(recommendationUuids);
    }

    private List<String> getRecommendationsFromDynamoDB(String userId) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        log.info("DynamoDB 추천 목록 조회를 시작합니다. userId: {}, 오늘 날짜: {}", userId, today);

        GetItemRequest request = GetItemRequest.builder()
                .tableName("user_recommendations")
                .key(Collections.singletonMap("id", AttributeValue.builder().s(userId).build()))
                .build();

        try {
            GetItemResponse response = dynamoDbClient.getItem(request);
            if (response.hasItem()) {
                Map<String, AttributeValue> item = response.item();
                log.info("DynamoDB에서 userId '{}'에 대한 항목을 찾았습니다.", userId);

                String createdAt = item.get("created_at").s();
                log.info("저장된 추천 날짜: {}. 오늘 날짜와 비교합니다.", createdAt);

//                if (!today.equals(createdAt)) {
//                    log.warn("날짜 불일치: DynamoDB의 추천 데이터가 최신이 아닙니다. (저장된 날짜: {}, 오늘 날짜: {}). 빈 목록을 반환합니다.", createdAt, today);
//                    return Collections.emptyList();
//                }

                AttributeValue recommendationsAttr = item.get("recommendations");
                if (recommendationsAttr != null && recommendationsAttr.hasL()) {
                    List<String> recommendationUuids = recommendationsAttr.l().stream()
                            .map(AttributeValue::s)
                            .collect(Collectors.toList());
                    log.info("DynamoDB에서 {}개의 추천 UUID를 성공적으로 조회했습니다. (샘플 5개: {})", recommendationUuids.size(), recommendationUuids.subList(0, Math.min(5, recommendationUuids.size())));
                    return recommendationUuids;
                } else {
                    log.warn("항목은 찾았으나, 'recommendations' 속성이 없거나 리스트 형식이 아닙니다.");
                }
            } else {
                log.warn("DynamoDB에서 userId '{}'에 대한 항목을 찾을 수 없습니다.", userId);
            }
        } catch (Exception e) {
            log.error("DynamoDB 조회 실패 - userId: {}", userId, e);
            throw new RuntimeException("추천 정보 조회 중 오류가 발생했습니다.", e);
        }
        log.warn("DynamoDB 조회 과정에서 최종적으로 빈 목록을 반환합니다. userId: {}", userId);
        return Collections.emptyList();
    }

    public ResponseEntity<Void> rerankRecommendations(String userId, List<String> samples) {
        List<String> recommendationUuids = getRecommendationsFromDynamoDB(userId);

        log.warn("recommendationUuids: {}", recommendationUuids); // 리랭킹 전

        if (recommendationUuids.isEmpty()) {
            invokeLambdaFunction(userId, samples);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }

        if (recommendationUuids.size() > 5) {
            List<String> topFive = new ArrayList<>(recommendationUuids.subList(0, 5));
            List<String> remaining = new ArrayList<>(recommendationUuids.subList(5, recommendationUuids.size()));
            remaining.addAll(topFive);
            updateRecommendationsInDynamoDB(userId, remaining);
            log.warn("re-ranked recommendationUuids: {}", remaining); // 리랭킹 후
        }

        return ResponseEntity.ok().build();
    }

    private void invokeLambdaFunction(String userId, List<String> samples) {
        String functionName = "newUserRanking";
        String payload;
        try {
            // userId와 samples를 포함하는 JSON 객체 생성
            payload = objectMapper.writeValueAsString(Map.of(
                    "user_id", userId,
                    "samples", samples
            ));
        } catch (Exception e) {
            log.error("Failed to create JSON payload", e);
            return;
        }

        InvokeRequest request = InvokeRequest.builder()
                .functionName(functionName)
                .payload(SdkBytes.fromUtf8String(payload))
                .build();

        try {
            InvokeResponse response = lambdaClient.invoke(request);
            log.info("Lambda function invoked successfully, response: {}", response.payload().asUtf8String());
        } catch (Exception e) {
            log.error("Failed to invoke Lambda function", e);
        }
    }


    private void updateRecommendationsInDynamoDB(String userId, List<String> newRecommendations) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        List<AttributeValue> recommendationAttributeValues = newRecommendations.stream()
                .map(uuid -> AttributeValue.builder().s(uuid).build())
                .collect(Collectors.toList());

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName("user_recommendations")
                .key(Collections.singletonMap("id", AttributeValue.builder().s(userId).build()))
                .updateExpression("SET recommendations = :r, created_at = :c")
                .expressionAttributeValues(Map.of(
                        ":r", AttributeValue.builder().l(recommendationAttributeValues).build(),
                        ":c", AttributeValue.builder().s(today).build()
                ))
                .build();

        try {
            dynamoDbClient.updateItem(request);
            log.info("Successfully updated recommendations for userId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to update recommendations in DynamoDB for userId: {}", userId, e);
            throw new RuntimeException("추천 순서 변경 중 오류가 발생했습니다.", e);
        }
    }


    private List<RecruitmentSimpleDto> getRecruitmentsFromOpenSearch(List<String> uuids) {
        if (uuids == null || uuids.isEmpty()) {
            log.warn("OpenSearch 조회 단계로 넘어온 UUID 목록이 비어있어 조회를 생략합니다.");
            return Collections.emptyList();
        }
        log.info("OpenSearch에서 {}개의 채용 공고 조회를 시작합니다. (샘플 5개: {})", uuids.size(), uuids.subList(0, Math.min(5, uuids.size())));

        SearchRequest searchRequest = new SearchRequest("recruitment_parsed");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        sourceBuilder.query(QueryBuilders.termsQuery("uuid.keyword", uuids));
        sourceBuilder.size(uuids.size()); // 모든 문서를 가져오기 위해 크기를 uuid 리스트 크기로 설정
        searchRequest.source(sourceBuilder);
        log.info("OpenSearch 쿼리 생성 완료:\n{}", searchRequest.source().toString());

        try {
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            log.info("OpenSearch 응답 수신. 총 {}개의 문서를 찾았습니다.", response.getHits().getTotalHits().value);

            // OpenSearch 결과를 UUID를 키로 하는 Map으로 변환 (순서 보장 없음)
            Map<String, RecruitmentSimpleDto> resultsMap = Arrays.stream(response.getHits().getHits())
                    .map(hit -> {
                        Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                        String uuid = (String) sourceAsMap.get("uuid");
                        String title = (String) sourceAsMap.get("title");
                        String companyJson = (String) sourceAsMap.get("company");
                        String companyName = parseCompanyName(companyJson);
                        return new RecruitmentSimpleDto(uuid, title, companyName);
                    })
                    .collect(Collectors.toMap(RecruitmentSimpleDto::getUuid, dto -> dto));

            // DynamoDB의 UUID 순서대로 결과를 재정렬
            List<RecruitmentSimpleDto> orderedResults = uuids.stream()
                    .map(resultsMap::get)
                    .filter(Objects::nonNull) // OpenSearch에 해당 UUID가 없는 경우 제외
                    .collect(Collectors.toList());

            if (uuids.size() != orderedResults.size()) {
                log.warn("OpenSearch 조회 결과와 DynamoDB UUID 목록 개수가 다릅니다. (DynamoDB: {}개, OpenSearch: {}개)", uuids.size(), orderedResults.size());
            }
            log.info("총 조회 및 순서 재정렬된 추천 공고 수: {}", orderedResults.size());
            return orderedResults;

        } catch (IOException e) {
            log.error("OpenSearch 조회 실패 - uuids: {}", uuids, e);
            throw new RuntimeException("채용정보 조회 중 오류가 발생했습니다.", e);
        }
    }

    public List<String> getUuidsFromS3() throws IOException {
        //String bucketName = "zighangbok-vector";
        //LocalDate yesterday = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1);
        //String key = "item_vectors_" + yesterday.format(DateTimeFormatter.ISO_LOCAL_DATE) + ".json";

        String bucketName = "zighangbok-vector";
        LocalDate fixedDate = LocalDate.of(2025, 7, 26);
        String key = "item_vectors_" + fixedDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + ".json";

        log.info("S3에서 파일을 읽으려고 시도합니다. Bucket: {}, Key: {}", bucketName, key);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest)) {
            Map<String, Object> vectorMap = objectMapper.readValue(s3Object, new TypeReference<Map<String, Object>>() {
            });
            log.info("S3 파일 읽기 성공. Bucket: {}, Key: {}", bucketName, key);
            List<String> uuids = new ArrayList<>(vectorMap.keySet());
            log.info("S3 JSON 파일에서 {}개의 key(UUID)를 파싱했습니다.", uuids.size());
            return uuids;
        } catch (NoSuchKeyException e) {
            log.error("S3 버킷 '{}'에 파일 '{}'이(가) 존재하지 않습니다. 파일을 확인해주세요.", bucketName, key);
            throw new IOException("S3 파일을 찾을 수 없습니다.", e);
        } catch (S3Exception e) {
            log.error("S3 접근 중 오류가 발생했습니다. ECS Task Role의 IAM 권한을 확인하세요. Bucket: {}, Key: {}", bucketName, key, e);
            throw new IOException("S3 서비스 오류가 발생했습니다.", e);
        } catch (JsonProcessingException e) {
            log.error("S3 파일 '{}'의 JSON 파싱에 실패했습니다. 파일 형식이 올바른지 확인해주세요.", key, e);
            throw new IOException("JSON 데이터 처리 중 오류가 발생했습니다.", e);
        } catch (Exception e) {
            log.error("S3 파일 처리 중 예상치 못한 오류가 발생했습니다. Bucket: {}, Key: {}", bucketName, key, e);
            throw new IOException("S3 파일 처리 중 일반 오류가 발생했습니다.", e);
        }
    }


    public List<RecruitmentListDto> getRecruitmentList(int page, int size) {
        log.info("채용정보 조회 시작 - page: {}, size: {}", page, size);

        try {
            log.info("S3에서 UUID 목록 조회를 시작합니다.");
            List<String> uuids = getUuidsFromS3();
            log.info("S3에서 {}개의 UUID를 조회했습니다.", uuids.size());

            if (uuids.isEmpty()) {
                log.warn("S3에서 조회된 UUID가 없어 빈 목록을 반환합니다.");
                return new ArrayList<>();
            }
            if (!uuids.isEmpty()) {
                log.info("S3 UUID 목록 (샘플 5개): {}", uuids.subList(0, Math.min(5, uuids.size())));
            }

            SearchRequest searchRequest = new SearchRequest("recruitment_parsed");
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            int from = page * size;

            // Bool 쿼리 생성
            var boolQuery = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termsQuery("uuid.keyword", uuids)) // .keyword 추가
                    .must(QueryBuilders.termQuery("depthFilter", 1)); // depthFilter 조건 다시 추가

            sourceBuilder.query(boolQuery);
            sourceBuilder.from(from);
            sourceBuilder.size(size);

            searchRequest.source(sourceBuilder);
            log.info("OpenSearch 쿼리 생성 완료:\n{}", searchRequest.source().toString());

            log.info("OpenSearch로 검색을 요청합니다.");
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            log.info("OpenSearch 응답 수신. 총 {}개의 문서를 찾았습니다.", response.getHits().getTotalHits().value);

            List<RecruitmentListDto> result = new ArrayList<>();
            List<String> responseUuids = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                log.debug("결과 파싱 중: {}", sourceAsMap);

                String uuid = (String) sourceAsMap.get("uuid");
                responseUuids.add(uuid);
                String title = (String) sourceAsMap.get("title");
                String companyJson = (String) sourceAsMap.get("company");

                String companyName = parseCompanyName(companyJson);

                RecruitmentListDto dto = RecruitmentListDto.builder()
                        .uuid(uuid)
                        .title(title)
                        .company(companyJson)
                        .companyName(companyName)
                        .build();

                result.add(dto);

                log.debug("조회된 데이터 - uuid: {}, title: {}, companyName: {}", uuid, title, companyName);
            }

            // [Debug] S3 목록에 없는 UUID가 반환되었는지 검증
            List<String> finalUuids = uuids;
            List<String> unexpectedUuids = responseUuids.stream()
                    .filter(uuid -> !finalUuids.contains(uuid))
                    .collect(Collectors.toList());

            if (!unexpectedUuids.isEmpty()) {
                String errorMessage = String.format(
                        "!!!!!!!!!! 데이터 불일치 오류: OpenSearch가 S3에 없는 UUID를 반환했습니다! 비정상 UUID: %s !!!!!!!!!!",
                        unexpectedUuids
                );
                log.error(errorMessage);
                throw new IllegalStateException(errorMessage); // 요청을 중단시키고 에러를 발생시킴
            }

            log.info("최종적으로 {}개의 채용정보를 반환합니다.", result.size());
            return result;

        } catch (IOException e) {
            log.error("채용정보 조회 중 심각한 오류 발생", e);
            throw new RuntimeException("채용정보 조회 중 오류가 발생했습니다.", e);
        }
    }

    private String parseCompanyName(String companyJson) {
        if (companyJson == null || companyJson.isEmpty()) {
            return "회사명 없음";
        }
        try {
            // 작은따옴표를 큰따옴표로 변환
            String fixedJson = companyJson.replace("'", "\"").replace("None", "null");
            Map<String, Object> companyMap = objectMapper.readValue(fixedJson, new TypeReference<>() {
            });

            if (companyMap.get("companyName") != null) {
                return companyMap.get("companyName").toString();
            }
        } catch (Exception e) {
            //log.warn("회사 정보 파싱 실패: {}", companyJson);
        }
        return "회사명 없음";
    }

    public List<RecruitmentSimpleDto> getSimpleRecruitmentList(int page, int size) {
        List<RecruitmentListDto> fullList = getRecruitmentList(page, size);

        return fullList.stream()
                .map(r -> new RecruitmentSimpleDto(r.getUuid(), r.getTitle(), r.getCompanyName()))
                .collect(Collectors.toList());
    }
}