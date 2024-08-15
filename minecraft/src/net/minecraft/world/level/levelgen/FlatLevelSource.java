package net.minecraft.world.level.levelgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public class FlatLevelSource extends ChunkGenerator {
	public static final MapCodec<FlatLevelSource> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(FlatLevelGeneratorSettings.CODEC.fieldOf("settings").forGetter(FlatLevelSource::settings))
				.apply(instance, instance.stable(FlatLevelSource::new))
	);
	private final FlatLevelGeneratorSettings settings;

	public FlatLevelSource(FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
		super(new FixedBiomeSource(flatLevelGeneratorSettings.getBiome()), Util.memoize(flatLevelGeneratorSettings::adjustGenerationSettings));
		this.settings = flatLevelGeneratorSettings;
	}

	@Override
	public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> holderLookup, RandomState randomState, long l) {
		Stream<Holder<StructureSet>> stream = (Stream<Holder<StructureSet>>)this.settings
			.structureOverrides()
			.map(HolderSet::stream)
			.orElseGet(() -> holderLookup.listElements().map(reference -> reference));
		return ChunkGeneratorStructureState.createForFlat(randomState, l, this.biomeSource, stream);
	}

	@Override
	protected MapCodec<? extends ChunkGenerator> codec() {
		return CODEC;
	}

	public FlatLevelGeneratorSettings settings() {
		return this.settings;
	}

	@Override
	public void buildSurface(WorldGenRegion worldGenRegion, StructureManager structureManager, RandomState randomState, ChunkAccess chunkAccess) {
	}

	@Override
	public int getSpawnHeight(LevelHeightAccessor levelHeightAccessor) {
		return levelHeightAccessor.getMinBuildHeight() + Math.min(levelHeightAccessor.getHeight(), this.settings.getLayers().size());
	}

	@Override
	public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess) {
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
	public int getBaseHeight(int i, int j, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
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
	public NoiseColumn getBaseColumn(int i, int j, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
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
	public void addDebugScreenInfo(List<String> list, RandomState randomState, BlockPos blockPos) {
	}

	@Override
	public void applyCarvers(
		WorldGenRegion worldGenRegion, long l, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunkAccess
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
