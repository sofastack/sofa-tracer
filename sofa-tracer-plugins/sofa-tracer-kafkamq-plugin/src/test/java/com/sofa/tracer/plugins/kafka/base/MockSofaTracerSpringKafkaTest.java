package com.sofa.tracer.plugins.kafka.base;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Objects;


/**
 * @author chenchen6  2020/9/13 22:22
 * @since 3.1.0-SNAPSHOT
 */
public class MockSofaTracerSpringKafkaTest {
//    @ClassRule
//    public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(2, true, 2, "spring");


    @Autowired
    private KafkaTemplate<Integer, String> kafkaTemplate;

    @Test
    public void test() {
        if(Objects.isNull(kafkaTemplate)) {
            return ;
        }
        kafkaTemplate.send("spring", "message");
    }

}
