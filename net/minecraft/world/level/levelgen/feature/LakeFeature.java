/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.material.Material;

public class LakeFeature
extends Feature<BlockStateConfiguration> {
    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();

    public LakeFeature(Codec<BlockStateConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, BlockStateConfiguration blockStateConfiguration) {
        int t;
        int j;
        while (blockPos.getY() > 5 && worldGenLevel.isEmptyBlock(blockPos)) {
            blockPos = blockPos.below();
        }
        if (blockPos.getY() <= 4) {
            return false;
        }
        if (worldGenLevel.startsForFeature(SectionPos.of(blockPos = blockPos.below(4)), StructureFeature.VILLAGE).findAny().isPresent()) {
            return false;
        }
        boolean[] bls = new boolean[2048];
        int i = random.nextInt(4) + 4;
        for (j = 0; j < i; ++j) {
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
        for (j = 0; j < 16; ++j) {
            for (int s = 0; s < 16; ++s) {
                for (t = 0; t < 8; ++t) {
                    boolean bl;
                    boolean bl2 = bl = !bls[(j * 16 + s) * 8 + t] && (j < 15 && bls[((j + 1) * 16 + s) * 8 + t] || j > 0 && bls[((j - 1) * 16 + s) * 8 + t] || s < 15 && bls[(j * 16 + s + 1) * 8 + t] || s > 0 && bls[(j * 16 + (s - 1)) * 8 + t] || t < 7 && bls[(j * 16 + s) * 8 + t + 1] || t > 0 && bls[(j * 16 + s) * 8 + (t - 1)]);
                    if (!bl) continue;
                    Material material = worldGenLevel.getBlockState(blockPos.offset(j, t, s)).getMaterial();
                    if (t >= 4 && material.isLiquid()) {
                        return false;
                    }
                    if (t >= 4 || material.isSolid() || worldGenLevel.getBlockState(blockPos.offset(j, t, s)) == blockStateConfiguration.state) continue;
                    return false;
                }
            }
        }
        for (j = 0; j < 16; ++j) {
            for (int s = 0; s < 16; ++s) {
                for (t = 0; t < 8; ++t) {
                    if (!bls[(j * 16 + s) * 8 + t]) continue;
                    worldGenLevel.setBlock(blockPos.offset(j, t, s), t >= 4 ? AIR : blockStateConfiguration.state, 2);
                }
            }
        }
        for (j = 0; j < 16; ++j) {
            for (int s = 0; s < 16; ++s) {
                for (t = 4; t < 8; ++t) {
                    BlockPos blockPos2;
                    if (!bls[(j * 16 + s) * 8 + t] || !LakeFeature.isDirt(worldGenLevel.getBlockState(blockPos2 = blockPos.offset(j, t - 1, s)).getBlock()) || worldGenLevel.getBrightness(LightLayer.SKY, blockPos.offset(j, t, s)) <= 0) continue;
                    Biome biome = worldGenLevel.getBiome(blockPos2);
                    if (biome.getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial().is(Blocks.MYCELIUM)) {
                        worldGenLevel.setBlock(blockPos2, Blocks.MYCELIUM.defaultBlockState(), 2);
                        continue;
                    }
                    worldGenLevel.setBlock(blockPos2, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                }
            }
        }
        if (blockStateConfiguration.state.getMaterial() == Material.LAVA) {
            for (j = 0; j < 16; ++j) {
                for (int s = 0; s < 16; ++s) {
                    for (t = 0; t < 8; ++t) {
                        boolean bl;
                        boolean bl3 = bl = !bls[(j * 16 + s) * 8 + t] && (j < 15 && bls[((j + 1) * 16 + s) * 8 + t] || j > 0 && bls[((j - 1) * 16 + s) * 8 + t] || s < 15 && bls[(j * 16 + s + 1) * 8 + t] || s > 0 && bls[(j * 16 + (s - 1)) * 8 + t] || t < 7 && bls[(j * 16 + s) * 8 + t + 1] || t > 0 && bls[(j * 16 + s) * 8 + (t - 1)]);
                        if (!bl || t >= 4 && random.nextInt(2) == 0 || !worldGenLevel.getBlockState(blockPos.offset(j, t, s)).getMaterial().isSolid()) continue;
                        worldGenLevel.setBlock(blockPos.offset(j, t, s), Blocks.STONE.defaultBlockState(), 2);
                    }
                }
            }
        }
        if (blockStateConfiguration.state.getMaterial() == Material.WATER) {
            for (j = 0; j < 16; ++j) {
                for (int s = 0; s < 16; ++s) {
                    t = 4;
                    BlockPos blockPos2 = blockPos.offset(j, 4, s);
                    if (!worldGenLevel.getBiome(blockPos2).shouldFreeze(worldGenLevel, blockPos2, false)) continue;
                    worldGenLevel.setBlock(blockPos2, Blocks.ICE.defaultBlockState(), 2);
                }
            }
        }
        return true;
    }
}

