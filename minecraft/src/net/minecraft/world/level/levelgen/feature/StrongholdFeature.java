package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.NoiseAffectingStructureStart;
import net.minecraft.world.level.levelgen.structure.StrongholdPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class StrongholdFeature extends StructureFeature<NoneFeatureConfiguration> {
	public StrongholdFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
		return StrongholdFeature.StrongholdStart::new;
	}

	protected boolean isFeatureChunk(
		ChunkGenerator chunkGenerator,
		BiomeSource biomeSource,
		long l,
		WorldgenRandom worldgenRandom,
		ChunkPos chunkPos,
		Biome biome,
		ChunkPos chunkPos2,
		NoneFeatureConfiguration noneFeatureConfiguration,
		LevelHeightAccessor levelHeightAccessor
	) {
		return chunkGenerator.hasStronghold(chunkPos);
	}

	public static class StrongholdStart extends NoiseAffectingStructureStart<NoneFeatureConfiguration> {
		private final long seed;

		public StrongholdStart(StructureFeature<NoneFeatureConfiguration> structureFeature, ChunkPos chunkPos, int i, long l) {
			super(structureFeature, chunkPos, i, l);
			this.seed = l;
		}

		public void generatePieces(
			RegistryAccess registryAccess,
			ChunkGenerator chunkGenerator,
			StructureManager structureManager,
			ChunkPos chunkPos,
			Biome biome,
			NoneFeatureConfiguration noneFeatureConfiguration,
			LevelHeightAccessor levelHeightAccessor
		) {
			int i = 0;

			StrongholdPieces.StartPiece startPiece;
			do {
				this.clearPieces();
				this.random.setLargeFeatureSeed(this.seed + (long)(i++), chunkPos.x, chunkPos.z);
				StrongholdPieces.resetPieces();
				startPiece = new StrongholdPieces.StartPiece(this.random, chunkPos.getBlockX(2), chunkPos.getBlockZ(2));
				this.addPiece(startPiece);
				startPiece.addChildren(startPiece, this, this.random);
				List<StructurePiece> list = startPiece.pendingChildren;

				while (!list.isEmpty()) {
					int j = this.random.nextInt(list.size());
					StructurePiece structurePiece = (StructurePiece)list.remove(j);
					structurePiece.addChildren(startPiece, this, this.random);
				}

				this.moveBelowSeaLevel(chunkGenerator.getSeaLevel(), chunkGenerator.getMinY(), this.random, 10);
			} while (this.hasNoPieces() || startPiece.portalRoomPiece == null);
		}
	}
}
