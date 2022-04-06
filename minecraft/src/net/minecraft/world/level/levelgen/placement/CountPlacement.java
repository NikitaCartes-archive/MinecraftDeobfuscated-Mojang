package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;

public class CountPlacement extends RepeatingPlacement {
	public static final Codec<CountPlacement> CODEC = IntProvider.codec(0, 256)
		.fieldOf("count")
		.<CountPlacement>xmap(CountPlacement::new, countPlacement -> countPlacement.count)
		.codec();
	private final IntProvider count;

	private CountPlacement(IntProvider intProvider) {
		this.count = intProvider;
	}

	public static CountPlacement of(IntProvider intProvider) {
		return new CountPlacement(intProvider);
	}

	public static CountPlacement of(int i) {
		return of(ConstantInt.of(i));
	}

	@Override
	protected int count(RandomSource randomSource, BlockPos blockPos) {
		return this.count.sample(randomSource);
	}

	@Override
	public PlacementModifierType<?> type() {
		return PlacementModifierType.COUNT;
	}
}
