package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BeardedStructureStart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PillagerOutpostPieces;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class PillagerOutpostFeature extends StructureFeature<NoneFeatureConfiguration> {
	private static final List<Biome.SpawnerData> OUTPOST_ENEMIES = Lists.<Biome.SpawnerData>newArrayList(new Biome.SpawnerData(EntityType.PILLAGER, 1, 1, 1));

	public PillagerOutpostFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public List<Biome.SpawnerData> getSpecialEnemies() {
		return OUTPOST_ENEMIES;
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
		NoneFeatureConfiguration noneFeatureConfiguration
	) {
		int k = i >> 4;
		int m = j >> 4;
		worldgenRandom.setSeed((long)(k ^ m << 4) ^ l);
		worldgenRandom.nextInt();
		if (worldgenRandom.nextInt(5) != 0) {
			return false;
		} else {
			for (int n = i - 10; n <= i + 10; n++) {
				for (int o = j - 10; o <= j + 10; o++) {
					ChunkPos chunkPos2 = StructureFeature.VILLAGE
						.getPotentialFeatureChunk(chunkGenerator.getSettings().getConfig(StructureFeature.VILLAGE), l, worldgenRandom, n, o);
					if (n == chunkPos2.x && o == chunkPos2.z) {
						return false;
					}
				}
			}

			return true;
		}
	}

	@Override
	public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
		return PillagerOutpostFeature.FeatureStart::new;
	}

	public static class FeatureStart extends BeardedStructureStart<NoneFeatureConfiguration> {
		public FeatureStart(StructureFeature<NoneFeatureConfiguration> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
			super(structureFeature, i, j, boundingBox, k, l);
		}

		public void generatePieces(
			ChunkGenerator chunkGenerator, StructureManager structureManager, int i, int j, Biome biome, NoneFeatureConfiguration noneFeatureConfiguration
		) {
			BlockPos blockPos = new BlockPos(i * 16, 0, j * 16);
			PillagerOutpostPieces.addPieces(chunkGenerator, structureManager, blockPos, this.pieces, this.random);
			this.calculateBoundingBox();
		}
	}
}
