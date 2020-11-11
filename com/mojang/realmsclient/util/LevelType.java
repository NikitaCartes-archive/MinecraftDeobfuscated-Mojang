/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(value=EnvType.CLIENT)
public enum LevelType {
    DEFAULT(0, new TranslatableComponent("generator.default")),
    FLAT(1, new TranslatableComponent("generator.flat")),
    LARGE_BIOMES(2, new TranslatableComponent("generator.large_biomes")),
    AMPLIFIED(3, new TranslatableComponent("generator.amplified"));

    private final int index;
    private final Component name;

    private LevelType(int j, Component component) {
        this.index = j;
        this.name = component;
    }

    public Component getName() {
        return this.name;
    }

    public int getDtoIndex() {
        return this.index;
    }
}

