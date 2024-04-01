package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class UpwardsBranchingTrunkPlacer extends TrunkPlacer {
	public static final Codec<UpwardsBranchingTrunkPlacer> CODEC = RecordCodecBuilder.create(
		instance -> trunkPlacerParts(instance)
				.<IntProvider, float, IntProvider, HolderSet<Block>, boolean>and(
					instance.group(
						IntProvider.POSITIVE_CODEC.fieldOf("extra_branch_steps").forGetter(upwardsBranchingTrunkPlacer -> upwardsBranchingTrunkPlacer.extraBranchSteps),
						Codec.floatRange(0.0F, 1.0F)
							.fieldOf("place_branch_per_log_probability")
							.forGetter(upwardsBranchingTrunkPlacer -> upwardsBranchingTrunkPlacer.placeBranchPerLogProbability),
						IntProvider.NON_NEGATIVE_CODEC.fieldOf("extra_branch_length").forGetter(upwardsBranchingTrunkPlacer -> upwardsBranchingTrunkPlacer.extraBranchLength),
						RegistryCodecs.homogeneousList(Registries.BLOCK)
							.fieldOf("can_grow_through")
							.forGetter(upwardsBranchingTrunkPlacer -> upwardsBranchingTrunkPlacer.canGrowThrough),
						Codec.BOOL.fieldOf("megabush").forGetter(upwardsBranchingTrunkPlacer -> upwardsBranchingTrunkPlacer.megabush)
					)
				)
				.apply(instance, UpwardsBranchingTrunkPlacer::new)
	);
	private final IntProvider extraBranchSteps;
	private final float placeBranchPerLogProbability;
	private final IntProvider extraBranchLength;
	private final HolderSet<Block> canGrowThrough;
	private final boolean megabush;

	public UpwardsBranchingTrunkPlacer(int i, int j, int k, IntProvider intProvider, float f, IntProvider intProvider2, HolderSet<Block> holderSet, boolean bl) {
		super(i, j, k);
		this.extraBranchSteps = intProvider;
		this.placeBranchPerLogProbability = f;
		this.extraBranchLength = intProvider2;
		this.canGrowThrough = holderSet;
		this.megabush = bl;
	}

	@Override
	protected TrunkPlacerType<?> type() {
		return TrunkPlacerType.UPWARDS_BRANCHING_TRUNK_PLACER;
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
		List<FoliagePlacer.FoliageAttachment> list = Lists.<FoliagePlacer.FoliageAttachment>newArrayList();
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int j = 0; j < i; j++) {
			int k = blockPos.getY() + j;
			if (this.placeLog(levelSimulatedReader, biConsumer, randomSource, mutableBlockPos.set(blockPos.getX(), k, blockPos.getZ()), treeConfiguration)
				&& j < i - 1
				&& randomSource.nextFloat() < this.placeBranchPerLogProbability) {
				Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(randomSource);
				int l = this.extraBranchLength.sample(randomSource);
				int m = Math.max(0, l - this.extraBranchLength.sample(randomSource) - 1);
				int n = this.extraBranchSteps.sample(randomSource);
				this.placeBranch(levelSimulatedReader, biConsumer, randomSource, i, treeConfiguration, list, mutableBlockPos, k, direction, m, n);
			}

			if (j == i - 1) {
				list.add(new FoliagePlacer.FoliageAttachment(mutableBlockPos.set(blockPos.getX(), k + 1, blockPos.getZ()), 0, false));
			}
		}

		return list;
	}

	private void placeBranch(
		LevelSimulatedReader levelSimulatedReader,
		BiConsumer<BlockPos, BlockState> biConsumer,
		RandomSource randomSource,
		int i,
		TreeConfiguration treeConfiguration,
		List<FoliagePlacer.FoliageAttachment> list,
		BlockPos.MutableBlockPos mutableBlockPos,
		int j,
		Direction direction,
		int k,
		int l
	) {
		int m = j + k;
		int n = mutableBlockPos.getX();
		int o = mutableBlockPos.getZ();
		int p = k;

		while (p < i && l > 0) {
			if (p >= 1) {
				int q = j + p;
				n += direction.getStepX();
				o += direction.getStepZ();
				m = q;
				if (this.placeLog(levelSimulatedReader, biConsumer, randomSource, mutableBlockPos.set(n, q, o), treeConfiguration)) {
					m = q + 1;
				}

				if (this.megabush) {
					list.add(new FoliagePlacer.FoliageAttachment(mutableBlockPos.immutable(), 0, false));
				}
			}

			p++;
			l--;
		}

		if (m - j > 1) {
			BlockPos blockPos = new BlockPos(n, m, o);
			list.add(new FoliagePlacer.FoliageAttachment(blockPos, 0, false));
			if (this.megabush) {
				list.add(new FoliagePlacer.FoliageAttachment(blockPos.below(2), 0, false));
			}
		}
	}

	@Override
	protected boolean validTreePos(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return super.validTreePos(levelSimulatedReader, blockPos)
			|| levelSimulatedReader.isStateAtPosition(blockPos, blockState -> blockState.is(this.canGrowThrough));
	}
}
