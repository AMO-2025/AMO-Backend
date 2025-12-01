package com.AMO.autismGame.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
// 📌 SageMakerRuntimeClient는 AnalysisService에서 직접 생성하므로 import 제거
import software.amazon.awssdk.services.sts.StsClient; // 📌 STS Import

 @Configuration
public class AwsConfig {

    // 📌 [추가] STS Client Bean (A계정의 기본 자격증명 사용)
    @Bean
    public StsClient stsClient() {
        return StsClient.builder()
                .region(Region.AP_NORTHEAST_2) // STS는 글로벌 서비스지만 리전 지정 권장
                .build();
    }

    // 📌 [제거 또는 주석처리]
    // @Bean
    // public SageMakerRuntimeClient sageMakerRuntimeClient() {
    //    // 이 Bean은 A계정(9111...)으로만 동작하므로
    //    // B계정(1872...)의 엔드포인트를 호출할 수 없습니다.
    //    return SageMakerRuntimeClient.builder()
    //            .region(Region.AP_NORTHEAST_2) 
    //            .build();
    // }
}
