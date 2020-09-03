package com.havelsan.kkmconnector.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.havelsan.kkmconnector.db.entity.sql.SqlAlarmEntity;
import com.havelsan.kkmconnector.db.repository.SqlAlarmRepository;
import com.havelsan.kkmconnector.model.KkmKafkaAlarmDto;
import com.havelsan.kkmconnector.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.thingsboard.server.common.data.alarm.Alarm;
import org.thingsboard.server.common.data.alarm.AlarmStatus;
import org.thingsboard.server.common.data.asset.Asset;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

@Service
@Slf4j
public class AlarmService {

    // asset map
    Map<String , Asset> assetMap = new HashMap<>();

    @Autowired
    EgysRestService egysRestService;

    @Qualifier("sqlAlarmRepository")
    @Autowired
    SqlAlarmRepository sqlAlarmRepository;

    @Value("${kkm.alarm.retry.maxcount}")
    private Integer alarmMaxRetryCount;

    @Value("${kkm.alarm.retry.delay}")
    private Integer retryDelay ;

    @Autowired
    @Qualifier("alarmThreadPoolTaskExecutor")
    ThreadPoolTaskExecutor alarmThreadPoolTaskExecutor;

    @Autowired
    @Qualifier("kafkaThreadPoolTaskScheduler")
    private ThreadPoolTaskScheduler kafkaThreadPoolTaskScheduler;

    @Autowired
    KafkaMessageService kafkaMessageService;

    public void handleKafkaAlarmDto(KkmKafkaAlarmDto dto) {
        try {
            Asset asset = this.getTgkmAsset(dto);
            if(asset==null){
                log.info("asset couldn't find.....{} ", dto.getTgkmName() );
                return;
            }

            switch (dto.getAlarmActionType()) {
                case 0:
                    log.info("save alarm switch case entered");
                    saveOrUpdateAlarmOnKkmAsync(dto, asset);
                    break;
                case 1:
                    ackAlarmOnKkmAsync(dto);
                    break;
                case 2:
                    clearAlarmOnKkmAsync(dto);
                    break;
                default:
                    log.info("undefined alarm action type !");
                    break;
            }

        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    private Asset getTgkmAsset(KkmKafkaAlarmDto dto) throws Exception {
        if(assetMap.containsKey(dto.getTgkmName())){
            return  assetMap.get(dto.getTgkmName());
        }
        else{
            Asset asset = egysRestService.getAssetByName(dto.getTgkmName());
            if(asset !=null){
                assetMap.put(asset.getName(),asset);
                return  asset;
            }
            else {
                asset = egysRestService.saveTgkmAsset(dto);
                if (asset != null) {
                    assetMap.put(asset.getName(),asset);
                    return asset;
                } else{
                    return null;
                }
            }
        }
    }

    public Alarm saveOrUpdateAlarmOnKkm(KkmKafkaAlarmDto dto, Asset  asset) throws JsonMappingException, IOException {

        log.info("saveOrUpdateAlarmOnKkm started");
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Alarm kkmAlarm = mapper.readValue(dto.getAlarmJsonStr(), Alarm.class);


        kkmAlarm.setOriginator(asset.getId());
        kkmAlarm.setType(this.generateKkmAlarmType(dto.getEntityType(), dto.getEntityName(), dto.getAlarmType()));
        ObjectNode details = mapper.createObjectNode();
        details.put("data", dto.getDetails());
        kkmAlarm.setDetails(details);

        kkmAlarm.setId(null);

        Alarm savedAlarm = egysRestService.saveOrUpdateAlarm(kkmAlarm);
        sqlAlarmRepository.save(new SqlAlarmEntity(dto.getAlarmId(), savedAlarm.getId().getId().toString(),
                savedAlarm.getType(), savedAlarm.getName(), savedAlarm.getSeverity().toString(),
                Util.SUCCESS_FAIL.SUCCESS, dto.getRetryCount(),savedAlarm.getStatus().toString())
        );

        return savedAlarm;

    }

    public String findKkmAlarmByTgkmAlarmId(String tgkmAlarmId) {

        SqlAlarmEntity entity = sqlAlarmRepository.findByTgkmAlarmId(tgkmAlarmId);
        if (entity != null)
            return entity.getKkmAlarmId();
        else
            return null;

    }

    public void clearAlarmOnKkm(KkmKafkaAlarmDto dto) throws Exception {

        if (Util.isAlarmActive(AlarmStatus.valueOf(sqlAlarmRepository.findByTgkmAlarmId(dto.getAlarmId()).getAlarmStatus()))){

            String kkmAlarmId = this.findKkmAlarmByTgkmAlarmId(dto.getAlarmId());
            if (StringUtils.isEmpty(kkmAlarmId)) {
                log.info("kkm alarm couldn't find, tgkm:{} alarm:{}", dto.getTgkmName(), dto.getAlarmJsonStr());
                return;
            }

            egysRestService.clearAlarm(kkmAlarmId);

            // update alarm status
            SqlAlarmEntity entity = sqlAlarmRepository.findByTgkmAlarmId(dto.getAlarmId());
            ObjectMapper mapper = new ObjectMapper();
            Alarm alarmDto = mapper.readValue(dto.getAlarmJsonStr(),Alarm.class);
            entity.setAlarmStatus(alarmDto.getStatus().toString());
            sqlAlarmRepository.save(entity);

        } else {
            log.info("alarm already cleared.");
        }

    }

    public void ackAlarmOnKkm(KkmKafkaAlarmDto dto) {
        String kkmAlarmId = this.findKkmAlarmByTgkmAlarmId(dto.getAlarmId());
        if(StringUtils.isEmpty(kkmAlarmId)){
            // find alarm on kkm by alarmType and originatorId
            // kkmAlarmId = this.getKkmLatestAlarmByOriginatorAndType(dto);
            if(StringUtils.isEmpty(kkmAlarmId)){
                log.info("kkm alarm couldn't find, tgkm:{} alarm:{}", dto.getTgkmName() ,dto.getAlarmJsonStr());
                return;
            }
        }
        egysRestService.ackAlarm(kkmAlarmId);
    }

    // TODO
//    private String getKkmLatestAlarmByOriginatorAndType(KkmKafkaAlarmDto dto) {
//        String kkmAlarmId = "";
//        return kkmAlarmId;
//    }

    private String generateKkmAlarmType(String entityType, String entityName, String alarmType){
        return entityName + "-" + alarmType;
    }

    // Async methods
    public Future<Void> clearAlarmOnKkmAsync(KkmKafkaAlarmDto dto) {
        try {
            Callable<Void> callable = () -> {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    this.clearAlarmOnKkm(dto);
                } catch (Exception e) {

                    if( dto.getRetryCount() <= this.alarmMaxRetryCount ){
                        if(dto.getRetryCount()>-1){
                            dto.setRetryCount(dto.getRetryCount()+1);
                        }

                        KkmKafkaAlarmDto finalAlarm = dto;
                        kafkaThreadPoolTaskScheduler.schedule(() -> {
                            try {
                                kafkaMessageService.publish(objectMapper.writeValueAsString(finalAlarm));
                            } catch (JsonProcessingException e1) {
                                log.error("Error while parsing processing Alarm[tgkm_alarm_Id: {}]: {}", finalAlarm.getAlarmId(), e1.getStackTrace());
                            }
                        }, Instant.now().plusMillis(retryDelay));
                    }

                }
                return null;
            };

            return alarmThreadPoolTaskExecutor.submit(callable);

        }catch (HttpClientErrorException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error("create alarm exception: {}", e.getMessage());
        }
        return null;
    }

    public Future<Void> ackAlarmOnKkmAsync(KkmKafkaAlarmDto dto) {
        try {
            Callable<Void> callable = () -> {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    this.ackAlarmOnKkm(dto);
                } catch (Exception e) {

                    if( dto.getRetryCount() <= this.alarmMaxRetryCount ){
                        if(dto.getRetryCount()>-1){
                            dto.setRetryCount(dto.getRetryCount()+1);
                        }

                        KkmKafkaAlarmDto finalAlarm = dto;
                        kafkaThreadPoolTaskScheduler.schedule(() -> {
                            try {
                                kafkaMessageService.publish(objectMapper.writeValueAsString(finalAlarm));
                            } catch (JsonProcessingException e1) {
                                log.error("Error while parsing processing Alarm[tgkm_alarm_Id: {}]: {}", finalAlarm.getAlarmId(), e1.getStackTrace());
                            }
                        }, Instant.now().plusMillis(retryDelay));
                    }

                }
                return  null;
            };

            return alarmThreadPoolTaskExecutor.submit(callable);

        }catch (HttpClientErrorException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error("create alarm exception: {}", e.getMessage());
        }
        return null;
    }

    public Future<Alarm> saveOrUpdateAlarmOnKkmAsync(KkmKafkaAlarmDto dto, Asset asset) {
        try {
            Callable<Alarm> callable = () -> {
                Alarm alarmSaved = null;
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    alarmSaved = saveOrUpdateAlarmOnKkm(dto,asset);
                    log.info("alarm save succesfully");
                } catch (Exception e) {

                    if( dto.getRetryCount() <= this.alarmMaxRetryCount ){
                        if(dto.getRetryCount()>-1){
                            dto.setRetryCount(dto.getRetryCount()+1);
                        }

                        KkmKafkaAlarmDto finalAlarm = dto;
                        kafkaThreadPoolTaskScheduler.schedule(() -> {
                            try {
                                kafkaMessageService.publish(objectMapper.writeValueAsString(finalAlarm));
                            } catch (JsonProcessingException e1) {
                                log.error("Error while parsing processing Alarm[tgkm_alarm_Id: {}]: {}", finalAlarm.getAlarmId(), e1.getStackTrace());
                            }
                        }, Instant.now().plusMillis(retryDelay));
                    }

                }
                return alarmSaved;
            };

            return alarmThreadPoolTaskExecutor.submit(callable);

        }catch (HttpClientErrorException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return null;
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error("create alarm exception: {}", e.getMessage());
            return null;
        }
    }
}
