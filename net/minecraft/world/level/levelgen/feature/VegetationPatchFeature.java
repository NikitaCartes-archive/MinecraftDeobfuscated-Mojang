/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;

public class VegetationPatchFeature
extends Feature<VegetationPatchConfiguration> {
    public VegetationPatchFeature(Codec<VegetationPatchConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<VegetationPatchConfiguration> featurePlaceContext) {
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        VegetationPatchConfiguration vegetationPatchConfiguration = featurePlaceContext.config();
        Random random = featurePlaceContext.random();
        BlockPos blockPos = featurePlaceContext.origin();
        Predicate<BlockState> predicate = VegetationPatchFeature.getReplaceableTag(vegetationPatchConfiguration);
        int i = vegetationPatchConfiguration.xzRadius.sample(random) + 1;
        int j = vegetationPatchConfiguration.xzRadius.sample(random) + 1;
        Set<BlockPos> set = this.placeGroundPatch(worldGenLevel, vegetationPatchConfiguration, random, blockPos, predicate, i, j);
        this.distributeVegetation(featurePlaceContext, worldGenLevel, vegetationPatchConfiguration, random, set, i, j);
        return !set.isEmpty();
    }

    protected Set<BlockPos> placeGroundPatch(WorldGenLevel worldGenLevel, VegetationPatchConfiguration vegetationPatchConfiguration, Random random, BlockPos blockPos, Predicate<BlockState> predicate, int i, int j) {
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        BlockPos.MutableBlockPos mutableBlockPos2 = mutableBlockPos.mutable();
        Direction direction = vegetationPatchConfiguration.surface.getDirection();
        Direction direction2 = direction.getOpposite();
        HashSet<BlockPos> set = new HashSet<BlockPos>();
        for (int k = -i; k <= i; ++k) {
            boolean bl = k == -i || k == i;
            for (int l = -j; l <= j; ++l) {
                int m;
                boolean bl5;
                boolean bl2 = l == -j || l == j;
                boolean bl3 = bl || bl2;
                boolean bl4 = bl && bl2;
                boolean bl6 = bl5 = bl3 && !bl4;
                if (bl4 || bl5 && (vegetationPatchConfiguration.extraEdgeColumnChance == 0.0f || random.nextFloat() > vegetationPatchConfiguration.extraEdgeColumnChance)) continue;
                mutableBlockPos.setWithOffset(blockPos, k, 0, l);
                for (m = 0; worldGenLevel.isStateAtPosition(mutableBlockPos, BlockBehaviour.BlockStateBase::isAir) && m < vegetationPatchConfiguration.verticalRange; ++m) {
                    mutableBlockPos.move(direction);
                }
                for (m = 0; worldGenLevel.isStateAtPosition(mutableBlockPos, blockState -> !blockState.isAir()) && m < vegetationPatchConfiguration.verticalRange; ++m) {
                    mutableBlockPos.move(direction2);
                }
                mutableBlockPos2.setWithOffset(mutableBlockPos, vegetationPatchConfiguration.surface.getDirection());
                BlockState blockState2 = worldGenLevel.getBlockState(mutableBlockPos2);
                if (!worldGenLevel.isEmptyBlock(mutableBlockPos) || !blockState2.isFaceSturdy(worldGenLevel, mutableBlockPos2, vegetationPatchConfiguration.surface.getDirection().getOpposite())) continue;
                int n = vegetationPatchConfiguration.depth.sample(random) + (vegetationPatchConfiguration.extraBottomBlockChance > 0.0f && random.nextFloat() < vegetationPatchConfiguration.extraBottomBlockChance ? 1 : 0);
                BlockPos blockPos2 = mutableBlockPos2.immutable();
                boolean bl62 = this.placeGround(worldGenLevel, vegetationPatchConfiguration, predicate, random, mutableBlockPos2, n);
                if (!bl62) continue;
                set.add(blockPos2);
            }
        }
        return set;
    }

    protected void distributeVegetation(FeaturePlaceContext<VegetationPatchConfiguration> featurePlaceContext, WorldGenLevel worldGenLevel, VegetationPatchConfiguration vegetationPatchConfiguration, Random random, Set<BlockPos> set, int i, int j) {
        for (BlockPos blockPos : set) {
            if (!(vegetationPatchConfiguration.vegetationChance > 0.0f) || !(random.nextFloat() < vegetationPatchConfiguration.vegetationChance)) continue;
            this.placeVegetation(worldGenLevel, vegetationPatchConfiguration, featurePlaceContext.chunkGenerator(), random, blockPos);
        }
    }

    protected boolean placeVegetation(WorldGenLevel worldGenLevel, VegetationPatchConfiguration vegetationPatchConfiguration, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos) {
        return vegetationPatchConfiguration.vegetationFeature.get().place(worldGenLevel, chunkGenerator, random, blockPos.above());
    }

    protected boolean placeGround(WorldGenLevel worldGenLevel, VegetationPatchConfiguration vegetationPatchConfiguration, Predicate<BlockState> predicate, Random random, BlockPos.MutableBlockPos mutableBlockPos, int i) {
        for (int j = 0; j < i; ++j) {
            if (!predicate.test(worldGenLevel.getBlockState(mutableBlockPos))) {
                return j != 0;
            }
            worldGenLevel.setBlock(mutableBlockPos, vegetationPatchConfiguration.groundState.getState(random, mutableBlockPos), 2);
            mutableBlockPos.move(vegetationPatchConfiguration.surface.getDirection());
        }
        return true;
    }

    private static Predicate<BlockState> getReplaceableTag(VegetationPatchConfiguration vegetationPatchConfiguration) {
        Tag<Block> tag = BlockTags.getAllTags().getTag(vegetationPatchConfiguration.replaceable);
        return tag == null ? blockState -> true : blockState -> blockState.is(tag);
    }
}

