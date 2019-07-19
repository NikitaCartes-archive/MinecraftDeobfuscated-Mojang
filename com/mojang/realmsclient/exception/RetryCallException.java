/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.exception;

import com.mojang.realmsclient.exception.RealmsServiceException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class RetryCallException
extends RealmsServiceException {
    public final int delaySeconds;

    public RetryCallException(int i) {
        super(503, "Retry operation", -1, "");
        this.delaySeconds = i < 0 || i > 120 ? 5 : i;
    }
}

