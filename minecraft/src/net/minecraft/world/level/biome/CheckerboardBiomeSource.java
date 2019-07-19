package net.minecraft.world.level.biome;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.StructureFeature;

public class CheckerboardBiomeSource extends BiomeSource {
	private final Biome[] allowedBiomes;
	private final int bitShift;

	public CheckerboardBiomeSource(CheckerboardBiomeSourceSettings checkerboardBiomeSourceSettings) {
		this.allowedBiomes = checkerboardBiomeSourceSettings.getAllowedBiomes();
		this.bitShift = checkerboardBiomeSourceSettings.getSize() + 4;
	}

	@Override
	public Biome getBiome(int i, int j) {
		return this.allowedBiomes[Math.abs(((i >> this.bitShift) + (j >> this.bitShift)) % this.allowedBiomes.length)];
	}

	@Override
	public Biome[] getBiomeBlock(int i, int j, int k, int l, boolean bl) {
		Biome[] biomes = new Biome[k * l];

		for (int m = 0; m < l; m++) {
			for (int n = 0; n < k; n++) {
				int o = Math.abs(((i + m >> this.bitShift) + (j + n >> this.bitShift)) % this.allowedBiomes.length);
				Biome biome = this.allowedBiomes[o];
				biomes[m * k + n] = biome;
			}
		}

		return biomes;
	}

	@Nullable
	@Override
	public BlockPos findBiome(int i, int j, int k, List<Biome> list, Random random) {
		return null;
	}

	@Override
	public boolean canGenerateStructure(StructureFeature<?> structureFeature) {
		return (Boolean)this.supportedStructures.computeIfAbsent(structureFeature, structureFeaturex -> {
			for (Biome biome : this.allowedBiomes) {
				if (biome.isValidStart(structureFeaturex)) {
					return true;
				}
			}

			return false;
		});
	}

	@Override
	public Set<BlockState> getSurfaceBlocks() {
		if (this.surfaceBlocks.isEmpty()) {
			for (Biome biome : this.allowedBiomes) {
				this.surfaceBlocks.add(biome.getSurfaceBuilderConfig().getTopMaterial());
			}
		}

		return this.surfaceBlocks;
	}

	@Override
	public Set<Biome> getBiomesWithin(int i, int j, int k) {
		return Sets.<Biome>newHashSet(this.allowedBiomes);
	}
}
