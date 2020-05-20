/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class ChanceDecoratorConfiguration
implements DecoratorConfiguration {
    public static final Codec<ChanceDecoratorConfiguration> CODEC = ((MapCodec)Codec.INT.fieldOf("chance")).xmap(ChanceDecoratorConfiguration::new, chanceDecoratorConfiguration -> chanceDecoratorConfiguration.chance).codec();
    public final int chance;

    public ChanceDecoratorConfiguration(int i) {
        this.chance = i;
    }
}

