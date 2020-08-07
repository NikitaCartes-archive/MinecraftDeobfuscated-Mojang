package net.minecraft.world.level.levelgen.feature.blockplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class ColumnPlacer extends BlockPlacer {
	public static final Codec<ColumnPlacer> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.INT.fieldOf("min_size").forGetter(columnPlacer -> columnPlacer.minSize),
					Codec.INT.fieldOf("extra_size").forGetter(columnPlacer -> columnPlacer.extraSize)
				)
				.apply(instance, ColumnPlacer::new)
	);
	private final int minSize;
	private final int extraSize;

	public ColumnPlacer(int i, int j) {
		this.minSize = i;
		this.extraSize = j;
	}

	@Override
	protected BlockPlacerType<?> type() {
		return BlockPlacerType.COLUMN_PLACER;
	}

	@Override
	public void place(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, Random random) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
		int i = this.minSize + random.nextInt(random.nextInt(this.extraSize + 1) + 1);

		for (int j = 0; j < i; j++) {
			levelAccessor.setBlock(mutableBlockPos, blockState, 2);
			mutableBlockPos.move(Direction.UP);
		}
	}
}
