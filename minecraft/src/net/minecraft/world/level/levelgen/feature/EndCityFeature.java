package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.EndCityPieces;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class EndCityFeature extends StructureFeature<NoneFeatureConfiguration> {
	public EndCityFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function);
	}

	@Override
	protected ChunkPos getPotentialFeatureChunkFromLocationWithOffset(ChunkGenerator<?> chunkGenerator, Random random, int i, int j, int k, int l) {
		int m = chunkGenerator.getSettings().getEndCitySpacing();
		int n = chunkGenerator.getSettings().getEndCitySeparation();
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
		if (i != chunkPos.x || j != chunkPos.z) {
			return false;
		} else if (!chunkGenerator.isBiomeValidStartForStructure(biome, this)) {
			return false;
		} else {
			int k = getYPositionForFeature(i, j, chunkGenerator);
			return k >= 60;
		}
	}

	@Override
	public StructureFeature.StructureStartFactory getStartFactory() {
		return EndCityFeature.EndCityStart::new;
	}

	@Override
	public String getFeatureName() {
		return "EndCity";
	}

	@Override
	public int getLookupRange() {
		return 8;
	}

	private static int getYPositionForFeature(int i, int j, ChunkGenerator<?> chunkGenerator) {
		Random random = new Random((long)(i + j * 10387313));
		Rotation rotation = Rotation.values()[random.nextInt(Rotation.values().length)];
		int k = 5;
		int l = 5;
		if (rotation == Rotation.CLOCKWISE_90) {
			k = -5;
		} else if (rotation == Rotation.CLOCKWISE_180) {
			k = -5;
			l = -5;
		} else if (rotation == Rotation.COUNTERCLOCKWISE_90) {
			l = -5;
		}

		int m = (i << 4) + 7;
		int n = (j << 4) + 7;
		int o = chunkGenerator.getFirstOccupiedHeight(m, n, Heightmap.Types.WORLD_SURFACE_WG);
		int p = chunkGenerator.getFirstOccupiedHeight(m, n + l, Heightmap.Types.WORLD_SURFACE_WG);
		int q = chunkGenerator.getFirstOccupiedHeight(m + k, n, Heightmap.Types.WORLD_SURFACE_WG);
		int r = chunkGenerator.getFirstOccupiedHeight(m + k, n + l, Heightmap.Types.WORLD_SURFACE_WG);
		return Math.min(Math.min(o, p), Math.min(q, r));
	}

	public static class EndCityStart extends StructureStart {
		public EndCityStart(StructureFeature<?> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
			super(structureFeature, i, j, boundingBox, k, l);
		}

		@Override
		public void generatePieces(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, int i, int j, Biome biome) {
			Rotation rotation = Rotation.values()[this.random.nextInt(Rotation.values().length)];
			int k = EndCityFeature.getYPositionForFeature(i, j, chunkGenerator);
			if (k >= 60) {
				BlockPos blockPos = new BlockPos(i * 16 + 8, k, j * 16 + 8);
				EndCityPieces.startHouseTower(structureManager, blockPos, rotation, this.pieces, this.random);
				this.calculateBoundingBox();
			}
		}
	}
}
