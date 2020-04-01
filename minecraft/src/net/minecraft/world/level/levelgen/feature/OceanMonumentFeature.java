package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.OceanMonumentPieces;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class OceanMonumentFeature extends StructureFeature<NoneFeatureConfiguration> {
	private static final List<Biome.SpawnerData> MONUMENT_ENEMIES = Lists.<Biome.SpawnerData>newArrayList(new Biome.SpawnerData(EntityType.GUARDIAN, 1, 2, 4));

	public OceanMonumentFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function, Function<Random, ? extends NoneFeatureConfiguration> function2) {
		super(function, function2);
	}

	@Override
	protected ChunkPos getPotentialFeatureChunkFromLocationWithOffset(ChunkGenerator<?> chunkGenerator, Random random, int i, int j, int k, int l) {
		int m = chunkGenerator.getSettings().getMonumentsSpacing();
		int n = chunkGenerator.getSettings().getMonumentsSeparation();
		int o = i + m * k;
		int p = j + m * l;
		int q = o < 0 ? o - m + 1 : o;
		int r = p < 0 ? p - m + 1 : p;
		int s = q / m;
		int t = r / m;
		((WorldgenRandom)random).setLargeFeatureWithSalt(chunkGenerator.getSeed(), s, t, 10387313);
		s *= m;
		t *= m;
		s += (random.nextInt(m - n) + random.nextInt(m - n)) / 2;
		t += (random.nextInt(m - n) + random.nextInt(m - n)) / 2;
		return new ChunkPos(s, t);
	}

	@Override
	public boolean isFeatureChunk(BiomeManager biomeManager, ChunkGenerator<?> chunkGenerator, Random random, int i, int j, Biome biome) {
		ChunkPos chunkPos = this.getPotentialFeatureChunkFromLocationWithOffset(chunkGenerator, random, i, j, 0, 0);
		if (i == chunkPos.x && j == chunkPos.z) {
			for (Biome biome2 : chunkGenerator.getBiomeSource().getBiomesWithin(i * 16 + 9, chunkGenerator.getSeaLevel(), j * 16 + 9, 16)) {
				if (!chunkGenerator.isBiomeValidStartForStructure(biome2, this)) {
					return false;
				}
			}

			for (Biome biome3 : chunkGenerator.getBiomeSource().getBiomesWithin(i * 16 + 9, chunkGenerator.getSeaLevel(), j * 16 + 9, 29)) {
				if (biome3.getBiomeCategory() != Biome.BiomeCategory.OCEAN && biome3.getBiomeCategory() != Biome.BiomeCategory.RIVER) {
					return false;
				}
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public StructureFeature.StructureStartFactory getStartFactory() {
		return OceanMonumentFeature.OceanMonumentStart::new;
	}

	@Override
	public String getFeatureName() {
		return "Monument";
	}

	@Override
	public int getLookupRange() {
		return 8;
	}

	@Override
	public List<Biome.SpawnerData> getSpecialEnemies() {
		return MONUMENT_ENEMIES;
	}

	public static class OceanMonumentStart extends StructureStart {
		private boolean isCreated;

		public OceanMonumentStart(StructureFeature<?> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
			super(structureFeature, i, j, boundingBox, k, l);
		}

		@Override
		public void generatePieces(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, int i, int j, Biome biome) {
			this.generatePieces(i, j);
		}

		private void generatePieces(int i, int j) {
			int k = i * 16 - 29;
			int l = j * 16 - 29;
			Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(this.random);
			this.pieces.add(new OceanMonumentPieces.MonumentBuilding(this.random, k, l, direction));
			this.calculateBoundingBox();
			this.isCreated = true;
		}

		@Override
		public void postProcess(LevelAccessor levelAccessor, ChunkGenerator<?> chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos) {
			if (!this.isCreated) {
				this.pieces.clear();
				this.generatePieces(this.getChunkX(), this.getChunkZ());
			}

			super.postProcess(levelAccessor, chunkGenerator, random, boundingBox, chunkPos);
		}
	}
}
