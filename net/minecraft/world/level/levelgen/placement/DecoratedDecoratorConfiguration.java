/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.ConfiguredDecorator;

public class DecoratedDecoratorConfiguration
implements DecoratorConfiguration {
    public static final Codec<DecoratedDecoratorConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ConfiguredDecorator.CODEC.fieldOf("outer")).forGetter(DecoratedDecoratorConfiguration::outer), ((MapCodec)ConfiguredDecorator.CODEC.fieldOf("inner")).forGetter(DecoratedDecoratorConfiguration::inner)).apply((Applicative<DecoratedDecoratorConfiguration, ?>)instance, DecoratedDecoratorConfiguration::new));
    private final ConfiguredDecorator<?> outer;
    private final ConfiguredDecorator<?> inner;

    public DecoratedDecoratorConfiguration(ConfiguredDecorator<?> configuredDecorator, ConfiguredDecorator<?> configuredDecorator2) {
        this.outer = configuredDecorator;
        this.inner = configuredDecorator2;
    }

    public ConfiguredDecorator<?> outer() {
        return this.outer;
    }

    public ConfiguredDecorator<?> inner() {
        return this.inner;
    }
}

