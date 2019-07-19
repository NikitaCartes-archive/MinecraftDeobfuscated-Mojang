/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import net.minecraft.core.Position;
import net.minecraft.world.level.Level;

public interface Location
extends Position {
    public Level getLevel();
}

