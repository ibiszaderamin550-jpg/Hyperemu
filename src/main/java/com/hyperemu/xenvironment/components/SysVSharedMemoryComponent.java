package com.hyperemu.xenvironment.components;

import com.hyperemu.sysvshm.SysVSHMConnectionHandler;
import com.hyperemu.sysvshm.SysVSHMRequestHandler;
import com.hyperemu.sysvshm.SysVSharedMemory;
import com.hyperemu.xconnector.UnixSocketConfig;
import com.hyperemu.xconnector.XConnectorEpoll;
import com.hyperemu.xenvironment.EnvironmentComponent;
import com.hyperemu.xserver.SHMSegmentManager;
import com.hyperemu.xserver.XServer;

public class SysVSharedMemoryComponent extends EnvironmentComponent {
    private XConnectorEpoll connector;
    public final UnixSocketConfig socketConfig;
    private SysVSharedMemory sysVSharedMemory;
    private final XServer xServer;

    public SysVSharedMemoryComponent(XServer xServer, UnixSocketConfig socketConfig) {
        this.xServer = xServer;
        this.socketConfig = socketConfig;
    }

    @Override
    public void start() {
        if (connector != null) return;
        sysVSharedMemory = new SysVSharedMemory();
        connector = new XConnectorEpoll(socketConfig, new SysVSHMConnectionHandler(sysVSharedMemory), new SysVSHMRequestHandler());
        connector.start();

        xServer.setSHMSegmentManager(new SHMSegmentManager(sysVSharedMemory));
    }

    @Override
    public void stop() {
        if (connector != null) {
            connector.stop();
            connector = null;
        }

        sysVSharedMemory.deleteAll();
    }
}
