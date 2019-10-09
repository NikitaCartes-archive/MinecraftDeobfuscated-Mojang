/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class NoneDecoratorConfiguration
implements DecoratorConfiguration {
    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
        return new Dynamic<T>(dynamicOps, dynamicOps.emptyMap());
    }

    public static NoneDecoratorConfiguration deserialize(Dynamic<?> dynamic) {
        return new NoneDecoratorConfiguration();
    }
}

