package com.havelsan.kkmconnector.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class KkmKafkaAlarmDto {

    private String tgkmName;
    private Double tgkmLat;
    private Double tgkmLon;
    private String details;

    private String entityName;
    private String entityType;
    private String alarmId;
    private String alarmName;
    private String alarmType;
    private Integer alarmActionType;

    private String alarmJsonStr;

    private Integer success;

    private Integer retryCount;
}
