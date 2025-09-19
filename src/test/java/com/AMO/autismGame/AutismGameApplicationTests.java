package com.AMO.autismGame;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application.properties")
class AutismGameApplicationTests {

	@Test
	@Disabled("실제 환경변수가 필요한 통합 테스트는 로컬 환경에서만 실행")
	void contextLoads() {
	}

}
