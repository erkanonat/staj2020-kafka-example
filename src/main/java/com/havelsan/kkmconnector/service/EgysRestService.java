package com.havelsan.kkmconnector.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.havelsan.kkmconnector.client.MyRestClient;
import com.havelsan.kkmconnector.model.KkmKafkaAlarmDto;
import com.havelsan.kkmconnector.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.thingsboard.server.common.data.UUIDConverter;
import org.thingsboard.server.common.data.alarm.Alarm;
import org.thingsboard.server.common.data.alarm.AlarmId;
import org.thingsboard.server.common.data.alarm.AlarmSeverity;
import org.thingsboard.server.common.data.asset.Asset;
import org.thingsboard.server.common.data.relation.EntityRelation;
import org.thingsboard.server.common.data.relation.RelationTypeGroup;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;


@Service
@Slf4j
public class EgysRestService {

    @Value("${kkm.restclient.endpoint}")
    private String EGYS_REST_ENDPOINT ;

    @Value("${kkm.restclient.username}")
    private String USERNAME ;

    @Value("${kkm.restclient.password}")
    private String PASSWORD ;

    private MyRestClient egysRestClient;

    private Logger logger = Logger.getLogger(this.getClass().getName());
    private ObjectMapper mapper = new ObjectMapper();

    public EgysRestService() {
    }

    @PostConstruct
    public void init(){

        egysRestClient = new MyRestClient(EGYS_REST_ENDPOINT);
        egysRestClient.login(USERNAME,PASSWORD);

    }


    // find asset
    public Asset getAssetByName(String assetName) throws Exception{

        try {
            Optional<Asset> result =  egysRestClient.getTenantAsset(assetName);
            if(result.isPresent()) {
                return result.get();
            }
            return null;

        }catch (HttpClientErrorException e) {

            log.info("couldn't find asset on kkm: "+ e.getMessage());
            return null;
        }
    }

    public void saveAssetContainsRelation(Asset asset) throws Exception {
        try {

            Asset genel_asset = this.getAssetByName(Util.GENEL_ASSET);
            if (genel_asset != null) {
                EntityRelation er = new EntityRelation();
                er.setFrom(genel_asset.getId());
                er.setTo(asset.getId());
                er.setAdditionalInfo(null);
                er.setType(Util.ENTITY_RELATION_TYPE.CONTAINS_TYPE);
                er.setTypeGroup(RelationTypeGroup.COMMON);
                egysRestClient.saveRelation(er);

            }

            else {
                log.info("genel asset couldn't find on kkm");
            }
        }
        catch(Exception e){
                log.error("asset relation couldn't be saved: {}", e.getMessage());
        }
    }

    public void saveAssetLatLon(Asset asset, double lat, double lon){

        try {

            ObjectNode node = mapper.createObjectNode();
            node.put("lat",lat);
            node.put("lon",lon);
            egysRestClient.saveEntityAttributesV1(asset.getId(), "SERVER_SCOPE", node);
        }
        catch (Exception e) {
            log.error("asset attribute couldn't be saved: {}" ,e.getMessage());
        }
    }

    // create asset
    public Asset saveTgkmAsset(KkmKafkaAlarmDto dto) throws  JSONException, JsonProcessingException {

        try {
            Asset asset = new Asset();
            asset.setName(dto.getTgkmName());
            asset.setType("TGKM");
            ObjectNode info = mapper.createObjectNode();
            info.put("description","TGKM");
            asset.setAdditionalInfo(info);
            asset.setCreatedTime(System.currentTimeMillis());

            Asset newAsset = egysRestClient.saveAsset(asset);
            if (newAsset != null && newAsset.getId() != null) {
                this.saveAssetLatLon(newAsset, dto.getTgkmLat(),dto.getTgkmLon());
                this.saveAssetContainsRelation(newAsset);
            }
            return newAsset;
        }catch (Exception e) {
            log.error("asset couldn't created on kkm: {}" ,e.getMessage());
            return null;
        }
    }

    // send alarm2KKM
    public Alarm saveOrUpdateAlarm(Alarm alarm) {

        try {

            Alarm savedAlarm = egysRestClient.saveAlarm(alarm);
            if (savedAlarm != null && savedAlarm.getId() != null) {
                log.info("alarm successfully created on kkm");
                return savedAlarm;
            } else {
                log.info("alarm couldn't created/updated");
                return null;
            }

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

    // clear alarm On KKM
    public void clearAlarm(String alarmId){
        try {
            egysRestClient.clearAlarm( AlarmId.fromString(alarmId) );
            log.info("alarm successfully cleared");

        }catch (HttpClientErrorException ex) {
            ex.printStackTrace();
            log.error("alarm couldn't cleared: {} " , ex.toString());
        }catch (Exception ex) {
            ex.printStackTrace();
            log.error("alarm couldn't cleared: {} " , ex.toString());
        }
    }

    // ack alarm On KKM
    public void ackAlarm(String alarmId){
        try {

            egysRestClient.ackAlarm(AlarmId.fromString(alarmId));
            log.info("alarm successfully acknowledged");
        }catch (HttpClientErrorException ex) {
            ex.printStackTrace();
            log.error("alarm couldn't acknowledged: {} " , ex.getMessage());
        }catch (Exception ex) {
            ex.printStackTrace();
            log.error("alarm couldn't acknowledged: {} " , ex.getMessage());
        }
    }


}
