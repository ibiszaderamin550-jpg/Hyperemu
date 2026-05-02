package com.hyperemu.xenvironment.components;

import com.hyperemu.alsaserver.ALSAClientConnectionHandler;
import com.hyperemu.alsaserver.ALSARequestHandler;
import com.hyperemu.xconnector.UnixSocketConfig;
import com.hyperemu.xconnector.XConnectorEpoll;
import com.hyperemu.xenvironment.EnvironmentComponent;

public class ALSAServerComponent extends EnvironmentComponent {
    private XConnectorEpoll connector;
    private final UnixSocketConfig socketConfig;

    public ALSAServerComponent(UnixSocketConfig socketConfig) {
        this.socketConfig = socketConfig;
    }

    @Override
    public void start() {
        if (connector != null) return;
        connector = new XConnectorEpoll(socketConfig, new ALSAClientConnectionHandler(), new ALSARequestHandler());
        connector.setMultithreadedClients(true);
        connector.start();
    }

    @Override
    public void stop() {
        if (connector != null) {
            connector.stop();
            connector = null;
        }
    }
}
