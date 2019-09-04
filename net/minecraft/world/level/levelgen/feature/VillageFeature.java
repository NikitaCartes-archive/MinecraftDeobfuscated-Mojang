/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.VillageConfiguration;
import net.minecraft.world.level.levelgen.feature.VillagePieces;
import net.minecraft.world.level.levelgen.structure.BeardedStructureStart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class VillageFeature
extends StructureFeature<VillageConfiguration> {
    public VillageFeature(Function<Dynamic<?>, ? extends VillageConfiguration> function) {
        super(function);
    }

    @Override
    protected ChunkPos getPotentialFeatureChunkFromLocationWithOffset(ChunkGenerator<?> chunkGenerator, Random random, int i, int j, int k, int l) {
        int m = ((ChunkGeneratorSettings)chunkGenerator.getSettings()).getVillagesSpacing();
        int n = ((ChunkGeneratorSettings)chunkGenerator.getSettings()).getVillagesSeparation();
        int o = i + m * k;
        int p = j + m * l;
        int q = o < 0 ? o - m + 1 : o;
        int r = p < 0 ? p - m + 1 : p;
        int s = q / m;
        int t = r / m;
        ((WorldgenRandom)random).setLargeFeatureWithSalt(chunkGenerator.getSeed(), s, t, 10387312);
        s *= m;
        t *= m;
        return new ChunkPos(s += random.nextInt(m - n), t += random.nextInt(m - n));
    }

    @Override
    public boolean isFeatureChunk(BiomeManager biomeManager, ChunkGenerator<?> chunkGenerator, Random random, int i, int j, Biome biome) {
        ChunkPos chunkPos = this.getPotentialFeatureChunkFromLocationWithOffset(chunkGenerator, random, i, j, 0, 0);
        if (i == chunkPos.x && j == chunkPos.z) {
            return chunkGenerator.isBiomeValidStartForStructure(biome, Feature.VILLAGE);
        }
        return false;
    }

    @Override
    public StructureFeature.StructureStartFactory getStartFactory() {
        return FeatureStart::new;
    }

    @Override
    public String getFeatureName() {
        return "Village";
    }

    @Override
    public int getLookupRange() {
        return 8;
    }

    public static class FeatureStart
    extends BeardedStructureStart {
        public FeatureStart(StructureFeature<?> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
            super(structureFeature, i, j, boundingBox, k, l);
        }

        @Override
        public void generatePieces(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, int i, int j, Biome biome) {
            VillageConfiguration villageConfiguration = chunkGenerator.getStructureConfiguration(biome, Feature.VILLAGE);
            BlockPos blockPos = new BlockPos(i * 16, 0, j * 16);
            VillagePieces.addPieces(chunkGenerator, structureManager, blockPos, this.pieces, this.random, villageConfiguration);
            this.calculateBoundingBox();
        }
    }
}

