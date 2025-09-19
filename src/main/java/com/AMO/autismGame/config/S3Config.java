package com.AMO.autismGame.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {

    @Value("${SPRING_CLOUD_AWS_CREDENTIALS_ACCESS_KEY}")
    private String accessKey;

    @Value("${SPRING_CLOUD_AWS_CREDENTIALS_SECRET_KEY}")
    private String secretKey;

    @Value("${SPRING_CLOUD_AWS_REGION_STATIC}")
    private String region;

    @Value("${SPRING_CLOUD_AWS_S3_BUCKET}")
    private String bucketName;

    @Bean
    public AmazonS3Client amazonS3Client() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        return (AmazonS3Client) AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }

    public String getBucketName() {
        return bucketName;
    }
} 