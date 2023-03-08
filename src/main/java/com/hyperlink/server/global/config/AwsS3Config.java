package com.hyperlink.server.global.config;

import lombok.extern.slf4j.Slf4j;

@Slf4j
//@Configuration
public class AwsS3Config {

//  private String accessKey;
//
//  private String secretKey;
//
//  private String region;
//
//  public AwsS3Config(@Value("${cloud.aws.credentials.access-key}") String accessKey,
//      @Value("${cloud.aws.credentials.secret-key}") String secretKey,
//      @Value("${cloud.aws.region.static}") String region) {
//    this.accessKey = accessKey;
//    this.secretKey = secretKey;
//    this.region = region;
//  }
//
//  @Bean
//  public AmazonS3Client amazonS3Client() {
//    BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
//    return (AmazonS3Client) AmazonS3ClientBuilder.standard()
//        .withRegion(region)
//        .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
//        .build();
//  }
}
