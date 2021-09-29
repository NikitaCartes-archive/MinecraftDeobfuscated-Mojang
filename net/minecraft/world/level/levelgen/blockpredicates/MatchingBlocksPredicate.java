/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;

class MatchingBlocksPredicate
implements BlockPredicate {
    private final List<Block> blocks;
    private final BlockPos offset;
    public static final Codec<MatchingBlocksPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Registry.BLOCK.listOf().fieldOf("blocks")).forGetter(matchingBlocksPredicate -> matchingBlocksPredicate.blocks), ((MapCodec)BlockPos.CODEC.fieldOf("offset")).forGetter(matchingBlocksPredicate -> matchingBlocksPredicate.offset)).apply((Applicative<MatchingBlocksPredicate, ?>)instance, MatchingBlocksPredicate::new));

    public MatchingBlocksPredicate(List<Block> list, BlockPos blockPos) {
        this.blocks = list;
        this.offset = blockPos;
    }

    @Override
    public boolean test(WorldGenLevel worldGenLevel, BlockPos blockPos) {
        Block block = worldGenLevel.getBlockState(blockPos.offset(this.offset)).getBlock();
        return this.blocks.contains(block);
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.MATCHING_BLOCKS;
    }

    @Override
    public /* synthetic */ boolean test(Object object, Object object2) {
        return this.test((WorldGenLevel)object, (BlockPos)object2);
    }
}

