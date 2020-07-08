/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class HugeMushroomFeatureConfiguration
implements FeatureConfiguration {
    public static final Codec<HugeMushroomFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BlockStateProvider.CODEC.fieldOf("cap_provider")).forGetter(hugeMushroomFeatureConfiguration -> hugeMushroomFeatureConfiguration.capProvider), ((MapCodec)BlockStateProvider.CODEC.fieldOf("stem_provider")).forGetter(hugeMushroomFeatureConfiguration -> hugeMushroomFeatureConfiguration.stemProvider), ((MapCodec)Codec.INT.fieldOf("foliage_radius")).orElse(2).forGetter(hugeMushroomFeatureConfiguration -> hugeMushroomFeatureConfiguration.foliageRadius)).apply((Applicative<HugeMushroomFeatureConfiguration, ?>)instance, HugeMushroomFeatureConfiguration::new));
    public final BlockStateProvider capProvider;
    public final BlockStateProvider stemProvider;
    public final int foliageRadius;

    public HugeMushroomFeatureConfiguration(BlockStateProvider blockStateProvider, BlockStateProvider blockStateProvider2, int i) {
        this.capProvider = blockStateProvider;
        this.stemProvider = blockStateProvider2;
        this.foliageRadius = i;
    }
}

