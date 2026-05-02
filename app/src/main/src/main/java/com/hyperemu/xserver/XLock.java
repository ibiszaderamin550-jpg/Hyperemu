package com.hyperemu.xserver;

public interface XLock extends AutoCloseable {
    @Override
    void close();
}
