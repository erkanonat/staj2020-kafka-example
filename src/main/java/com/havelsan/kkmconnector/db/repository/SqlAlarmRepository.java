package com.havelsan.kkmconnector.db.repository;

import com.havelsan.kkmconnector.db.entity.sql.SqlAlarmEntity;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository("sqlAlarmRepository")
public interface SqlAlarmRepository extends PagingAndSortingRepository<SqlAlarmEntity, String> {

    public SqlAlarmEntity findByTgkmAlarmId(String tgkmAlarmId);

    public SqlAlarmEntity findByKkmAlarmId(String kkmAlarmId);


}
