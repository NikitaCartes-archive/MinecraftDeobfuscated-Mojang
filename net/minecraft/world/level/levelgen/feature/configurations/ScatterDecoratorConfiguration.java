/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class ScatterDecoratorConfiguration
implements DecoratorConfiguration {
    public static final Codec<ScatterDecoratorConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)IntProvider.codec(-16, 16).fieldOf("xz_spread")).forGetter(scatterDecoratorConfiguration -> scatterDecoratorConfiguration.xzSpread), ((MapCodec)IntProvider.codec(-16, 16).fieldOf("y_spread")).forGetter(scatterDecoratorConfiguration -> scatterDecoratorConfiguration.ySpread)).apply((Applicative<ScatterDecoratorConfiguration, ?>)instance, ScatterDecoratorConfiguration::new));
    public final IntProvider xzSpread;
    public final IntProvider ySpread;

    public ScatterDecoratorConfiguration(IntProvider intProvider, IntProvider intProvider2) {
        this.xzSpread = intProvider;
        this.ySpread = intProvider2;
    }
}

