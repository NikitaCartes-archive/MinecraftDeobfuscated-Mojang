package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;

public class PillagerOutpostFeature extends JigsawFeature {
	private static final List<Biome.SpawnerData> OUTPOST_ENEMIES = Lists.<Biome.SpawnerData>newArrayList(new Biome.SpawnerData(EntityType.PILLAGER, 1, 1, 1));

	public PillagerOutpostFeature(Codec<JigsawConfiguration> codec) {
		super(codec, 0, true, true);
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
		JigsawConfiguration jigsawConfiguration
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
}
