/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client;

import java.util.function.IntFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.OptionEnum;

@Environment(value=EnvType.CLIENT)
public enum ParticleStatus implements OptionEnum
{
    ALL(0, "options.particles.all"),
    DECREASED(1, "options.particles.decreased"),
    MINIMAL(2, "options.particles.minimal");

    private static final IntFunction<ParticleStatus> BY_ID;
    private final int id;
    private final String key;

    private ParticleStatus(int j, String string2) {
        this.id = j;
        this.key = string2;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public int getId() {
        return this.id;
    }

    public static ParticleStatus byId(int i) {
        return BY_ID.apply(i);
    }

    static {
        BY_ID = ByIdMap.continuous(ParticleStatus::getId, ParticleStatus.values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    }
}

