/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.material.Material;

@Deprecated
public class LakeFeature
extends Feature<Configuration> {
    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();

    public LakeFeature(Codec<Configuration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Configuration> featurePlaceContext) {
        int t;
        int s;
        BlockPos blockPos = featurePlaceContext.origin();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        Random random = featurePlaceContext.random();
        Configuration configuration = featurePlaceContext.config();
        if (blockPos.getY() <= worldGenLevel.getMinBuildHeight() + 4) {
            return false;
        }
        blockPos = blockPos.below(4);
        boolean[] bls = new boolean[2048];
        int i = random.nextInt(4) + 4;
        for (int j = 0; j < i; ++j) {
            double d = random.nextDouble() * 6.0 + 3.0;
            double e = random.nextDouble() * 4.0 + 2.0;
            double f = random.nextDouble() * 6.0 + 3.0;
            double g = random.nextDouble() * (16.0 - d - 2.0) + 1.0 + d / 2.0;
            double h = random.nextDouble() * (8.0 - e - 4.0) + 2.0 + e / 2.0;
            double k = random.nextDouble() * (16.0 - f - 2.0) + 1.0 + f / 2.0;
            for (int l = 1; l < 15; ++l) {
                for (int m = 1; m < 15; ++m) {
                    for (int n = 1; n < 7; ++n) {
                        double o = ((double)l - g) / (d / 2.0);
                        double p = ((double)n - h) / (e / 2.0);
                        double q = ((double)m - k) / (f / 2.0);
                        double r = o * o + p * p + q * q;
                        if (!(r < 1.0)) continue;
                        bls[(l * 16 + m) * 8 + n] = true;
                    }
                }
            }
        }
        BlockState blockState = configuration.fluid().getState(random, blockPos);
        for (s = 0; s < 16; ++s) {
            for (t = 0; t < 16; ++t) {
                for (int u = 0; u < 8; ++u) {
                    boolean bl;
                    boolean bl2 = bl = !bls[(s * 16 + t) * 8 + u] && (s < 15 && bls[((s + 1) * 16 + t) * 8 + u] || s > 0 && bls[((s - 1) * 16 + t) * 8 + u] || t < 15 && bls[(s * 16 + t + 1) * 8 + u] || t > 0 && bls[(s * 16 + (t - 1)) * 8 + u] || u < 7 && bls[(s * 16 + t) * 8 + u + 1] || u > 0 && bls[(s * 16 + t) * 8 + (u - 1)]);
                    if (!bl) continue;
                    Material material = worldGenLevel.getBlockState(blockPos.offset(s, u, t)).getMaterial();
                    if (u >= 4 && material.isLiquid()) {
                        return false;
                    }
                    if (u >= 4 || material.isSolid() || worldGenLevel.getBlockState(blockPos.offset(s, u, t)) == blockState) continue;
                    return false;
                }
            }
        }
        for (s = 0; s < 16; ++s) {
            for (t = 0; t < 16; ++t) {
                for (int u = 0; u < 8; ++u) {
                    BlockPos blockPos2;
                    if (!bls[(s * 16 + t) * 8 + u] || !this.canReplaceBlock(worldGenLevel.getBlockState(blockPos2 = blockPos.offset(s, u, t)))) continue;
                    boolean bl2 = u >= 4;
                    worldGenLevel.setBlock(blockPos2, bl2 ? AIR : blockState, 2);
                    if (!bl2) continue;
                    worldGenLevel.scheduleTick(blockPos2, AIR.getBlock(), 0);
                    this.markAboveForPostProcessing(worldGenLevel, blockPos2);
                }
            }
        }
        BlockState blockState2 = configuration.barrier().getState(random, blockPos);
        if (!blockState2.isAir()) {
            for (t = 0; t < 16; ++t) {
                for (int u = 0; u < 16; ++u) {
                    for (int v = 0; v < 8; ++v) {
                        BlockState blockState3;
                        boolean bl2;
                        boolean bl = bl2 = !bls[(t * 16 + u) * 8 + v] && (t < 15 && bls[((t + 1) * 16 + u) * 8 + v] || t > 0 && bls[((t - 1) * 16 + u) * 8 + v] || u < 15 && bls[(t * 16 + u + 1) * 8 + v] || u > 0 && bls[(t * 16 + (u - 1)) * 8 + v] || v < 7 && bls[(t * 16 + u) * 8 + v + 1] || v > 0 && bls[(t * 16 + u) * 8 + (v - 1)]);
                        if (!bl2 || v >= 4 && random.nextInt(2) == 0 || !(blockState3 = worldGenLevel.getBlockState(blockPos.offset(t, v, u))).getMaterial().isSolid() || blockState3.is(BlockTags.LAVA_POOL_STONE_CANNOT_REPLACE)) continue;
                        BlockPos blockPos3 = blockPos.offset(t, v, u);
                        worldGenLevel.setBlock(blockPos3, blockState2, 2);
                        this.markAboveForPostProcessing(worldGenLevel, blockPos3);
                    }
                }
            }
        }
        if (blockState.getFluidState().is(FluidTags.WATER)) {
            for (t = 0; t < 16; ++t) {
                for (int u = 0; u < 16; ++u) {
                    int v = 4;
                    BlockPos blockPos4 = blockPos.offset(t, 4, u);
                    if (!worldGenLevel.getBiome(blockPos4).value().shouldFreeze(worldGenLevel, blockPos4, false) || !this.canReplaceBlock(worldGenLevel.getBlockState(blockPos4))) continue;
                    worldGenLevel.setBlock(blockPos4, Blocks.ICE.defaultBlockState(), 2);
                }
            }
        }
        return true;
    }

    private boolean canReplaceBlock(BlockState blockState) {
        return !blockState.is(BlockTags.FEATURES_CANNOT_REPLACE);
    }

    public record Configuration(BlockStateProvider fluid, BlockStateProvider barrier) implements FeatureConfiguration
    {
        public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BlockStateProvider.CODEC.fieldOf("fluid")).forGetter(Configuration::fluid), ((MapCodec)BlockStateProvider.CODEC.fieldOf("barrier")).forGetter(Configuration::barrier)).apply((Applicative<Configuration, ?>)instance, Configuration::new));
    }
}

