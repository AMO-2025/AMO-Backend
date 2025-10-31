package com.AMO.autismGame;

import software.amazon.awssdk.services.s3.S3Client;
import com.AMO.autismGame.config.S3Config;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@SpringBootTest
@ActiveProfiles("test")
class AutismGameApplicationTests {

	@TestConfiguration
	static class MockConfig {
		@Bean
		S3Client s3Client() {
			return Mockito.mock(S3Client.class);
		}

		@Bean
		S3Config s3Config() {
			S3Config cfg = Mockito.mock(S3Config.class);
			Mockito.when(cfg.getBucketName()).thenReturn("test-bucket"); // ★ GameService가 쓰는 메서드만 스텁
			return cfg;
		}

		@Bean
		DataSource dataSource() {
			DriverManagerDataSource dataSource = new DriverManagerDataSource();
			dataSource.setDriverClassName("org.h2.Driver");
			dataSource.setUrl("jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
			dataSource.setUsername("sa");
			dataSource.setPassword("");
			return dataSource;
		}
	}

	@Test
	void contextLoads() {}
}