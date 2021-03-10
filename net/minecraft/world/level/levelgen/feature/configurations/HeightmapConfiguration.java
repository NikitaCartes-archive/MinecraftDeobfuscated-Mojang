/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class HeightmapConfiguration
implements DecoratorConfiguration {
    public static final Codec<HeightmapConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Heightmap.Types.CODEC.fieldOf("heightmap")).forGetter(heightmapConfiguration -> heightmapConfiguration.heightmap)).apply((Applicative<HeightmapConfiguration, ?>)instance, HeightmapConfiguration::new));
    public final Heightmap.Types heightmap;

    public HeightmapConfiguration(Heightmap.Types types) {
        this.heightmap = types;
    }
}

