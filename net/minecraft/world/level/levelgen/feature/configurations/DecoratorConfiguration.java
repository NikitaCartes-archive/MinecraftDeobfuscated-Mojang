/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public interface DecoratorConfiguration {
    public static final NoneDecoratorConfiguration NONE = new NoneDecoratorConfiguration();

    public <T> Dynamic<T> serialize(DynamicOps<T> var1);
}

