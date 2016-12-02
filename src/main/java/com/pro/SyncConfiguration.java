package com.pro;

import com.pro.sync.SyncConfigs;
import com.pro.sync.SyncConfigs.SyncConfig;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;

@Configuration
public class SyncConfiguration {

    @Autowired
    private DataSource dataSource;

    @Bean
    public SyncConfigs getSyncConfigs(){
        DSLContext jooqCreate = null;
        try {
            jooqCreate = DSL.using(dataSource.getConnection(), SQLDialect.POSTGRES);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        SyncConfig customerSyncConfig = new SyncConfig()
                .addTableName("customers")
                .addDeleteBy("customerId")
                .addFieldToCopy("customerId", "customer_id")
                .addFieldToCopy("firstName", "first_name")
                .addFieldToCopy("lastName", "last_name");

        SyncConfig providerSyncConfig = new SyncConfig()
                .addTableName("providers")
                .addDeleteBy("providerId")
                .addFieldToCopy("providerId", "provider_id")
                .addFieldToCopy("firstName", "first_name")
                .addFieldToCopy("lastName", "last_name");

        return new SyncConfigs(jooqCreate)
                .addSyncConfig("customers", customerSyncConfig)
                .addSyncConfig("providers", providerSyncConfig
                );
    }
}
