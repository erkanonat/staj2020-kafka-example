package com.havelsan.kkmconnector.util;

import org.thingsboard.server.common.data.alarm.AlarmStatus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class Util {

    private static final String KKM_TIMESTAMP_FORMAT = "HH:mm:ss dd-MM-yyyy";

    public static final String GENEL_ASSET ="TURKIYE";

    public static class ASSET_ATTRIBUTES{
        public static final String alarmCount="alarmCount";
    }

    public static class ENTITY_RELATION_TYPE {
        public static final String CONTAINS_TYPE = "Contains";
        public static final String MANAGES_TYPE = "Manages";
    }

    public static Long parse(String timestamp) throws ParseException {
        Long dateTime;
        SimpleDateFormat dateFormat = new SimpleDateFormat(KKM_TIMESTAMP_FORMAT);
        Date parsedDate = dateFormat.parse(timestamp);
        dateTime = parsedDate.getTime();

        return dateTime;

    }

    public static class SUCCESS_FAIL {
        public static final int SUCCESS=1;
        public static final int FAILED =0;
    }

    public static class REST_RESPONSE_STATUS {
        public static final int SUCCESS=1;
        public static final int FAILED =0;
    }

    public static final int HATA_KODU_BASARILI=0;

    public static class EGYS_REST_SERVICES {
        public static final String CREATE_ALARM="/api/alarm";
        public static final String CLEAR_ALARM="/api/alarm/${ALARM_ID}/clear";
        public static final String ACK_ALARM="/api/alarm/${ALARM_ID}/ack";
        public static final String SAVE_RELATION = "api/relation";

        public static final String GET_ASSET_BY_NAME="/api/tenant/assets";
        public static final String GET_DEVICE_CREDENTIALS="/api/device/${DEVICE_ID}/credentials";

        public static final String CREATE_ASSET = "/api/asset";
        public static final String SAVE_ASSET_ATTRIBUTE = "/api/plugins/telemetry/ASSET/${ASSET_UUID}/SERVER_SCOPE";
        public static final String LIST_ATTRIBUTE = "/api/plugins/telemetry/${ENTITY_TYPE}/${ENTITY_ID}/values/attributes";

        public static final String DEVICE_ID_PLACEHOLDER = "${DEVICE_ID}";
        public static final String ALARM_ID_PLACEHOLDER = "${ALARM_ID}";
        public static final String ASSET_UUID_PLACEHOLDER = "${ASSET_UUID}";
        public static final String ENTITY_TYPE_PLACEHOLDER = "${ENTITY_TYPE}";
        public static final String ENTITY_ID_PLACEHOLDER = "${ENTITY_ID}";

        public static String getListAttributesEndpoint(String entityType , String entityId ){
            return LIST_ATTRIBUTE.replace(ENTITY_TYPE_PLACEHOLDER, entityType).replace(ENTITY_ID_PLACEHOLDER,entityId);
        }

        public static String getDeviceCredentialsEndpoint(String  deviceID) {
            return GET_DEVICE_CREDENTIALS.replace(DEVICE_ID_PLACEHOLDER, deviceID);
        }

        public static String getClearAlarmEndpoint(String alarmId){
            return CLEAR_ALARM.replace(ALARM_ID_PLACEHOLDER, alarmId);
        }
        public static String getAckAlarmEndpoint(String alarmId){
            return ACK_ALARM.replace(ALARM_ID_PLACEHOLDER, alarmId);
        }

        public static String getSaveAssetAttributeEndpoint(String assetUuid) {
            return SAVE_ASSET_ATTRIBUTE.replace(ASSET_UUID_PLACEHOLDER, assetUuid );
        }
    }

    public static String getActionTime() {
        LocalDateTime now = LocalDateTime.now();
        Date from = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        return getStringDate(from);
    }
    public static String getStringDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return format.format(date);
    }

    public static boolean isAlarmActive(AlarmStatus status){
        if(AlarmStatus.ACTIVE_UNACK.equals(status) || AlarmStatus.ACTIVE_ACK.equals(status)){
            return  true;
        }else {
            return  false;
        }
    }

}
