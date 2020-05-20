/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class JigsawConfiguration
implements FeatureConfiguration {
    public static final Codec<JigsawConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ResourceLocation.CODEC.fieldOf("start_pool")).forGetter(JigsawConfiguration::getStartPool), ((MapCodec)Codec.INT.fieldOf("size")).forGetter(JigsawConfiguration::getSize)).apply((Applicative<JigsawConfiguration, ?>)instance, JigsawConfiguration::new));
    public final ResourceLocation startPool;
    public final int size;

    public JigsawConfiguration(ResourceLocation resourceLocation, int i) {
        this.startPool = resourceLocation;
        this.size = i;
    }

    public int getSize() {
        return this.size;
    }

    public ResourceLocation getStartPool() {
        return this.startPool;
    }
}

