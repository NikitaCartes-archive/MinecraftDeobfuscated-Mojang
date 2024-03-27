package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class CherryTrunkPlacer extends TrunkPlacer {
	private static final Codec<UniformInt> BRANCH_START_CODEC = UniformInt.CODEC
		.codec()
		.validate(
			uniformInt -> uniformInt.getMaxValue() - uniformInt.getMinValue() < 1
					? DataResult.error(() -> "Need at least 2 blocks variation for the branch starts to fit both branches")
					: DataResult.success(uniformInt)
		);
	public static final MapCodec<CherryTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(
		instance -> trunkPlacerParts(instance)
				.<IntProvider, IntProvider, UniformInt, IntProvider>and(
					instance.group(
						IntProvider.codec(1, 3).fieldOf("branch_count").forGetter(cherryTrunkPlacer -> cherryTrunkPlacer.branchCount),
						IntProvider.codec(2, 16).fieldOf("branch_horizontal_length").forGetter(cherryTrunkPlacer -> cherryTrunkPlacer.branchHorizontalLength),
						IntProvider.validateCodec(-16, 0, BRANCH_START_CODEC)
							.fieldOf("branch_start_offset_from_top")
							.forGetter(cherryTrunkPlacer -> cherryTrunkPlacer.branchStartOffsetFromTop),
						IntProvider.codec(-16, 16).fieldOf("branch_end_offset_from_top").forGetter(cherryTrunkPlacer -> cherryTrunkPlacer.branchEndOffsetFromTop)
					)
				)
				.apply(instance, CherryTrunkPlacer::new)
	);
	private final IntProvider branchCount;
	private final IntProvider branchHorizontalLength;
	private final UniformInt branchStartOffsetFromTop;
	private final UniformInt secondBranchStartOffsetFromTop;
	private final IntProvider branchEndOffsetFromTop;

	public CherryTrunkPlacer(int i, int j, int k, IntProvider intProvider, IntProvider intProvider2, UniformInt uniformInt, IntProvider intProvider3) {
		super(i, j, k);
		this.branchCount = intProvider;
		this.branchHorizontalLength = intProvider2;
		this.branchStartOffsetFromTop = uniformInt;
		this.secondBranchStartOffsetFromTop = UniformInt.of(uniformInt.getMinValue(), uniformInt.getMaxValue() - 1);
		this.branchEndOffsetFromTop = intProvider3;
	}

	@Override
	protected TrunkPlacerType<?> type() {
		return TrunkPlacerType.CHERRY_TRUNK_PLACER;
	}

	@Override
	public List<FoliagePlacer.FoliageAttachment> placeTrunk(
		LevelSimulatedReader levelSimulatedReader,
		BiConsumer<BlockPos, BlockState> biConsumer,
		RandomSource randomSource,
		int i,
		BlockPos blockPos,
		TreeConfiguration treeConfiguration
	) {
		setDirtAt(levelSimulatedReader, biConsumer, randomSource, blockPos.below(), treeConfiguration);
		int j = Math.max(0, i - 1 + this.branchStartOffsetFromTop.sample(randomSource));
		int k = Math.max(0, i - 1 + this.secondBranchStartOffsetFromTop.sample(randomSource));
		if (k >= j) {
			k++;
		}

		int l = this.branchCount.sample(randomSource);
		boolean bl = l == 3;
		boolean bl2 = l >= 2;
		int m;
		if (bl) {
			m = i;
		} else if (bl2) {
			m = Math.max(j, k) + 1;
		} else {
			m = j + 1;
		}

		for (int n = 0; n < m; n++) {
			this.placeLog(levelSimulatedReader, biConsumer, randomSource, blockPos.above(n), treeConfiguration);
		}

		List<FoliagePlacer.FoliageAttachment> list = new ArrayList();
		if (bl) {
			list.add(new FoliagePlacer.FoliageAttachment(blockPos.above(m), 0, false));
		}

		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(randomSource);
		Function<BlockState, BlockState> function = blockState -> blockState.trySetValue(RotatedPillarBlock.AXIS, direction.getAxis());
		list.add(
			this.generateBranch(levelSimulatedReader, biConsumer, randomSource, i, blockPos, treeConfiguration, function, direction, j, j < m - 1, mutableBlockPos)
		);
		if (bl2) {
			list.add(
				this.generateBranch(
					levelSimulatedReader, biConsumer, randomSource, i, blockPos, treeConfiguration, function, direction.getOpposite(), k, k < m - 1, mutableBlockPos
				)
			);
		}

		return list;
	}

	private FoliagePlacer.FoliageAttachment generateBranch(
		LevelSimulatedReader levelSimulatedReader,
		BiConsumer<BlockPos, BlockState> biConsumer,
		RandomSource randomSource,
		int i,
		BlockPos blockPos,
		TreeConfiguration treeConfiguration,
		Function<BlockState, BlockState> function,
		Direction direction,
		int j,
		boolean bl,
		BlockPos.MutableBlockPos mutableBlockPos
	) {
		mutableBlockPos.set(blockPos).move(Direction.UP, j);
		int k = i - 1 + this.branchEndOffsetFromTop.sample(randomSource);
		boolean bl2 = bl || k < j;
		int l = this.branchHorizontalLength.sample(randomSource) + (bl2 ? 1 : 0);
		BlockPos blockPos2 = blockPos.relative(direction, l).above(k);
		int m = bl2 ? 2 : 1;

		for (int n = 0; n < m; n++) {
			this.placeLog(levelSimulatedReader, biConsumer, randomSource, mutableBlockPos.move(direction), treeConfiguration, function);
		}

		Direction direction2 = blockPos2.getY() > mutableBlockPos.getY() ? Direction.UP : Direction.DOWN;

		while (true) {
			int o = mutableBlockPos.distManhattan(blockPos2);
			if (o == 0) {
				return new FoliagePlacer.FoliageAttachment(blockPos2.above(), 0, false);
			}

			float f = (float)Math.abs(blockPos2.getY() - mutableBlockPos.getY()) / (float)o;
			boolean bl3 = randomSource.nextFloat() < f;
			mutableBlockPos.move(bl3 ? direction2 : direction);
			this.placeLog(levelSimulatedReader, biConsumer, randomSource, mutableBlockPos, treeConfiguration, bl3 ? Function.identity() : function);
		}
	}
}
