package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

public class FlatLevelSource extends ChunkGenerator {
	public static final Codec<FlatLevelSource> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(FlatLevelGeneratorSettings.CODEC.fieldOf("settings").forGetter(FlatLevelSource::settings))
				.apply(instance, instance.stable(FlatLevelSource::new))
	);
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

	@Override
	public ChunkGenerator withSeed(long l) {
		return this;
	}

	public FlatLevelGeneratorSettings settings() {
		return this.settings;
	}

	@Override
	public void buildSurface(WorldGenRegion worldGenRegion, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
	}

	@Override
	public int getSpawnHeight(LevelHeightAccessor levelHeightAccessor) {
		return levelHeightAccessor.getMinBuildHeight() + Math.min(levelHeightAccessor.getHeight(), this.settings.getLayers().size());
	}

	@Override
	protected Holder<Biome> adjustBiome(Holder<Biome> holder) {
		return this.settings.getBiome();
	}

	@Override
	public CompletableFuture<ChunkAccess> fillFromNoise(
		Executor executor, Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess
	) {
		List<BlockState> list = this.settings.getLayers();
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		Heightmap heightmap = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
		Heightmap heightmap2 = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

		for (int i = 0; i < Math.min(chunkAccess.getHeight(), list.size()); i++) {
			BlockState blockState = (BlockState)list.get(i);
			if (blockState != null) {
				int j = chunkAccess.getMinBuildHeight() + i;

				for (int k = 0; k < 16; k++) {
					for (int l = 0; l < 16; l++) {
						chunkAccess.setBlockState(mutableBlockPos.set(k, j, l), blockState, false);
						heightmap.update(k, j, l, blockState);
						heightmap2.update(k, j, l, blockState);
					}
				}
			}
		}

		return CompletableFuture.completedFuture(chunkAccess);
	}

	@Override
	public int getBaseHeight(int i, int j, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor) {
		List<BlockState> list = this.settings.getLayers();

		for (int k = Math.min(list.size(), levelHeightAccessor.getMaxBuildHeight()) - 1; k >= 0; k--) {
			BlockState blockState = (BlockState)list.get(k);
			if (blockState != null && types.isOpaque().test(blockState)) {
				return levelHeightAccessor.getMinBuildHeight() + k + 1;
			}
		}

		return levelHeightAccessor.getMinBuildHeight();
	}

	@Override
	public NoiseColumn getBaseColumn(int i, int j, LevelHeightAccessor levelHeightAccessor) {
		return new NoiseColumn(
			levelHeightAccessor.getMinBuildHeight(),
			(BlockState[])this.settings
				.getLayers()
				.stream()
				.limit((long)levelHeightAccessor.getHeight())
				.map(blockState -> blockState == null ? Blocks.AIR.defaultBlockState() : blockState)
				.toArray(BlockState[]::new)
		);
	}

	@Override
	public Climate.Sampler climateSampler() {
		return (i, j, k) -> Climate.target(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
	}

	@Override
	public void applyCarvers(
		WorldGenRegion worldGenRegion,
		long l,
		BiomeManager biomeManager,
		StructureFeatureManager structureFeatureManager,
		ChunkAccess chunkAccess,
		GenerationStep.Carving carving
	) {
	}

	@Override
	public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
	}

	@Override
	public int getMinY() {
		return 0;
	}

	@Override
	public int getGenDepth() {
		return 384;
	}

	@Override
	public int getSeaLevel() {
		return -63;
	}
}
