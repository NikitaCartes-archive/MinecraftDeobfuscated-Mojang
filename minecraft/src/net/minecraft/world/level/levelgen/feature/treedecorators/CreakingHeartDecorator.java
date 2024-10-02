package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CreakingHeartBlock;

public class CreakingHeartDecorator extends TreeDecorator {
	public static final MapCodec<CreakingHeartDecorator> CODEC = Codec.floatRange(0.0F, 1.0F)
		.fieldOf("probability")
		.xmap(CreakingHeartDecorator::new, creakingHeartDecorator -> creakingHeartDecorator.probability);
	private final float probability;

	public CreakingHeartDecorator(float f) {
		this.probability = f;
	}

	@Override
	protected TreeDecoratorType<?> type() {
		return TreeDecoratorType.CREAKING_HEART;
	}

	@Override
	public void place(TreeDecorator.Context context) {
		RandomSource randomSource = context.random();
		List<BlockPos> list = context.logs();
		if (!list.isEmpty()) {
			if (!(randomSource.nextFloat() >= this.probability)) {
				List<BlockPos> list2 = new ArrayList(list);
				Util.shuffle(list2, randomSource);
				Optional<BlockPos> optional = list2.stream().filter(blockPos -> {
					for (Direction direction : Direction.values()) {
						if (!context.checkBlock(blockPos.relative(direction), blockState -> blockState.is(BlockTags.LOGS))) {
							return false;
						}
					}

					return true;
				}).findFirst();
				if (!optional.isEmpty()) {
					context.setBlock(
						(BlockPos)optional.get(), Blocks.CREAKING_HEART.defaultBlockState().setValue(CreakingHeartBlock.CREAKING, CreakingHeartBlock.CreakingHeartState.DORMANT)
					);
				}
			}
		}
	}
}
