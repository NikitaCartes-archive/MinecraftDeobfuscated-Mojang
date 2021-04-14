package net.minecraft.world.level.levelgen.feature.blockplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class ColumnPlacer extends BlockPlacer {
	public static final Codec<ColumnPlacer> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(IntProvider.NON_NEGATIVE_CODEC.fieldOf("size").forGetter(columnPlacer -> columnPlacer.size)).apply(instance, ColumnPlacer::new)
	);
	private final IntProvider size;

	public ColumnPlacer(IntProvider intProvider) {
		this.size = intProvider;
	}

	@Override
	protected BlockPlacerType<?> type() {
		return BlockPlacerType.COLUMN_PLACER;
	}

	@Override
	public void place(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, Random random) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
		int i = this.size.sample(random);

		for (int j = 0; j < i; j++) {
			levelAccessor.setBlock(mutableBlockPos, blockState, 2);
			mutableBlockPos.move(Direction.UP);
		}
	}
}
