/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.rootplacers;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record AboveRootPlacement(BlockStateProvider aboveRootProvider, float aboveRootPlacementChance) {
    public static final Codec<AboveRootPlacement> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BlockStateProvider.CODEC.fieldOf("above_root_provider")).forGetter(aboveRootPlacement -> aboveRootPlacement.aboveRootProvider), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("above_root_placement_chance")).forGetter(aboveRootPlacement -> Float.valueOf(aboveRootPlacement.aboveRootPlacementChance))).apply((Applicative<AboveRootPlacement, ?>)instance, AboveRootPlacement::new));
}

