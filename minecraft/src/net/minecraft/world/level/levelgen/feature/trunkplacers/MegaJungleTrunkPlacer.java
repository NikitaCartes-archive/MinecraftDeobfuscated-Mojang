package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class MegaJungleTrunkPlacer extends GiantTrunkPlacer {
	public static final MapCodec<MegaJungleTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(
		instance -> trunkPlacerParts(instance).apply(instance, MegaJungleTrunkPlacer::new)
	);

	public MegaJungleTrunkPlacer(int i, int j, int k) {
		super(i, j, k);
	}

	@Override
	protected TrunkPlacerType<?> type() {
		return TrunkPlacerType.MEGA_JUNGLE_TRUNK_PLACER;
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
		list.addAll(super.placeTrunk(levelSimulatedReader, biConsumer, randomSource, i, blockPos, treeConfiguration));

		for (int j = i - 2 - randomSource.nextInt(4); j > i / 2; j -= 2 + randomSource.nextInt(4)) {
			float f = randomSource.nextFloat() * (float) (Math.PI * 2);
			int k = 0;
			int l = 0;

			for (int m = 0; m < 5; m++) {
				k = (int)(1.5F + Mth.cos(f) * (float)m);
				l = (int)(1.5F + Mth.sin(f) * (float)m);
				BlockPos blockPos2 = blockPos.offset(k, j - 3 + m / 2, l);
				this.placeLog(levelSimulatedReader, biConsumer, randomSource, blockPos2, treeConfiguration);
			}

			list.add(new FoliagePlacer.FoliageAttachment(blockPos.offset(k, j, l), -2, false));
		}

		return list;
	}
}
