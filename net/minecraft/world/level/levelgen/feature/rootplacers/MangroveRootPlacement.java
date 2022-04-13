/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.rootplacers;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record MangroveRootPlacement(HolderSet<Block> canGrowThrough, HolderSet<Block> muddyRootsIn, BlockStateProvider muddyRootsProvider, int maxRootWidth, int maxRootLength, float randomSkewChance) {
    public static final Codec<MangroveRootPlacement> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)RegistryCodecs.homogeneousList(Registry.BLOCK_REGISTRY).fieldOf("can_grow_through")).forGetter(mangroveRootPlacement -> mangroveRootPlacement.canGrowThrough), ((MapCodec)RegistryCodecs.homogeneousList(Registry.BLOCK_REGISTRY).fieldOf("muddy_roots_in")).forGetter(mangroveRootPlacement -> mangroveRootPlacement.muddyRootsIn), ((MapCodec)BlockStateProvider.CODEC.fieldOf("muddy_roots_provider")).forGetter(mangroveRootPlacement -> mangroveRootPlacement.muddyRootsProvider), ((MapCodec)Codec.intRange(1, 12).fieldOf("max_root_width")).forGetter(mangroveRootPlacement -> mangroveRootPlacement.maxRootWidth), ((MapCodec)Codec.intRange(1, 64).fieldOf("max_root_length")).forGetter(mangroveRootPlacement -> mangroveRootPlacement.maxRootLength), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("random_skew_chance")).forGetter(mangroveRootPlacement -> Float.valueOf(mangroveRootPlacement.randomSkewChance))).apply((Applicative<MangroveRootPlacement, ?>)instance, MangroveRootPlacement::new));
}

