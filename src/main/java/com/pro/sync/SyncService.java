package com.pro.sync;

import org.jooq.DeleteQuery;
import org.jooq.InsertQuery;
import org.jooq.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SyncService {

    @Autowired
    private SyncConfigs syncConfigs;

    public void sync(String syncName, String json) {
        SyncQueries queries = syncConfigs.generateSyncQueries(syncName, json);
        for (SyncQueries.SyncQuery query : queries.queries) {
            for (DeleteQuery<Record> delete : query.deletes) {
                delete.execute();
            }
            for (InsertQuery<Record> insert : query.inserts) {
                insert.execute();
            }
        }
    }
}
