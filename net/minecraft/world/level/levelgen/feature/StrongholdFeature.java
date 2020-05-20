/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StrongholdPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class StrongholdFeature
extends StructureFeature<NoneFeatureConfiguration> {
    public StrongholdFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
        return StrongholdStart::new;
    }

    @Override
    protected boolean isFeatureChunk(ChunkGenerator chunkGenerator, BiomeSource biomeSource, long l, WorldgenRandom worldgenRandom, int i, int j, Biome biome, ChunkPos chunkPos, NoneFeatureConfiguration noneFeatureConfiguration) {
        return chunkGenerator.hasStronghold(new ChunkPos(i, j));
    }

    public static class StrongholdStart
    extends StructureStart<NoneFeatureConfiguration> {
        private final long seed;

        public StrongholdStart(StructureFeature<NoneFeatureConfiguration> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
            super(structureFeature, i, j, boundingBox, k, l);
            this.seed = l;
        }

        @Override
        public void generatePieces(ChunkGenerator chunkGenerator, StructureManager structureManager, int i, int j, Biome biome, NoneFeatureConfiguration noneFeatureConfiguration) {
            StrongholdPieces.StartPiece startPiece;
            int k = 0;
            do {
                this.pieces.clear();
                this.boundingBox = BoundingBox.getUnknownBox();
                this.random.setLargeFeatureSeed(this.seed + (long)k++, i, j);
                StrongholdPieces.resetPieces();
                startPiece = new StrongholdPieces.StartPiece(this.random, (i << 4) + 2, (j << 4) + 2);
                this.pieces.add(startPiece);
                startPiece.addChildren(startPiece, this.pieces, this.random);
                List<StructurePiece> list = startPiece.pendingChildren;
                while (!list.isEmpty()) {
                    int l = this.random.nextInt(list.size());
                    StructurePiece structurePiece = list.remove(l);
                    structurePiece.addChildren(startPiece, this.pieces, this.random);
                }
                this.calculateBoundingBox();
                this.moveBelowSeaLevel(chunkGenerator.getSeaLevel(), this.random, 10);
            } while (this.pieces.isEmpty() || startPiece.portalRoomPiece == null);
        }
    }
}

