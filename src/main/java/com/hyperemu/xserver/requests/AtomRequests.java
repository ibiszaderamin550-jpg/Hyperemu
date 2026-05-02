package com.hyperemu.xserver.requests;

import static com.hyperemu.xserver.XClientRequestHandler.RESPONSE_CODE_SUCCESS;

import com.hyperemu.xconnector.XInputStream;
import com.hyperemu.xconnector.XOutputStream;
import com.hyperemu.xconnector.XStreamLock;
import com.hyperemu.xserver.Atom;
import com.hyperemu.xserver.XClient;
import com.hyperemu.xserver.errors.BadAtom;
import com.hyperemu.xserver.errors.XRequestError;

import java.io.IOException;

public abstract class AtomRequests {
    public static void internAtom(XClient client, XInputStream inputStream, XOutputStream outputStream) throws IOException, XRequestError {
        boolean onlyIfExists = client.getRequestData() == 1;
        short length = inputStream.readShort();
        inputStream.skip(2);
        String name = inputStream.readString8(length);
        int id = onlyIfExists ? Atom.getId(name) : Atom.internAtom(name);
        if (id < 0) throw new BadAtom(id);

        try (XStreamLock lock = outputStream.lock()) {
            outputStream.writeByte(RESPONSE_CODE_SUCCESS);
            outputStream.writeByte((byte)0);
            outputStream.writeShort(client.getSequenceNumber());
            outputStream.writeInt(0);
            outputStream.writeInt(id);
            outputStream.writePad(20);
        }
    }
}
