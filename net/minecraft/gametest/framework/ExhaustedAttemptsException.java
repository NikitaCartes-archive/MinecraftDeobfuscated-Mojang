/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.gametest.framework;

import net.minecraft.gametest.framework.GameTestInfo;

class ExhaustedAttemptsException
extends Throwable {
    public ExhaustedAttemptsException(int i, int j, GameTestInfo gameTestInfo) {
        super("Not enough successes: " + j + " out of " + i + " attempts. Required successes: " + gameTestInfo.requiredSuccesses() + ". max attempts: " + gameTestInfo.maxAttempts() + ".", gameTestInfo.getError());
    }
}

