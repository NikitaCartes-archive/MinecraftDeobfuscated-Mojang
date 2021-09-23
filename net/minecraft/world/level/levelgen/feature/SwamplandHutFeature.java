/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.SwamplandHutPiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class SwamplandHutFeature
extends StructureFeature<NoneFeatureConfiguration> {
    public static final WeightedRandomList<MobSpawnSettings.SpawnerData> SWAMPHUT_ENEMIES = WeightedRandomList.create((WeightedEntry[])new MobSpawnSettings.SpawnerData[]{new MobSpawnSettings.SpawnerData(EntityType.WITCH, 1, 1, 1)});
    public static final WeightedRandomList<MobSpawnSettings.SpawnerData> SWAMPHUT_ANIMALS = WeightedRandomList.create((WeightedEntry[])new MobSpawnSettings.SpawnerData[]{new MobSpawnSettings.SpawnerData(EntityType.CAT, 1, 1, 1)});

    public SwamplandHutFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec, SwamplandHutFeature::generatePieces);
    }

    private static void generatePieces(StructurePiecesBuilder structurePiecesBuilder, NoneFeatureConfiguration noneFeatureConfiguration, PieceGenerator.Context context) {
        if (!context.validBiomeOnTop(Heightmap.Types.WORLD_SURFACE_WG)) {
            return;
        }
        structurePiecesBuilder.addPiece(new SwamplandHutPiece(context.random(), context.chunkPos().getMinBlockX(), context.chunkPos().getMinBlockZ()));
    }
}

