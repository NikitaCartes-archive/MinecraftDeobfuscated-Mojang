package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.EndCityPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class EndCityFeature extends StructureFeature<NoneFeatureConfiguration> {
	private static final int RANDOM_SALT = 10387313;

	public EndCityFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	protected boolean linearSeparation() {
		return false;
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
		return getYPositionForFeature(chunkPos, chunkGenerator, levelHeightAccessor) >= 60;
	}

	@Override
	public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
		return EndCityFeature.EndCityStart::new;
	}

	static int getYPositionForFeature(ChunkPos chunkPos, ChunkGenerator chunkGenerator, LevelHeightAccessor levelHeightAccessor) {
		Random random = new Random((long)(chunkPos.x + chunkPos.z * 10387313));
		Rotation rotation = Rotation.getRandom(random);
		int i = 5;
		int j = 5;
		if (rotation == Rotation.CLOCKWISE_90) {
			i = -5;
		} else if (rotation == Rotation.CLOCKWISE_180) {
			i = -5;
			j = -5;
		} else if (rotation == Rotation.COUNTERCLOCKWISE_90) {
			j = -5;
		}

		int k = chunkPos.getBlockX(7);
		int l = chunkPos.getBlockZ(7);
		int m = chunkGenerator.getFirstOccupiedHeight(k, l, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor);
		int n = chunkGenerator.getFirstOccupiedHeight(k, l + j, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor);
		int o = chunkGenerator.getFirstOccupiedHeight(k + i, l, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor);
		int p = chunkGenerator.getFirstOccupiedHeight(k + i, l + j, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor);
		return Math.min(Math.min(m, n), Math.min(o, p));
	}

	public static class EndCityStart extends StructureStart<NoneFeatureConfiguration> {
		public EndCityStart(StructureFeature<NoneFeatureConfiguration> structureFeature, ChunkPos chunkPos, int i, long l) {
			super(structureFeature, chunkPos, i, l);
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
			Rotation rotation = Rotation.getRandom(this.random);
			int i = EndCityFeature.getYPositionForFeature(chunkPos, chunkGenerator, levelHeightAccessor);
			if (i >= 60) {
				BlockPos blockPos = chunkPos.getMiddleBlockPosition(i);
				List<StructurePiece> list = Lists.<StructurePiece>newArrayList();
				EndCityPieces.startHouseTower(structureManager, blockPos, rotation, list, this.random);
				list.forEach(this::addPiece);
			}
		}
	}
}
