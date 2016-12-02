package com.pro.sync;


import com.pro.client.CustomerService;
import com.pro.event.CustomerEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
public class CustomerSyncService {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private SyncService syncService;

    @Transactional
    public void sync(CustomerEvent customerEvent) throws IOException {
        String json = customerService.get(customerEvent.getCustomerId()).execute().body().string();
        syncService.sync("customers", json);
    }


}
