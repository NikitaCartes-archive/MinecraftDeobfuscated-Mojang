/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.JunglePyramidPiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class JunglePyramidFeature
extends StructureFeature<NoneFeatureConfiguration> {
    public JunglePyramidFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
        return FeatureStart::new;
    }

    public static class FeatureStart
    extends StructureStart<NoneFeatureConfiguration> {
        public FeatureStart(StructureFeature<NoneFeatureConfiguration> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
            super(structureFeature, i, j, boundingBox, k, l);
        }

        @Override
        public void generatePieces(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, StructureManager structureManager, int i, int j, Biome biome, NoneFeatureConfiguration noneFeatureConfiguration) {
            JunglePyramidPiece junglePyramidPiece = new JunglePyramidPiece(this.random, SectionPos.sectionToBlockCoord(i), SectionPos.sectionToBlockCoord(j));
            this.pieces.add(junglePyramidPiece);
            this.calculateBoundingBox();
        }
    }
}

