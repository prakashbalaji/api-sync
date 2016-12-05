package com.pro.stepdefs;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.gson.Gson;
import com.pro.ApiSyncApplication;
import com.pro.event.CustomerEvent;
import com.pro.event.ProviderEvent;
import com.pro.sync.CustomerSyncService;
import com.pro.sync.ProviderSyncService;
import cucumber.api.DataTable;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationContextLoader;
import org.springframework.test.context.ContextConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@ContextConfiguration(classes = ApiSyncApplication.class, loader = SpringApplicationContextLoader.class)
public class MyStepdefs {
    private Gson gson = new Gson();
    private WireMockServer wireMockServer;
    private DSLContext jooqCreate;

    @Autowired
    private CustomerSyncService customerSyncService;

    @Autowired
    private ProviderSyncService providerSyncService;

    @Autowired
    private DataSource dataSource;

    @Before
    public void setUp(){
        wireMockServer = new WireMockServer(wireMockConfig().port(8089));
        WireMock.configureFor("localhost", 8089);
        wireMockServer.start();

        try {
            jooqCreate = DSL.using(dataSource.getConnection(), SQLDialect.POSTGRES);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown(){
        wireMockServer.resetAll();
        wireMockServer.stop();
    }

    @Given("^I have the environment initialized$")
    public void I_have_the_environment_initialized() throws Throwable {
    }


    @Given("^I stub GET to \"([^\"]*)\" with body$")
    public void I_stub_GET_to_with_body(String path, DataTable table) throws Throwable {
        stubFor(get(urlEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(json(table))));
    }

    private String json(DataTable table) {
        return StringUtils.join(table.asList(String.class).toArray(), "");
    }

    @Then("^I raise a sync event for customer \"([^\"]*)\"$")
    public void I_raise_a_sync_event_for_customer(String customerId) throws Throwable {
        customerSyncService.sync(new CustomerEvent(customerId));
    }

    @Then("^I raise a sync event for provider \"([^\"]*)\"$")
    public void I_raise_a_sync_event_for_provider(String providerId) throws Throwable {
        providerSyncService.sync(new ProviderEvent(providerId));
    }


    @Then("^I verify that the table \"([^\"]*)\" has the following entries$")
    public void I_verify_that_the_table_has_the_following_entries(String tableName, DataTable table) throws Throwable {
        List<Map<String, String>> expectedValues = table.asMaps(String.class, String.class);
        Set<String> columnNames = expectedValues.get(0).keySet();


        List<Field<Object>> fields = new ArrayList<>();
        for (String columnName : columnNames) {
            Field<Object> field = DSL.field(columnName);
            fields.add(field);
        }

        List<Map<String, Object>> actualValues = jooqCreate.select(fields).from(tableName).fetchMaps();

        List<Map<String, String>> actualValuesAsString = new ArrayList<>();
        for (Map<String, Object> actualValue : actualValues) {
            Map<String, String> m = new HashMap<>();
            for (String key : actualValue.keySet()) {
                if(actualValue.get(key)!=null){
                    m.put(key, actualValue.get(key).toString());
                }
            }
            actualValuesAsString.add(m);
        }

        assertThat(actualValuesAsString, is(expectedValues));
    }

}

