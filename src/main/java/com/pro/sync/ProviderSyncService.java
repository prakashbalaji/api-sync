package com.pro.sync;


import com.pro.client.ProviderService;
import com.pro.event.ProviderEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
public class ProviderSyncService {

    @Autowired
    private ProviderService providerService;

    @Autowired
    private SyncService syncService;

    @Transactional
    public void sync(ProviderEvent providerEvent) throws IOException {
        String json = providerService.get(providerEvent.getProviderId()).execute().body().string();
        syncService.sync("providers", json);
    }


}
