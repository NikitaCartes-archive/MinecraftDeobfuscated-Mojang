/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network;

import io.netty.handler.codec.EncoderException;

public class SkipPacketException
extends EncoderException {
    public SkipPacketException(Throwable throwable) {
        super(throwable);
    }
}

