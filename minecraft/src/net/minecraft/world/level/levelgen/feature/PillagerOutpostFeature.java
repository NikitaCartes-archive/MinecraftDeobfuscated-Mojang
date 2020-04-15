package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BeardedStructureStart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PillagerOutpostPieces;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class PillagerOutpostFeature extends RandomScatteredFeature<NoneFeatureConfiguration> {
	private static final List<Biome.SpawnerData> OUTPOST_ENEMIES = Lists.<Biome.SpawnerData>newArrayList(new Biome.SpawnerData(EntityType.PILLAGER, 1, 1, 1));

	public PillagerOutpostFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function);
	}

	@Override
	public String getFeatureName() {
		return "Pillager_Outpost";
	}

	@Override
	public int getLookupRange() {
		return 3;
	}

	@Override
	public List<Biome.SpawnerData> getSpecialEnemies() {
		return OUTPOST_ENEMIES;
	}

	@Override
	protected boolean isFeatureChunk(
		BiomeManager biomeManager, ChunkGenerator<?> chunkGenerator, WorldgenRandom worldgenRandom, int i, int j, Biome biome, ChunkPos chunkPos
	) {
		int k = i >> 4;
		int l = j >> 4;
		worldgenRandom.setSeed((long)(k ^ l << 4) ^ chunkGenerator.getSeed());
		worldgenRandom.nextInt();
		if (worldgenRandom.nextInt(5) != 0) {
			return false;
		} else {
			for (int m = i - 10; m <= i + 10; m++) {
				for (int n = j - 10; n <= j + 10; n++) {
					Biome biome2 = biomeManager.getBiome(new BlockPos((m << 4) + 9, 0, (n << 4) + 9));
					if (Feature.VILLAGE.featureChunk(biomeManager, chunkGenerator, worldgenRandom, m, n, biome2)) {
						return false;
					}
				}
			}

			return true;
		}
	}

	@Override
	public StructureFeature.StructureStartFactory getStartFactory() {
		return PillagerOutpostFeature.FeatureStart::new;
	}

	@Override
	protected int getRandomSalt(ChunkGeneratorSettings chunkGeneratorSettings) {
		return 165745296;
	}

	public static class FeatureStart extends BeardedStructureStart {
		public FeatureStart(StructureFeature<?> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
			super(structureFeature, i, j, boundingBox, k, l);
		}

		@Override
		public void generatePieces(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, int i, int j, Biome biome) {
			BlockPos blockPos = new BlockPos(i * 16, 0, j * 16);
			PillagerOutpostPieces.addPieces(chunkGenerator, structureManager, blockPos, this.pieces, this.random);
			this.calculateBoundingBox();
		}
	}
}
