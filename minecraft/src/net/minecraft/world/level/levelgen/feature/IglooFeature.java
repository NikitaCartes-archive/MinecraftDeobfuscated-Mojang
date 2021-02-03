package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.IglooPieces;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class IglooFeature extends StructureFeature<NoneFeatureConfiguration> {
	public IglooFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
		return IglooFeature.FeatureStart::new;
	}

	public static class FeatureStart extends StructureStart<NoneFeatureConfiguration> {
		public FeatureStart(StructureFeature<NoneFeatureConfiguration> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
			super(structureFeature, i, j, boundingBox, k, l);
		}

		public void generatePieces(
			RegistryAccess registryAccess,
			ChunkGenerator chunkGenerator,
			StructureManager structureManager,
			int i,
			int j,
			Biome biome,
			NoneFeatureConfiguration noneFeatureConfiguration,
			LevelHeightAccessor levelHeightAccessor
		) {
			int k = SectionPos.sectionToBlockCoord(i);
			int l = SectionPos.sectionToBlockCoord(j);
			BlockPos blockPos = new BlockPos(k, 90, l);
			Rotation rotation = Rotation.getRandom(this.random);
			IglooPieces.addPieces(structureManager, blockPos, rotation, this.pieces, this.random);
			this.calculateBoundingBox();
		}
	}
}
