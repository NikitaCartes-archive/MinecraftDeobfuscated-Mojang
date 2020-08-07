package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;

public class PillagerOutpostFeature extends JigsawFeature {
	private static final List<MobSpawnSettings.SpawnerData> OUTPOST_ENEMIES = ImmutableList.of(new MobSpawnSettings.SpawnerData(EntityType.PILLAGER, 1, 1, 1));

	public PillagerOutpostFeature(Codec<JigsawConfiguration> codec) {
		super(codec, 0, true, true);
	}

	@Override
	public List<MobSpawnSettings.SpawnerData> getSpecialEnemies() {
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
		JigsawConfiguration jigsawConfiguration
	) {
		int k = i >> 4;
		int m = j >> 4;
		worldgenRandom.setSeed((long)(k ^ m << 4) ^ l);
		worldgenRandom.nextInt();
		return worldgenRandom.nextInt(5) != 0 ? false : !this.isNearVillage(chunkGenerator, l, worldgenRandom, i, j);
	}

	private boolean isNearVillage(ChunkGenerator chunkGenerator, long l, WorldgenRandom worldgenRandom, int i, int j) {
		StructureFeatureConfiguration structureFeatureConfiguration = chunkGenerator.getSettings().getConfig(StructureFeature.VILLAGE);
		if (structureFeatureConfiguration == null) {
			return false;
		} else {
			for (int k = i - 10; k <= i + 10; k++) {
				for (int m = j - 10; m <= j + 10; m++) {
					ChunkPos chunkPos = StructureFeature.VILLAGE.getPotentialFeatureChunk(structureFeatureConfiguration, l, worldgenRandom, k, m);
					if (k == chunkPos.x && m == chunkPos.z) {
						return true;
					}
				}
			}

			return false;
		}
	}
}
