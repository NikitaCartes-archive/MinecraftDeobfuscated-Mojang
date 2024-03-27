package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class ForkingTrunkPlacer extends TrunkPlacer {
	public static final MapCodec<ForkingTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(
		instance -> trunkPlacerParts(instance).apply(instance, ForkingTrunkPlacer::new)
	);

	public ForkingTrunkPlacer(int i, int j, int k) {
		super(i, j, k);
	}

	@Override
	protected TrunkPlacerType<?> type() {
		return TrunkPlacerType.FORKING_TRUNK_PLACER;
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
		List<FoliagePlacer.FoliageAttachment> list = Lists.<FoliagePlacer.FoliageAttachment>newArrayList();
		Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(randomSource);
		int j = i - randomSource.nextInt(4) - 1;
		int k = 3 - randomSource.nextInt(3);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		int l = blockPos.getX();
		int m = blockPos.getZ();
		OptionalInt optionalInt = OptionalInt.empty();

		for (int n = 0; n < i; n++) {
			int o = blockPos.getY() + n;
			if (n >= j && k > 0) {
				l += direction.getStepX();
				m += direction.getStepZ();
				k--;
			}

			if (this.placeLog(levelSimulatedReader, biConsumer, randomSource, mutableBlockPos.set(l, o, m), treeConfiguration)) {
				optionalInt = OptionalInt.of(o + 1);
			}
		}

		if (optionalInt.isPresent()) {
			list.add(new FoliagePlacer.FoliageAttachment(new BlockPos(l, optionalInt.getAsInt(), m), 1, false));
		}

		l = blockPos.getX();
		m = blockPos.getZ();
		Direction direction2 = Direction.Plane.HORIZONTAL.getRandomDirection(randomSource);
		if (direction2 != direction) {
			int ox = j - randomSource.nextInt(2) - 1;
			int p = 1 + randomSource.nextInt(3);
			optionalInt = OptionalInt.empty();

			for (int q = ox; q < i && p > 0; p--) {
				if (q >= 1) {
					int r = blockPos.getY() + q;
					l += direction2.getStepX();
					m += direction2.getStepZ();
					if (this.placeLog(levelSimulatedReader, biConsumer, randomSource, mutableBlockPos.set(l, r, m), treeConfiguration)) {
						optionalInt = OptionalInt.of(r + 1);
					}
				}

				q++;
			}

			if (optionalInt.isPresent()) {
				list.add(new FoliagePlacer.FoliageAttachment(new BlockPos(l, optionalInt.getAsInt(), m), 0, false));
			}
		}

		return list;
	}
}
