package com.pro.sync;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang3.StringUtils;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.*;
import java.util.stream.Collectors;

public class SyncConfigs {

    Map<String, SyncConfig> syncConfigMap = new HashMap<>();
    private DSLContext jooqCreate;

    public SyncConfigs(DSLContext jooqCreate) {

        this.jooqCreate = jooqCreate;
    }

    public SyncConfigs addSyncConfig(String name, SyncConfig config){
        syncConfigMap.put(name, config);
        return this;
    }

    public SyncQueries generateSyncQueries(String syncName, String apiResponse) {
        return syncConfigMap.get(syncName).generateSyncQueries(jooqCreate, apiResponse);
    }

    public static class SyncConfig{
        String tableName;
        List<String> deleteBy = new ArrayList<>();
        Map<String,String> apiToDBField = new HashMap<>();
        Map<String,String> dbFieldToApiField = new HashMap<>();


        public SyncConfig addTableName(String tableName){
            this.tableName = tableName;
            return this;
        }

        public SyncConfig addDeleteBy(String deleteBy){
            this.deleteBy.add(deleteBy);
            return this;
        }

        public SyncConfig addFieldToCopy(String apiFieldPath, String dbFieldName){
            apiToDBField.put(apiFieldPath, dbFieldName);
            dbFieldToApiField.put(dbFieldName, apiFieldPath);
            return this;
        }

        public SyncQueries generateSyncQueries(DSLContext jooqCreate, String apiResponse) {
            SyncQueries result = new SyncQueries();
            DocumentContext parsedJson = JsonPath.parse(apiResponse);
            if(deleteBy.size()>0){
                result.addDeleteQueries(generateDeleteStatements(jooqCreate, parsedJson));
            }
            result.addInsertQueries(generateInsertStatements(jooqCreate, parsedJson));
            return result;
        }

        private List<DeleteQuery<Record>> generateDeleteStatements(DSLContext jooqCreate, DocumentContext parsedJson) {
            List<DeleteQuery<Record>> result = new ArrayList<>();
            Object deleteKey = parsedJson.read("$." + deleteBy.get(0));
            DeleteQuery<Record> deleteQuery = jooqCreate.deleteQuery(DSL.table(tableName));
            deleteQuery.addConditions(DSL.condition(dbFieldNameOfApiField(deleteBy.get(0))+"=?",deleteKey));
            result.add(deleteQuery);
            return result;
        }


        private List<InsertQuery<Record>> generateInsertStatements(DSLContext jooqCreate, DocumentContext parsedJson) {
            List<InsertQuery<Record>> result = new ArrayList<>();
            Map<String, Object> valuesToInsert = parsedJson.read("$.[" + StringUtils.join(singleQuotedApiFields(), ",") + "]");
            InsertQuery<Record> insertQuery = jooqCreate.insertQuery(DSL.table(tableName));
            for (String dbFieldName : dbFieldToApiField.keySet()) {
                Field<Object> field = DSL.field(dbFieldName);
                Object value = insertValue(dbFieldToApiField.get(dbFieldName), valuesToInsert);
                insertQuery.addValue(field, value);
            }

            result.add(insertQuery);
            return result;
        }

        private Object insertValue(String apiField, Map<String, Object> valuesToInsert) {
            return valuesToInsert.get(apiField);
        }

        private Set<String> singleQuotedApiFields() {
            return apiToDBField.keySet().stream().map(apiField -> "'" + apiField + "'").collect(Collectors.toSet());
        }

        private String dbFieldNameOfApiField(String apiFieldName) {
            return apiToDBField.get(apiFieldName);
        }

    }
}
