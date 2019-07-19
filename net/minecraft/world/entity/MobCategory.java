/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum MobCategory {
    MONSTER("monster", 70, false, false),
    CREATURE("creature", 10, true, true),
    AMBIENT("ambient", 15, true, false),
    WATER_CREATURE("water_creature", 15, true, false),
    MISC("misc", 15, true, false);

    private static final Map<String, MobCategory> BY_NAME;
    private final int max;
    private final boolean isFriendly;
    private final boolean isPersistent;
    private final String name;

    private MobCategory(String string2, int j, boolean bl, boolean bl2) {
        this.name = string2;
        this.max = j;
        this.isFriendly = bl;
        this.isPersistent = bl2;
    }

    public String getName() {
        return this.name;
    }

    public int getMaxInstancesPerChunk() {
        return this.max;
    }

    public boolean isFriendly() {
        return this.isFriendly;
    }

    public boolean isPersistent() {
        return this.isPersistent;
    }

    static {
        BY_NAME = Arrays.stream(MobCategory.values()).collect(Collectors.toMap(MobCategory::getName, mobCategory -> mobCategory));
    }
}

