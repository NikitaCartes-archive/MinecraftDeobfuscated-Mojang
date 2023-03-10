/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.carver;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class CarverConfiguration
extends ProbabilityFeatureConfiguration {
    public static final MapCodec<CarverConfiguration> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("probability")).forGetter(carverConfiguration -> Float.valueOf(carverConfiguration.probability)), ((MapCodec)HeightProvider.CODEC.fieldOf("y")).forGetter(carverConfiguration -> carverConfiguration.y), ((MapCodec)FloatProvider.CODEC.fieldOf("yScale")).forGetter(carverConfiguration -> carverConfiguration.yScale), ((MapCodec)VerticalAnchor.CODEC.fieldOf("lava_level")).forGetter(carverConfiguration -> carverConfiguration.lavaLevel), CarverDebugSettings.CODEC.optionalFieldOf("debug_settings", CarverDebugSettings.DEFAULT).forGetter(carverConfiguration -> carverConfiguration.debugSettings), ((MapCodec)RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("replaceable")).forGetter(carverConfiguration -> carverConfiguration.replaceable)).apply((Applicative<CarverConfiguration, ?>)instance, CarverConfiguration::new));
    public final HeightProvider y;
    public final FloatProvider yScale;
    public final VerticalAnchor lavaLevel;
    public final CarverDebugSettings debugSettings;
    public final HolderSet<Block> replaceable;

    public CarverConfiguration(float f, HeightProvider heightProvider, FloatProvider floatProvider, VerticalAnchor verticalAnchor, CarverDebugSettings carverDebugSettings, HolderSet<Block> holderSet) {
        super(f);
        this.y = heightProvider;
        this.yScale = floatProvider;
        this.lavaLevel = verticalAnchor;
        this.debugSettings = carverDebugSettings;
        this.replaceable = holderSet;
    }
}

