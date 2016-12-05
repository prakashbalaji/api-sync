package com.pro.sync;

import org.jooq.Delete;
import org.jooq.DeleteQuery;
import org.jooq.InsertQuery;
import org.jooq.Record;

import java.util.*;

public class SyncQueries {

    Set<SyncQuery> queries = new LinkedHashSet<>();

    public SyncQueries addDeleteQueries(List<Delete<Record>> deletes){
        this.queries.add(SyncQuery.deleteQueries(deletes));
        return this;
    }

    public SyncQueries addInsertQueries(List<InsertQuery<Record>> inserts){
        this.queries.add(SyncQuery.insertQueries(inserts));
        return this;
    }


    public static class SyncQuery {
        List<InsertQuery<Record>> inserts = new ArrayList<>();
        List<Delete<Record>> deletes = new ArrayList<>();



        public static SyncQuery deleteQueries(List<Delete<Record>> deletes) {
            SyncQuery syncQuery = new SyncQuery();
            syncQuery.deletes = deletes;
            return syncQuery;
        }

        public static SyncQuery insertQueries(List<InsertQuery<Record>> inserts) {
            SyncQuery syncQuery = new SyncQuery();
            syncQuery.inserts = inserts;
            return syncQuery;
        }
    }
}
