package com.pro.sync;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

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
        return syncConfigMap.get(syncName).generateSyncQueries(syncConfigMap, jooqCreate, apiResponse);
    }

    public static class SyncConfig{
        String tableName;
        List<String> deleteBy = new ArrayList<>();
        Map<String,String> apiToDBField = new HashMap<>();
        Map<String,String> dbFieldToApiField = new HashMap<>();
        List<SyncAssociation> associations = new ArrayList<>();


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

        public SyncConfig addAssociation(String associationType, String name, String deleteBy) {
            associations.add(new SyncAssociation(associationType, name, deleteBy));
            return this;
        }


        public SyncQueries generateSyncQueries(Map<String, SyncConfig> syncConfigMap, DSLContext jooqCreate, String apiResponse) {
            SyncQueries result = new SyncQueries();
            DocumentContext parsedJson = JsonPath.parse(apiResponse);
            for (SyncAssociation association : associations) {
                result.addDeleteQueries(association.generateDeleteStatements(this, syncConfigMap.get(association.getName()), jooqCreate, parsedJson));
            }

            if(deleteBy.size()>0){
                Object key = parsedJson.read("$." + deleteBy.get(0));
                List<Object> deleteKeys = new ArrayList<>();
                deleteKeys.add(key);
                result.addDeleteQueries(generateDeleteStatements(jooqCreate, deleteKeys));
            }
            result.addInsertQueries(asList(generateInsertStatement(jooqCreate, parsedJson.read("$.[" + StringUtils.join(singleQuotedApiFields(), ",") + "]"))));

            for (SyncAssociation association : associations) {
                result.addInsertQueries(association.generateInsertStatements(this,syncConfigMap.get(association.getName()), jooqCreate, parsedJson));
            }

            return result;
        }

        private List<Delete<Record>> generateDeleteStatements(DSLContext jooqCreate, List<Object> deleteKeys) {
            List<Delete<Record>> result = new ArrayList<>();
            result.add(jooqCreate.delete(DSL.table(tableName)).where(DSL.field(dbFieldNameOfApiField(deleteBy.get(0))).in(deleteKeys)));
            return result;
        }

        private List<Delete<Record>> generateDeleteStatements(DSLContext jooqCreate, Collection<Object> deleteKeys, List<String> deleteBy) {
            List<Delete<Record>> result = new ArrayList<>();
            result.add(jooqCreate.delete(DSL.table(tableName)).where(DSL.field(dbFieldNameOfApiField(deleteBy.get(0))).in(deleteKeys)));
            return result;
        }


        private InsertQuery<Record> generateInsertStatement(DSLContext jooqCreate, Map<String, Object> row) {
            InsertQuery<Record> insertQuery = jooqCreate.insertQuery(DSL.table(tableName));
            for (String dbFieldName : dbFieldToApiField.keySet()) {
                Field<Object> field = DSL.field(dbFieldName);
                Object value = insertValue(dbFieldToApiField.get(dbFieldName), row);
                insertQuery.addValue(field, value);
            }

            return insertQuery;
        }

        private List<InsertQuery<Record>> generateInsertStatements(DSLContext jooqCreate, List<Map<String, Object>> rows) {
            List<InsertQuery<Record>> result = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                result.add(generateInsertStatement(jooqCreate, row));
            }
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

    @Data
    public static class SyncAssociation {
        private  String associationType;
        private  String name;
        private  List<String> deleteBy = new ArrayList<>();

        public SyncAssociation(String associationType, String name, String...deleteBy) {
            this.associationType = associationType;
            this.name = name;
            for (String d : deleteBy) {
                this.deleteBy.add(d);
            }
        }

        public List<Delete<Record>> generateDeleteStatements(SyncConfig parentConfig, SyncConfig syncConfig, DSLContext jooqCreate, DocumentContext parsedJson) {
            List<Object> deleteKeys = parsedJson.read("$." + name + ".." + deleteBy.get(0));
            return syncConfig.generateDeleteStatements(jooqCreate, new HashSet<>(deleteKeys), deleteBy);

        }

        public List<InsertQuery<Record>> generateInsertStatements(SyncConfig parentConfig, SyncConfig syncConfig, DSLContext jooqCreate, DocumentContext parsedJson) {
            List<Map<String, Object>> rows = parsedJson.read("$." + name + "..[" + StringUtils.join(syncConfig.singleQuotedApiFields(), ",") + "]");
            return syncConfig.generateInsertStatements(jooqCreate, rows);
        }
    }
}
