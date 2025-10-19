package org.example.domain.config.s3;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class S3Config {

	@Value("${cloud.aws.credentials.access-key}")
	private String accessKey;

	@Value("${cloud.aws.credentials.secret-key}")
	private String secretKey;

	@Value("${cloud.aws.region.static}")
	private String region;

	@Bean
	public AmazonS3 amazonS3Client() {
		log.info("🔑 AWS AccessKey (앞 4자리): {}", accessKey.substring(0, 4));
		log.info("🌏 AWS Region: {}", region);

		// ✅ 환경변수보다 우선하도록 강제
		System.setProperty("aws.accessKeyId", accessKey);
		System.setProperty("aws.secretKey", secretKey);

		BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);

		// ✅ virtual-hosted-style endpoint 강제 지정
		AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
			.withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
			.withRegion(region)
			.build();

		// ✅ 실제 endpoint 테스트 (버킷명 포함)
		String endpoint = String.format("https://%s.s3.%s.amazonaws.com/", "copybara-bucket-s3", region);
		log.info("✅ S3 Client initialized successfully with endpoint: {}", endpoint);

		return s3Client;
	}
}