package com.hyperemu.sysvshm;

import com.hyperemu.xconnector.Client;
import com.hyperemu.xconnector.ConnectionHandler;

public class SysVSHMConnectionHandler implements ConnectionHandler {
    private final SysVSharedMemory sysVSharedMemory;

    public SysVSHMConnectionHandler(SysVSharedMemory sysVSharedMemory) {
        this.sysVSharedMemory = sysVSharedMemory;
    }

    @Override
    public void handleNewConnection(Client client) {
        client.createIOStreams();
        client.setTag(sysVSharedMemory);
    }

    @Override
    public void handleConnectionShutdown(Client client) {}
}
