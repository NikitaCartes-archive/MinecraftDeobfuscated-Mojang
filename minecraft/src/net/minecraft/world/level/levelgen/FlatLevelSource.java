package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

public class FlatLevelSource extends ChunkGenerator {
	public static final Codec<FlatLevelSource> CODEC = FlatLevelGeneratorSettings.CODEC
		.fieldOf("settings")
		.<FlatLevelSource>xmap(FlatLevelSource::new, FlatLevelSource::settings)
		.codec();
	private final FlatLevelGeneratorSettings settings;

	public FlatLevelSource(FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
		super(
			new FixedBiomeSource(flatLevelGeneratorSettings.getBiomeFromSettings()),
			new FixedBiomeSource(flatLevelGeneratorSettings.getBiome()),
			flatLevelGeneratorSettings.structureSettings(),
			0L
		);
		this.settings = flatLevelGeneratorSettings;
	}

	@Override
	protected Codec<? extends ChunkGenerator> codec() {
		return CODEC;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public ChunkGenerator withSeed(long l) {
		return this;
	}

	public FlatLevelGeneratorSettings settings() {
		return this.settings;
	}

	@Override
	public void buildSurfaceAndBedrock(WorldGenRegion worldGenRegion, ChunkAccess chunkAccess) {
	}

	@Override
	public int getSpawnHeight() {
		BlockState[] blockStates = this.settings.getLayers();

		for (int i = 0; i < blockStates.length; i++) {
			BlockState blockState = blockStates[i] == null ? Blocks.AIR.defaultBlockState() : blockStates[i];
			if (!Heightmap.Types.MOTION_BLOCKING.isOpaque().test(blockState)) {
				return i - 1;
			}
		}

		return blockStates.length;
	}

	@Override
	public void fillFromNoise(LevelAccessor levelAccessor, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
		BlockState[] blockStates = this.settings.getLayers();
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		Heightmap heightmap = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
		Heightmap heightmap2 = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

		for (int i = 0; i < blockStates.length; i++) {
			BlockState blockState = blockStates[i];
			if (blockState != null) {
				for (int j = 0; j < 16; j++) {
					for (int k = 0; k < 16; k++) {
						chunkAccess.setBlockState(mutableBlockPos.set(j, i, k), blockState, false);
						heightmap.update(j, i, k, blockState);
						heightmap2.update(j, i, k, blockState);
					}
				}
			}
		}
	}

	@Override
	public int getBaseHeight(int i, int j, Heightmap.Types types) {
		BlockState[] blockStates = this.settings.getLayers();

		for (int k = blockStates.length - 1; k >= 0; k--) {
			BlockState blockState = blockStates[k];
			if (blockState != null && types.isOpaque().test(blockState)) {
				return k + 1;
			}
		}

		return 0;
	}

	@Override
	public BlockGetter getBaseColumn(int i, int j) {
		return new NoiseColumn(
			(BlockState[])Arrays.stream(this.settings.getLayers())
				.map(blockState -> blockState == null ? Blocks.AIR.defaultBlockState() : blockState)
				.toArray(BlockState[]::new)
		);
	}
}
