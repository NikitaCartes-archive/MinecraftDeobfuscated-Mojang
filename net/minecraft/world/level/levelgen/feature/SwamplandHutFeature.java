/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.RandomScatteredFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.SwamplandHutPiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class SwamplandHutFeature
extends RandomScatteredFeature<NoneFeatureConfiguration> {
    private static final List<Biome.SpawnerData> SWAMPHUT_ENEMIES = Lists.newArrayList(new Biome.SpawnerData(EntityType.WITCH, 1, 1, 1));
    private static final List<Biome.SpawnerData> SWAMPHUT_ANIMALS = Lists.newArrayList(new Biome.SpawnerData(EntityType.CAT, 1, 1, 1));

    public SwamplandHutFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
        super(function);
    }

    @Override
    public String getFeatureName() {
        return "Swamp_Hut";
    }

    @Override
    public int getLookupRange() {
        return 3;
    }

    @Override
    public StructureFeature.StructureStartFactory getStartFactory() {
        return FeatureStart::new;
    }

    @Override
    protected int getRandomSalt() {
        return 14357620;
    }

    @Override
    public List<Biome.SpawnerData> getSpecialEnemies() {
        return SWAMPHUT_ENEMIES;
    }

    @Override
    public List<Biome.SpawnerData> getSpecialAnimals() {
        return SWAMPHUT_ANIMALS;
    }

    public boolean isSwamphut(LevelAccessor levelAccessor, BlockPos blockPos) {
        StructureStart structureStart = this.getStructureAt(levelAccessor, blockPos, true);
        if (structureStart == StructureStart.INVALID_START || !(structureStart instanceof FeatureStart) || structureStart.getPieces().isEmpty()) {
            return false;
        }
        StructurePiece structurePiece = structureStart.getPieces().get(0);
        return structurePiece instanceof SwamplandHutPiece;
    }

    public static class FeatureStart
    extends StructureStart {
        public FeatureStart(StructureFeature<?> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
            super(structureFeature, i, j, boundingBox, k, l);
        }

        @Override
        public void generatePieces(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, int i, int j, Biome biome) {
            SwamplandHutPiece swamplandHutPiece = new SwamplandHutPiece(this.random, i * 16, j * 16);
            this.pieces.add(swamplandHutPiece);
            this.calculateBoundingBox();
        }
    }
}

