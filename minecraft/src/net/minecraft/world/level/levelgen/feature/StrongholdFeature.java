package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StrongholdPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class StrongholdFeature extends StructureFeature<NoneFeatureConfiguration> {
	private boolean isSpotSelected;
	private ChunkPos[] strongholdPos;
	private final List<StructureStart> discoveredStarts = Lists.<StructureStart>newArrayList();
	private long currentSeed;

	public StrongholdFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> function) {
		super(function);
	}

	@Override
	public boolean featureChunk(BiomeManager biomeManager, ChunkGenerator<?> chunkGenerator, WorldgenRandom worldgenRandom, int i, int j, Biome biome) {
		ChunkPos chunkPos = this.getPotentialFeatureChunk(chunkGenerator, worldgenRandom, i, j);
		return this.isFeatureChunk(biomeManager, chunkGenerator, worldgenRandom, i, j, biome, chunkPos);
	}

	@Override
	protected boolean isFeatureChunk(
		BiomeManager biomeManager, ChunkGenerator<?> chunkGenerator, WorldgenRandom worldgenRandom, int i, int j, Biome biome, ChunkPos chunkPos
	) {
		if (this.currentSeed != chunkGenerator.getSeed()) {
			this.reset();
		}

		if (!this.isSpotSelected) {
			this.generatePositions(chunkGenerator);
			this.isSpotSelected = true;
		}

		for (ChunkPos chunkPos2 : this.strongholdPos) {
			if (i == chunkPos2.x && j == chunkPos2.z) {
				return true;
			}
		}

		return false;
	}

	private void reset() {
		this.isSpotSelected = false;
		this.strongholdPos = null;
		this.discoveredStarts.clear();
	}

	@Override
	public StructureFeature.StructureStartFactory getStartFactory() {
		return StrongholdFeature.StrongholdStart::new;
	}

	@Override
	public String getFeatureName() {
		return "Stronghold";
	}

	@Override
	public int getLookupRange() {
		return 8;
	}

	@Nullable
	@Override
	public BlockPos getNearestGeneratedFeature(
		ServerLevel serverLevel, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, BlockPos blockPos, int i, boolean bl
	) {
		if (!chunkGenerator.getBiomeSource().canGenerateStructure(this)) {
			return null;
		} else {
			if (this.currentSeed != serverLevel.getSeed()) {
				this.reset();
			}

			if (!this.isSpotSelected) {
				this.generatePositions(chunkGenerator);
				this.isSpotSelected = true;
			}

			BlockPos blockPos2 = null;
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
			double d = Double.MAX_VALUE;

			for (ChunkPos chunkPos : this.strongholdPos) {
				mutableBlockPos.set((chunkPos.x << 4) + 8, 32, (chunkPos.z << 4) + 8);
				double e = mutableBlockPos.distSqr(blockPos);
				if (blockPos2 == null) {
					blockPos2 = new BlockPos(mutableBlockPos);
					d = e;
				} else if (e < d) {
					blockPos2 = new BlockPos(mutableBlockPos);
					d = e;
				}
			}

			return blockPos2;
		}
	}

	private void generatePositions(ChunkGenerator<?> chunkGenerator) {
		this.currentSeed = chunkGenerator.getSeed();
		List<Biome> list = Lists.<Biome>newArrayList();

		for (Biome biome : Registry.BIOME) {
			if (biome != null && chunkGenerator.isBiomeValidStartForStructure(biome, this)) {
				list.add(biome);
			}
		}

		int i = chunkGenerator.getSettings().getStrongholdsDistance();
		int j = chunkGenerator.getSettings().getStrongholdsCount();
		int k = chunkGenerator.getSettings().getStrongholdsSpread();
		this.strongholdPos = new ChunkPos[j];
		int l = 0;

		for (StructureStart structureStart : this.discoveredStarts) {
			if (l < this.strongholdPos.length) {
				this.strongholdPos[l++] = new ChunkPos(structureStart.getChunkX(), structureStart.getChunkZ());
			}
		}

		Random random = new Random();
		random.setSeed(chunkGenerator.getSeed());
		double d = random.nextDouble() * Math.PI * 2.0;
		int m = l;
		if (l < this.strongholdPos.length) {
			int n = 0;
			int o = 0;

			for (int p = 0; p < this.strongholdPos.length; p++) {
				double e = (double)(4 * i + i * o * 6) + (random.nextDouble() - 0.5) * (double)i * 2.5;
				int q = (int)Math.round(Math.cos(d) * e);
				int r = (int)Math.round(Math.sin(d) * e);
				BlockPos blockPos = chunkGenerator.getBiomeSource().findBiomeHorizontal((q << 4) + 8, chunkGenerator.getSeaLevel(), (r << 4) + 8, 112, list, random);
				if (blockPos != null) {
					q = blockPos.getX() >> 4;
					r = blockPos.getZ() >> 4;
				}

				if (p >= m) {
					this.strongholdPos[p] = new ChunkPos(q, r);
				}

				d += (Math.PI * 2) / (double)k;
				if (++n == k) {
					o++;
					n = 0;
					k += 2 * k / (o + 1);
					k = Math.min(k, this.strongholdPos.length - p);
					d += random.nextDouble() * Math.PI * 2.0;
				}
			}
		}
	}

	public static class StrongholdStart extends StructureStart {
		public StrongholdStart(StructureFeature<?> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l) {
			super(structureFeature, i, j, boundingBox, k, l);
		}

		@Override
		public void generatePieces(ChunkGenerator<?> chunkGenerator, StructureManager structureManager, int i, int j, Biome biome) {
			int k = 0;
			long l = chunkGenerator.getSeed();

			StrongholdPieces.StartPiece startPiece;
			do {
				this.pieces.clear();
				this.boundingBox = BoundingBox.getUnknownBox();
				this.random.setLargeFeatureSeed(l + (long)(k++), i, j);
				StrongholdPieces.resetPieces();
				startPiece = new StrongholdPieces.StartPiece(this.random, (i << 4) + 2, (j << 4) + 2);
				this.pieces.add(startPiece);
				startPiece.addChildren(startPiece, this.pieces, this.random);
				List<StructurePiece> list = startPiece.pendingChildren;

				while (!list.isEmpty()) {
					int m = this.random.nextInt(list.size());
					StructurePiece structurePiece = (StructurePiece)list.remove(m);
					structurePiece.addChildren(startPiece, this.pieces, this.random);
				}

				this.calculateBoundingBox();
				this.moveBelowSeaLevel(chunkGenerator.getSeaLevel(), this.random, 10);
			} while (this.pieces.isEmpty() || startPiece.portalRoomPiece == null);

			((StrongholdFeature)this.getFeature()).discoveredStarts.add(this);
		}
	}
}
