package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.BuriedTreasureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.BuriedTreasurePieces;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class BuriedTreasureFeature extends StructureFeature<BuriedTreasureConfiguration> {
	public BuriedTreasureFeature(Codec<BuriedTreasureConfiguration> codec) {
		super(codec);
	}

	protected boolean isFeatureChunk(
		ChunkGenerator chunkGenerator,
		BiomeSource biomeSource,
		long l,
		WorldgenRandom worldgenRandom,
		int i,
		int j,
		Biome biome,
		ChunkPos chunkPos,
		BuriedTreasureConfiguration buriedTreasureConfiguration
	) {
		worldgenRandom.setLargeFeatureWithSalt(l, i, j, 10387320);
		return worldgenRandom.nextFloat() < buriedTreasureConfiguration.probability;
	}

	@Override
	public StructureFeature.StructureStartFactory<BuriedTreasureConfiguration> getStartFactory() {
		return BuriedTreasureFeature.BuriedTreasureStart::new;
	}

	public static class BuriedTreasureStart extends StructureStart<BuriedTreasureConfiguration> {
		public BuriedTreasureStart(StructureFeature<BuriedTreasureConfiguration> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
			super(structureFeature, i, j, boundingBox, k, l);
		}

		public void generatePieces(
			ChunkGenerator chunkGenerator, StructureManager structureManager, int i, int j, Biome biome, BuriedTreasureConfiguration buriedTreasureConfiguration
		) {
			int k = i * 16;
			int l = j * 16;
			BlockPos blockPos = new BlockPos(k + 9, 90, l + 9);
			this.pieces.add(new BuriedTreasurePieces.BuriedTreasurePiece(blockPos));
			this.calculateBoundingBox();
		}

		@Override
		public BlockPos getLocatePos() {
			return new BlockPos((this.getChunkX() << 4) + 9, 0, (this.getChunkZ() << 4) + 9);
		}
	}
}
