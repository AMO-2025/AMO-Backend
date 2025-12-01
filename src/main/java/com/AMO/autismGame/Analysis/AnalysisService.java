package com.AMO.autismGame.Analysis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sagemakerruntime.SageMakerRuntimeClient;
import software.amazon.awssdk.services.sagemakerruntime.model.InvokeEndpointRequest;
import software.amazon.awssdk.services.sagemakerruntime.model.InvokeEndpointResponse;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;
import software.amazon.awssdk.services.sts.model.Credentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;

import java.util.Base64;

@Service
public class AnalysisService {

    private final StsClient stsClient;
    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON 처리를 위한 객체

    // 📌 1. [중요] 현재 InService 상태인 엔드포인트 이름을 정확히 적어주세요!
    private static final String ENDPOINT_NAME = "amov6";

    private static final String SAGEMAKER_ROLE_ARN = "arn:aws:iam::187235500274:role/SageMaker-CrossAccount-Role-For-AMO";
    private static final Region SAGEMAKER_REGION = Region.AP_NORTHEAST_2;

    @Autowired
    public AnalysisService(StsClient stsClient) {
        this.stsClient = stsClient;
    }

    public String invokeSageMakerEndpoint(byte[] imageBytes, String contentType) {

        System.out.println("=== 2. SERVICE invokeSageMakerEndpoint CALLED ===");

        try {
            // -------------------------------------------------------
            // 1. STS 권한 획득 (기존 로직 유지)
            // -------------------------------------------------------
            AssumeRoleRequest assumeRoleRequest = AssumeRoleRequest.builder()
                    .roleArn(SAGEMAKER_ROLE_ARN)
                    .roleSessionName("amo-springboot-session")
                    .build();

            AssumeRoleResponse assumeRoleResponse = stsClient.assumeRole(assumeRoleRequest);
            Credentials tempCreds = assumeRoleResponse.credentials();

            SageMakerRuntimeClient temporarySageMakerClient = SageMakerRuntimeClient.builder()
                    .region(SAGEMAKER_REGION)
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsSessionCredentials.create(tempCreds.accessKeyId(), tempCreds.secretAccessKey(), tempCreds.sessionToken())
                    ))
                    .build();

            // -------------------------------------------------------
            // 2. [핵심] 이미지를 Base64 JSON으로 변환
            // -------------------------------------------------------

            // A. 이미지 바이트 -> Base64 문자열 변환
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // B. JSON 객체 생성: {"image_base64": "..."}
            ObjectNode jsonBody = objectMapper.createObjectNode();
            jsonBody.put("image_base64", base64Image);

            // C. JSON 객체 -> 문자열 변환
            String jsonString = objectMapper.writeValueAsString(jsonBody);

            // D. 문자열을 SageMaker 전송용 바이트로 변환
            SdkBytes body = SdkBytes.fromUtf8String(jsonString);

            System.out.println("=== Sending JSON Payload to SageMaker (Length: " + jsonString.length() + ") ===");

            // -------------------------------------------------------
            // 3. SageMaker 호출
            // -------------------------------------------------------
            InvokeEndpointRequest request = InvokeEndpointRequest.builder()
                    .endpointName(ENDPOINT_NAME)
                    .contentType("application/json") // 📌 무조건 application/json 고정!
                    .accept("application/json")
                    .body(body)
                    .build();

            System.out.println("=== 3. CALLING SAGEMAKER... ===");

            InvokeEndpointResponse response = temporarySageMakerClient.invokeEndpoint(request);

            System.out.println("=== 4. SAGEMAKER CALL SUCCEEDED ===");
            temporarySageMakerClient.close();

            return response.body().asUtf8String();

        } catch (Exception e) {
            e.printStackTrace();
            // 에러 발생 시 로그에 자세히 출력
            throw new RuntimeException("SageMaker Error: " + e.getMessage());
        }
    }
}