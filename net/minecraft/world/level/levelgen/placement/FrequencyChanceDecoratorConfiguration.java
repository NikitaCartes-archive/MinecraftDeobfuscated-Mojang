/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class FrequencyChanceDecoratorConfiguration
implements DecoratorConfiguration {
    public final int count;
    public final float chance;

    public FrequencyChanceDecoratorConfiguration(int i, float f) {
        this.count = i;
        this.chance = f;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
        return new Dynamic<T>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("count"), dynamicOps.createInt(this.count), dynamicOps.createString("chance"), dynamicOps.createFloat(this.chance))));
    }

    public static FrequencyChanceDecoratorConfiguration deserialize(Dynamic<?> dynamic) {
        int i = dynamic.get("count").asInt(0);
        float f = dynamic.get("chance").asFloat(0.0f);
        return new FrequencyChanceDecoratorConfiguration(i, f);
    }
}

