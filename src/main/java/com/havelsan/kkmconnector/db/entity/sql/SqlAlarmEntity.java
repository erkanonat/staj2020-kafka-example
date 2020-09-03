package com.havelsan.kkmconnector.db.entity.sql;


import com.havelsan.kkmconnector.model.KkmKafkaAlarmDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;


@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Alarm")
public class SqlAlarmEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "tgkm_alarm_id", nullable = false)
    private String tgkmAlarmId = null;

    @Column(name = "kkm_alarm_id", nullable = false)
    private String kkmAlarmId = null;

    @Column(name = "alarm_type")
    private String alarmType = null;

    @Column(name = "alarm_name")
    private String alarmName = null;

    @Column(name = "alarm_severity")
    private String alarmSeverity = null;

    @Column(name = "success")
    private Integer success ;

    @Column(name="retry_count")
    private Integer retryCount;

    @Column(name = "alarm_status")
    private String alarmStatus = null;

}

