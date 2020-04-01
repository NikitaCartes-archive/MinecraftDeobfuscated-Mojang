package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import it.unimi.dsi.fastutil.longs.LongIterator;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.FeatureAccess;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class StructureFeature<C extends FeatureConfiguration> extends Feature<C> {
	private static final Logger LOGGER = LogManager.getLogger();

	public StructureFeature(Function<Dynamic<?>, ? extends C> function, Function<Random, ? extends C> function2) {
		super(function, function2);
	}

	@Override
	public ConfiguredFeature<C, ? extends StructureFeature<C>> configured(C featureConfiguration) {
		return new ConfiguredFeature<>(this, featureConfiguration);
	}

	public ConfiguredFeature<C, ? extends StructureFeature<C>> random2(Random random) {
		return new ConfiguredFeature<>(this, (C)this.randomConfigurationFactory.apply(random));
	}

	@Override
	public boolean place(
		LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, BlockPos blockPos, C featureConfiguration
	) {
		if (!levelAccessor.getLevelData().isGenerateMapFeatures()) {
			return false;
		} else {
			int i = blockPos.getX() >> 4;
			int j = blockPos.getZ() >> 4;
			int k = i << 4;
			int l = j << 4;
			boolean bl = false;
			LongIterator var11 = levelAccessor.getChunk(i, j).getReferencesForFeature(this.getFeatureName()).iterator();

			while (var11.hasNext()) {
				Long long_ = (Long)var11.next();
				ChunkPos chunkPos = new ChunkPos(long_);
				StructureStart structureStart = levelAccessor.getChunk(chunkPos.x, chunkPos.z).getStartForFeature(this.getFeatureName());
				if (structureStart != null && structureStart != StructureStart.INVALID_START) {
					structureStart.postProcess(levelAccessor, chunkGenerator, random, new BoundingBox(k, l, k + 15, l + 15), new ChunkPos(i, j));
					bl = true;
				}
			}

			return bl;
		}
	}

	protected StructureStart getStructureAt(LevelAccessor levelAccessor, BlockPos blockPos, boolean bl) {
		for (StructureStart structureStart : this.dereferenceStructureStarts(levelAccessor, blockPos.getX() >> 4, blockPos.getZ() >> 4)) {
			if (structureStart.isValid() && structureStart.getBoundingBox().isInside(blockPos)) {
				if (!bl) {
					return structureStart;
				}

				for (StructurePiece structurePiece : structureStart.getPieces()) {
					if (structurePiece.getBoundingBox().isInside(blockPos)) {
						return structureStart;
					}
				}
			}
		}

		return StructureStart.INVALID_START;
	}

	public boolean isInsideBoundingFeature(LevelAccessor levelAccessor, BlockPos blockPos) {
		return this.getStructureAt(levelAccessor, blockPos, false).isValid();
	}

	public boolean isInsideFeature(LevelAccessor levelAccessor, BlockPos blockPos) {
		return this.getStructureAt(levelAccessor, blockPos, true).isValid();
	}

	@Nullable
	public BlockPos getNearestGeneratedFeature(Level level, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, BlockPos blockPos, int i, boolean bl) {
		if (!chunkGenerator.getBiomeSource().canGenerateStructure(this)) {
			return null;
		} else {
			int j = blockPos.getX() >> 4;
			int k = blockPos.getZ() >> 4;
			int l = 0;

			for (WorldgenRandom worldgenRandom = new WorldgenRandom(); l <= i; l++) {
				for (int m = -l; m <= l; m++) {
					boolean bl2 = m == -l || m == l;

					for (int n = -l; n <= l; n++) {
						boolean bl3 = n == -l || n == l;
						if (bl2 || bl3) {
							ChunkPos chunkPos = this.getPotentialFeatureChunkFromLocationWithOffset(chunkGenerator, worldgenRandom, j, k, m, n);
							StructureStart structureStart = level.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS).getStartForFeature(this.getFeatureName());
							if (structureStart != null && structureStart.isValid()) {
								if (bl && structureStart.canBeReferenced()) {
									structureStart.addReference();
									return structureStart.getLocatePos();
								}

								if (!bl) {
									return structureStart.getLocatePos();
								}
							}

							if (l == 0) {
								break;
							}
						}
					}

					if (l == 0) {
						break;
					}
				}
			}

			return null;
		}
	}

	private List<StructureStart> dereferenceStructureStarts(LevelAccessor levelAccessor, int i, int j) {
		List<StructureStart> list = Lists.<StructureStart>newArrayList();
		ChunkAccess chunkAccess = levelAccessor.getChunk(i, j, ChunkStatus.STRUCTURE_REFERENCES);
		LongIterator longIterator = chunkAccess.getReferencesForFeature(this.getFeatureName()).iterator();

		while (longIterator.hasNext()) {
			long l = longIterator.nextLong();
			FeatureAccess featureAccess = levelAccessor.getChunk(ChunkPos.getX(l), ChunkPos.getZ(l), ChunkStatus.STRUCTURE_STARTS);
			StructureStart structureStart = featureAccess.getStartForFeature(this.getFeatureName());
			if (structureStart != null) {
				list.add(structureStart);
			}
		}

		return list;
	}

	protected ChunkPos getPotentialFeatureChunkFromLocationWithOffset(ChunkGenerator<?> chunkGenerator, Random random, int i, int j, int k, int l) {
		return new ChunkPos(i + k, j + l);
	}

	public abstract boolean isFeatureChunk(BiomeManager biomeManager, ChunkGenerator<?> chunkGenerator, Random random, int i, int j, Biome biome);

	public abstract StructureFeature.StructureStartFactory getStartFactory();

	public abstract String getFeatureName();

	public abstract int getLookupRange();

	public interface StructureStartFactory {
		StructureStart create(StructureFeature<?> structureFeature, int i, int j, BoundingBox boundingBox, int k, long l);
	}
}
