package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.mojang.datafixers.Products.P3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public abstract class TrunkPlacer {
	public static final Codec<TrunkPlacer> CODEC = Registry.TRUNK_PLACER_TYPES.dispatch(TrunkPlacer::type, TrunkPlacerType::codec);
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
		Random random,
		int i,
		BlockPos blockPos,
		TreeConfiguration treeConfiguration
	);

	public int getTreeHeight(Random random) {
		return this.baseHeight + random.nextInt(this.heightRandA + 1) + random.nextInt(this.heightRandB + 1);
	}

	private static boolean isDirt(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return levelSimulatedReader.isStateAtPosition(
			blockPos, blockState -> Feature.isDirt(blockState) && !blockState.is(Blocks.GRASS_BLOCK) && !blockState.is(Blocks.MYCELIUM)
		);
	}

	protected static void setDirtAt(
		LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, Random random, BlockPos blockPos, TreeConfiguration treeConfiguration
	) {
		if (treeConfiguration.forceDirt || !isDirt(levelSimulatedReader, blockPos)) {
			biConsumer.accept(blockPos, treeConfiguration.dirtProvider.getState(random, blockPos));
		}
	}

	protected static boolean placeLog(
		LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, Random random, BlockPos blockPos, TreeConfiguration treeConfiguration
	) {
		return placeLog(levelSimulatedReader, biConsumer, random, blockPos, treeConfiguration, Function.identity());
	}

	protected static boolean placeLog(
		LevelSimulatedReader levelSimulatedReader,
		BiConsumer<BlockPos, BlockState> biConsumer,
		Random random,
		BlockPos blockPos,
		TreeConfiguration treeConfiguration,
		Function<BlockState, BlockState> function
	) {
		if (TreeFeature.validTreePos(levelSimulatedReader, blockPos)) {
			biConsumer.accept(blockPos, (BlockState)function.apply(treeConfiguration.trunkProvider.getState(random, blockPos)));
			return true;
		} else {
			return false;
		}
	}

	protected static void placeLogIfFree(
		LevelSimulatedReader levelSimulatedReader,
		BiConsumer<BlockPos, BlockState> biConsumer,
		Random random,
		BlockPos.MutableBlockPos mutableBlockPos,
		TreeConfiguration treeConfiguration
	) {
		if (TreeFeature.isFree(levelSimulatedReader, mutableBlockPos)) {
			placeLog(levelSimulatedReader, biConsumer, random, mutableBlockPos, treeConfiguration);
		}
	}
}
