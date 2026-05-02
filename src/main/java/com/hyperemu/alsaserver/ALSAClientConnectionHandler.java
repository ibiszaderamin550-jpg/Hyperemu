package com.hyperemu.alsaserver;

import com.hyperemu.xconnector.Client;
import com.hyperemu.xconnector.ConnectionHandler;

public class ALSAClientConnectionHandler implements ConnectionHandler {
    @Override
    public void handleNewConnection(Client client) {
        client.createIOStreams();
        client.setTag(new ALSAClient());
    }

    @Override
    public void handleConnectionShutdown(Client client) {
        ((ALSAClient)client.getTag()).release();
    }
}
