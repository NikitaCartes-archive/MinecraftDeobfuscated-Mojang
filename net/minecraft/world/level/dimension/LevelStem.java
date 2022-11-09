/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.dimension;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;

public record LevelStem(Holder<DimensionType> type, ChunkGenerator generator) {
    public static final Codec<LevelStem> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)DimensionType.CODEC.fieldOf("type")).forGetter(LevelStem::type), ((MapCodec)ChunkGenerator.CODEC.fieldOf("generator")).forGetter(LevelStem::generator)).apply((Applicative<LevelStem, ?>)instance, instance.stable(LevelStem::new)));
    public static final ResourceKey<LevelStem> OVERWORLD = ResourceKey.create(Registries.LEVEL_STEM, new ResourceLocation("overworld"));
    public static final ResourceKey<LevelStem> NETHER = ResourceKey.create(Registries.LEVEL_STEM, new ResourceLocation("the_nether"));
    public static final ResourceKey<LevelStem> END = ResourceKey.create(Registries.LEVEL_STEM, new ResourceLocation("the_end"));
}

