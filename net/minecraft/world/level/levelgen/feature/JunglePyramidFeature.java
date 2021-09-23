/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.JunglePyramidPiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class JunglePyramidFeature
extends StructureFeature<NoneFeatureConfiguration> {
    public JunglePyramidFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec, JunglePyramidFeature::generatePieces);
    }

    private static void generatePieces(StructurePiecesBuilder structurePiecesBuilder, NoneFeatureConfiguration noneFeatureConfiguration, PieceGenerator.Context context) {
        if (!context.validBiomeOnTop(Heightmap.Types.WORLD_SURFACE_WG)) {
            return;
        }
        if (context.getLowestY(12, 15) < context.chunkGenerator().getSeaLevel()) {
            return;
        }
        structurePiecesBuilder.addPiece(new JunglePyramidPiece(context.random(), context.chunkPos().getMinBlockX(), context.chunkPos().getMinBlockZ()));
    }
}

