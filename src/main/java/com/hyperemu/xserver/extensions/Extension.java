package com.hyperemu.xserver.extensions;

import com.hyperemu.xconnector.XInputStream;
import com.hyperemu.xconnector.XOutputStream;
import com.hyperemu.xserver.XClient;
import com.hyperemu.xserver.errors.XRequestError;

import java.io.IOException;

public interface Extension {
    String getName();

    byte getMajorOpcode();

    byte getFirstErrorId();

    byte getFirstEventId();

    void handleRequest(XClient client, XInputStream inputStream, XOutputStream outputStream) throws IOException, XRequestError;
}
