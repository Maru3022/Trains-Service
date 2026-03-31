package com.example.trainsservice;

import com.example.trainsservice.config.TestKafkaConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, excludeAutoConfiguration = KafkaAutoConfiguration.class)
@ActiveProfiles("test")
@Import(TestKafkaConfig.class)
public class TrainsServiceApplicationTests {

    @Test
    void contextLoads() {
        assertThat(true).isTrue();
    }

}
