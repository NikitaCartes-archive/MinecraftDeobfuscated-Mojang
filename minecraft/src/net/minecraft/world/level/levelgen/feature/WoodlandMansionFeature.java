package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.WoodlandMansionPieces;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class WoodlandMansionFeature extends StructureFeature<NoneFeatureConfiguration> {
	public WoodlandMansionFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function);
	}

	@Override
	protected ChunkPos getPotentialFeatureChunkFromLocationWithOffset(ChunkGenerator<?> chunkGenerator, Random random, int i, int j, int k, int l) {
		int m = chunkGenerator.getSettings().getWoodlandMansionSpacing();
		int n = chunkGenerator.getSettings().getWoodlandMangionSeparation();
		int o = i + m * k;
		int p = j + m * l;
		int q = o < 0 ? o - m + 1 : o;
		int r = p < 0 ? p - m + 1 : p;
		int s = q / m;
		int t = r / m;
		((WorldgenRandom)random).setLargeFeatureWithSalt(chunkGenerator.getSeed(), s, t, 10387319);
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
			for (Biome biome2 : chunkGenerator.getBiomeSource().getBiomesWithin(i * 16 + 9, chunkGenerator.getSeaLevel(), j * 16 + 9, 32)) {
				if (!chunkGenerator.isBiomeValidStartForStructure(biome2, this)) {
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
		return WoodlandMansionFeature.WoodlandMansionStart::new;
	}

	@Override
	public String getFeatureName() {
		return "Mansion";
	}

	@Override
	public int getLookupRange() {
		return 8;
	}

	public static class WoodlandMansionStart extends StructureStart {
		public WoodlandMansionStart(StructureFeature<?> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
			super(structureFeature, i, j, boundingBox, k, l);
		}

		@Override
		public void generatePieces(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, int i, int j, Biome biome) {
			Rotation rotation = Rotation.values()[this.random.nextInt(Rotation.values().length)];
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
			int s = Math.min(Math.min(o, p), Math.min(q, r));
			if (s >= 60) {
				BlockPos blockPos = new BlockPos(i * 16 + 8, s + 1, j * 16 + 8);
				List<WoodlandMansionPieces.WoodlandMansionPiece> list = Lists.<WoodlandMansionPieces.WoodlandMansionPiece>newLinkedList();
				WoodlandMansionPieces.generateMansion(structureManager, blockPos, rotation, list, this.random);
				this.pieces.addAll(list);
				this.calculateBoundingBox();
			}
		}

		@Override
		public void postProcess(LevelAccessor levelAccessor, ChunkGenerator<?> chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos) {
			super.postProcess(levelAccessor, chunkGenerator, random, boundingBox, chunkPos);
			int i = this.boundingBox.y0;

			for (int j = boundingBox.x0; j <= boundingBox.x1; j++) {
				for (int k = boundingBox.z0; k <= boundingBox.z1; k++) {
					BlockPos blockPos = new BlockPos(j, i, k);
					if (!levelAccessor.isEmptyBlock(blockPos) && this.boundingBox.isInside(blockPos)) {
						boolean bl = false;

						for (StructurePiece structurePiece : this.pieces) {
							if (structurePiece.getBoundingBox().isInside(blockPos)) {
								bl = true;
								break;
							}
						}

						if (bl) {
							for (int l = i - 1; l > 1; l--) {
								BlockPos blockPos2 = new BlockPos(j, l, k);
								if (!levelAccessor.isEmptyBlock(blockPos2) && !levelAccessor.getBlockState(blockPos2).getMaterial().isLiquid()) {
									break;
								}

								levelAccessor.setBlock(blockPos2, Blocks.COBBLESTONE.defaultBlockState(), 2);
							}
						}
					}
				}
			}
		}
	}
}
