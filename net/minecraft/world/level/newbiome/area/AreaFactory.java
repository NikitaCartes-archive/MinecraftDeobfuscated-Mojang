/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.newbiome.area;

import net.minecraft.world.level.newbiome.area.Area;

public interface AreaFactory<A extends Area> {
    public A make();
}

