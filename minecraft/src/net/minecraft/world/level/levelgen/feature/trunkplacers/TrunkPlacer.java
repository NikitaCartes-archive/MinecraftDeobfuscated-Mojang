package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.mojang.datafixers.Products.P3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public abstract class TrunkPlacer {
	public static final Codec<TrunkPlacer> CODEC = BuiltInRegistries.TRUNK_PLACER_TYPE.byNameCodec().dispatch(TrunkPlacer::type, TrunkPlacerType::codec);
	private static final int MAX_BASE_HEIGHT = 32;
	private static final int MAX_RAND = 24;
	public static final int MAX_HEIGHT = 80;
	protected final int baseHeight;
	protected final int heightRandA;
	protected final int heightRandB;

	protected static <P extends TrunkPlacer> P3<Mu<P>, Integer, Integer, Integer> trunkPlacerParts(Instance<P> instance) {
		return instance.group(
			Codec.intRange(0, 32).fieldOf("base_height").forGetter(trunkPlacer -> trunkPlacer.baseHeight),
			Codec.intRange(0, 24).fieldOf("height_rand_a").forGetter(trunkPlacer -> trunkPlacer.heightRandA),
			Codec.intRange(0, 24).fieldOf("height_rand_b").forGetter(trunkPlacer -> trunkPlacer.heightRandB)
		);
	}

	public TrunkPlacer(int i, int j, int k) {
		this.baseHeight = i;
		this.heightRandA = j;
		this.heightRandB = k;
	}

	protected abstract TrunkPlacerType<?> type();

	public abstract List<FoliagePlacer.FoliageAttachment> placeTrunk(
		LevelSimulatedReader levelSimulatedReader,
		BiConsumer<BlockPos, BlockState> biConsumer,
		RandomSource randomSource,
		int i,
		BlockPos blockPos,
		TreeConfiguration treeConfiguration
	);

	public int getTreeHeight(RandomSource randomSource) {
		return this.baseHeight + randomSource.nextInt(this.heightRandA + 1) + randomSource.nextInt(this.heightRandB + 1);
	}

	private static boolean isDirt(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return levelSimulatedReader.isStateAtPosition(
			blockPos, blockState -> Feature.isDirt(blockState) && !blockState.is(Blocks.GRASS_BLOCK) && !blockState.is(Blocks.MYCELIUM)
		);
	}

	protected static void setDirtAt(
		LevelSimulatedReader levelSimulatedReader,
		BiConsumer<BlockPos, BlockState> biConsumer,
		RandomSource randomSource,
		BlockPos blockPos,
		TreeConfiguration treeConfiguration
	) {
		if (treeConfiguration.forceDirt || !isDirt(levelSimulatedReader, blockPos)) {
			biConsumer.accept(blockPos, treeConfiguration.dirtProvider.getState(randomSource, blockPos));
		}
	}

	protected boolean placeLog(
		LevelSimulatedReader levelSimulatedReader,
		BiConsumer<BlockPos, BlockState> biConsumer,
		RandomSource randomSource,
		BlockPos blockPos,
		TreeConfiguration treeConfiguration
	) {
		return this.placeLog(levelSimulatedReader, biConsumer, randomSource, blockPos, treeConfiguration, Function.identity());
	}

	protected boolean placeLog(
		LevelSimulatedReader levelSimulatedReader,
		BiConsumer<BlockPos, BlockState> biConsumer,
		RandomSource randomSource,
		BlockPos blockPos,
		TreeConfiguration treeConfiguration,
		Function<BlockState, BlockState> function
	) {
		if (this.validTreePos(levelSimulatedReader, blockPos)) {
			biConsumer.accept(blockPos, (BlockState)function.apply(treeConfiguration.trunkProvider.getState(randomSource, blockPos)));
			return true;
		} else {
			return false;
		}
	}

	protected void placeLogIfFree(
		LevelSimulatedReader levelSimulatedReader,
		BiConsumer<BlockPos, BlockState> biConsumer,
		RandomSource randomSource,
		BlockPos.MutableBlockPos mutableBlockPos,
		TreeConfiguration treeConfiguration
	) {
		if (this.isFree(levelSimulatedReader, mutableBlockPos)) {
			this.placeLog(levelSimulatedReader, biConsumer, randomSource, mutableBlockPos, treeConfiguration);
		}
	}

	protected boolean validTreePos(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return TreeFeature.validTreePos(levelSimulatedReader, blockPos);
	}

	public boolean isFree(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return this.validTreePos(levelSimulatedReader, blockPos) || levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.is(BlockTags.LOGS));
	}
}
