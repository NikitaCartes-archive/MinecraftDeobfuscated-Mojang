/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.dimension;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSourceType;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.NetherGeneratorSettings;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class NetherDimension
extends Dimension {
    public NetherDimension(Level level, DimensionType dimensionType) {
        super(level, dimensionType);
        this.ultraWarm = true;
        this.hasCeiling = true;
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public Vec3 getFogColor(float f, float g) {
        return new Vec3(0.2f, 0.03f, 0.03f);
    }

    @Override
    protected void updateLightRamp() {
        float f = 0.1f;
        for (int i = 0; i <= 15; ++i) {
            float g = 1.0f - (float)i / 15.0f;
            this.brightnessRamp[i] = (1.0f - g) / (g * 3.0f + 1.0f) * 0.9f + 0.1f;
        }
    }

    @Override
    public ChunkGenerator<?> createRandomLevelGenerator() {
        NetherGeneratorSettings netherGeneratorSettings = ChunkGeneratorType.CAVES.createSettings();
        netherGeneratorSettings.setDefaultBlock(Blocks.NETHERRACK.defaultBlockState());
        netherGeneratorSettings.setDefaultFluid(Blocks.LAVA.defaultBlockState());
        return ChunkGeneratorType.CAVES.create(this.level, BiomeSourceType.FIXED.create(BiomeSourceType.FIXED.createSettings(this.level.getLevelData()).setBiome(Biomes.NETHER)), netherGeneratorSettings);
    }

    @Override
    public boolean isNaturalDimension() {
        return false;
    }

    @Override
    @Nullable
    public BlockPos getSpawnPosInChunk(ChunkPos chunkPos, boolean bl) {
        return null;
    }

    @Override
    @Nullable
    public BlockPos getValidSpawnPosition(int i, int j, boolean bl) {
        return null;
    }

    @Override
    public float getTimeOfDay(long l, float f) {
        return 0.5f;
    }

    @Override
    public boolean mayRespawn() {
        return false;
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public boolean isFoggyAt(int i, int j) {
        return true;
    }

    @Override
    public WorldBorder createWorldBorder() {
        return new WorldBorder(){

            @Override
            public double getCenterX() {
                return super.getCenterX() / 8.0;
            }

            @Override
            public double getCenterZ() {
                return super.getCenterZ() / 8.0;
            }
        };
    }

    @Override
    public DimensionType getType() {
        return DimensionType.NETHER;
    }
}

