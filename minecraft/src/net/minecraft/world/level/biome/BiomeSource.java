package net.minecraft.world.level.biome;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.StructureFeature;

public abstract class BiomeSource {
	private static final List<Biome> PLAYER_SPAWN_BIOMES = Lists.<Biome>newArrayList(
		Biomes.FOREST, Biomes.PLAINS, Biomes.TAIGA, Biomes.TAIGA_HILLS, Biomes.WOODED_HILLS, Biomes.JUNGLE, Biomes.JUNGLE_HILLS
	);
	protected final Map<StructureFeature<?>, Boolean> supportedStructures = Maps.<StructureFeature<?>, Boolean>newHashMap();
	protected final Set<BlockState> surfaceBlocks = Sets.<BlockState>newHashSet();

	protected BiomeSource() {
	}

	public List<Biome> getPlayerSpawnBiomes() {
		return PLAYER_SPAWN_BIOMES;
	}

	public Biome getBiome(BlockPos blockPos) {
		return this.getBiome(blockPos.getX(), blockPos.getZ());
	}

	public abstract Biome getBiome(int i, int j);

	public Biome getNoiseBiome(int i, int j) {
		return this.getBiome(i << 2, j << 2);
	}

	public Biome[] getBiomeBlock(int i, int j, int k, int l) {
		return this.getBiomeBlock(i, j, k, l, true);
	}

	public abstract Biome[] getBiomeBlock(int i, int j, int k, int l, boolean bl);

	public abstract Set<Biome> getBiomesWithin(int i, int j, int k);

	@Nullable
	public abstract BlockPos findBiome(int i, int j, int k, List<Biome> list, Random random);

	public float getHeightValue(int i, int j) {
		return 0.0F;
	}

	public abstract boolean canGenerateStructure(StructureFeature<?> structureFeature);

	public abstract Set<BlockState> getSurfaceBlocks();
}
