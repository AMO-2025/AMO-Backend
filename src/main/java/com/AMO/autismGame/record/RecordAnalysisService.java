package com.AMO.autismGame.record;

import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.SdkHttpClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sagemakerruntime.SageMakerRuntimeClient;
import software.amazon.awssdk.services.sagemakerruntime.model.InvokeEndpointRequest;
import software.amazon.awssdk.services.sagemakerruntime.model.InvokeEndpointResponse;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;
import software.amazon.awssdk.services.sts.model.Credentials;

import java.time.Duration;

@Service
public class RecordAnalysisService {

    private final StsClient stsClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String ENDPOINT_NAME = "recordv6";
    private static final String SAGEMAKER_ROLE_ARN = "arn:aws:iam::187235500274:role/SageMaker-CrossAccount-Role-For-AMO";
    private static final Region SAGEMAKER_REGION = Region.AP_NORTHEAST_2;

    @Autowired
    public RecordAnalysisService(StsClient stsClient) {
        this.stsClient = stsClient;
    }

    public String invokeSageMakerEndpoint(String audioBase64, String scenario) {
        System.out.println("INFO: RecordAnalysisService invoked.");
        try {
            // 1. Assume Role to get temporary credentials
            System.out.println("INFO: Assuming IAM role...");
            AssumeRoleRequest assumeRoleRequest = AssumeRoleRequest.builder()
                    .roleArn(SAGEMAKER_ROLE_ARN)
                    .roleSessionName("amo-springboot-record-session")
                    .build();

            AssumeRoleResponse assumeRoleResponse = stsClient.assumeRole(assumeRoleRequest);
            Credentials tempCreds = assumeRoleResponse.credentials();
            System.out.println("INFO: Successfully assumed IAM role.");
// 2. 📌 [핵심 수정] HTTP 소켓 타임아웃 설정 (여기부터)
            // 실제 통신을 담당하는 Apache Client의 귀를 5분 동안 열어둡니다.
            SdkHttpClient httpClient = ApacheHttpClient.builder()
                    .socketTimeout(Duration.ofMinutes(5))
                    .connectionTimeout(Duration.ofSeconds(10))
                    .build();
            // Set a 30-second timeout for the API call
            ClientOverrideConfiguration overrideConfig = ClientOverrideConfiguration.builder()
                    .apiCallTimeout(Duration.ofMinutes(5)) // 5분으로 넉넉하게!
                    .apiCallAttemptTimeout(Duration.ofMinutes(5)) // 시도 제한시간도 늘려주세요
                    .build();

            SageMakerRuntimeClient temporarySageMakerClient = SageMakerRuntimeClient.builder()
                    .region(SAGEMAKER_REGION)
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsSessionCredentials.create(tempCreds.accessKeyId(), tempCreds.secretAccessKey(), tempCreds.sessionToken())
                    ))
                    .httpClient(httpClient)
                    .overrideConfiguration(overrideConfig)
                    .build();

            // 2. Create JSON payload
            ObjectNode jsonBody = objectMapper.createObjectNode();
            jsonBody.put("audio_base64", audioBase64);
            jsonBody.put("scenario", scenario);

            String jsonString = objectMapper.writeValueAsString(jsonBody);
            SdkBytes body = SdkBytes.fromUtf8String(jsonString);

            // 3. Invoke SageMaker Endpoint
            InvokeEndpointRequest request = InvokeEndpointRequest.builder()
                    .endpointName(ENDPOINT_NAME)
                    .contentType("application/json")
                    .accept("application/json")
                    .body(body)
                    .build();

            System.out.println("INFO: Invoking SageMaker endpoint '" + ENDPOINT_NAME + "'...");
            InvokeEndpointResponse response = temporarySageMakerClient.invokeEndpoint(request);
            temporarySageMakerClient.close();
            System.out.println("INFO: SageMaker endpoint invoked successfully.");

            return response.body().asUtf8String();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("SageMaker Error for Record Analysis: " + e.getMessage());
        }
    }
}