/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.exception;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class RealmsHttpException
extends RuntimeException {
    public RealmsHttpException(String string, Exception exception) {
        super(string, exception);
    }
}

