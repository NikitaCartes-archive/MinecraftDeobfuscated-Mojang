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
public enum AmbientOcclusionStatus implements OptionEnum
{
    OFF(0, "options.ao.off"),
    MIN(1, "options.ao.min"),
    MAX(2, "options.ao.max");

    private static final IntFunction<AmbientOcclusionStatus> BY_ID;
    private final int id;
    private final String key;

    private AmbientOcclusionStatus(int j, String string2) {
        this.id = j;
        this.key = string2;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    public static AmbientOcclusionStatus byId(int i) {
        return BY_ID.apply(i);
    }

    static {
        BY_ID = ByIdMap.continuous(AmbientOcclusionStatus::getId, AmbientOcclusionStatus.values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    }
}

