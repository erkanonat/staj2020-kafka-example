package com.havelsan.kkmconnector.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.havelsan.kkmconnector.model.KkmKafkaAlarmDto;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Getter
@Setter
@Slf4j
public class KafkaMessageService {

    @Autowired
    AlarmService alarmService;

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @Value("${KAFKA_MESSAGE_TOPIC:Alarm}")
    private String kafkaMessageTopic;

    public void publish(final String message) {

        log.info("Message published: {}" , message);
        kafkaTemplate.send(kafkaMessageTopic, message);

    }


    @KafkaListener(topics = "${kafka.message.topic}", groupId = "${kafka.group.id}")
    public void onMessage(String message) {

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonParser jp = new JsonParser();
            JsonElement je = jp.parse(message);
            String prettyJsonString = gson.toJson(je);
            log.info("Kafka Received message: " + prettyJsonString);
            log.info("Kafka Received message: " + prettyJsonString);

            KkmKafkaAlarmDto receivedDto = mapper.readValue(message, KkmKafkaAlarmDto.class);

            alarmService.handleKafkaAlarmDto(receivedDto);

        } catch (IOException e) {
            log.error("Error while parsing alarm object {}", e.getStackTrace());
        }

    }


}
