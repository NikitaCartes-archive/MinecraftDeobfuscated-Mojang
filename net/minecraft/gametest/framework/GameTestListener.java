/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.gametest.framework;

import net.minecraft.gametest.framework.GameTestInfo;

public interface GameTestListener {
    public void testStructureLoaded(GameTestInfo var1);

    public void testPassed(GameTestInfo var1);

    public void testFailed(GameTestInfo var1);
}

