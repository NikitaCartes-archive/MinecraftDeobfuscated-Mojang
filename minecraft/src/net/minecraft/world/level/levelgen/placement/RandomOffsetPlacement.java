package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;

public class RandomOffsetPlacement extends PlacementModifier {
	public static final Codec<RandomOffsetPlacement> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					IntProvider.codec(-16, 16).fieldOf("xz_spread").forGetter(randomOffsetPlacement -> randomOffsetPlacement.xzSpread),
					IntProvider.codec(-16, 16).fieldOf("y_spread").forGetter(randomOffsetPlacement -> randomOffsetPlacement.ySpread)
				)
				.apply(instance, RandomOffsetPlacement::new)
	);
	private final IntProvider xzSpread;
	private final IntProvider ySpread;

	public static RandomOffsetPlacement of(IntProvider intProvider, IntProvider intProvider2) {
		return new RandomOffsetPlacement(intProvider, intProvider2);
	}

	public static RandomOffsetPlacement vertical(IntProvider intProvider) {
		return new RandomOffsetPlacement(ConstantInt.of(0), intProvider);
	}

	public static RandomOffsetPlacement horizontal(IntProvider intProvider) {
		return new RandomOffsetPlacement(intProvider, ConstantInt.of(0));
	}

	private RandomOffsetPlacement(IntProvider intProvider, IntProvider intProvider2) {
		this.xzSpread = intProvider;
		this.ySpread = intProvider2;
	}

	@Override
	public Stream<BlockPos> getPositions(PlacementContext placementContext, Random random, BlockPos blockPos) {
		int i = blockPos.getX() + this.xzSpread.sample(random);
		int j = blockPos.getY() + this.ySpread.sample(random);
		int k = blockPos.getZ() + this.xzSpread.sample(random);
		return Stream.of(new BlockPos(i, j, k));
	}

	@Override
	public PlacementModifierType<?> type() {
		return PlacementModifierType.RANDOM_OFFSET;
	}
}
