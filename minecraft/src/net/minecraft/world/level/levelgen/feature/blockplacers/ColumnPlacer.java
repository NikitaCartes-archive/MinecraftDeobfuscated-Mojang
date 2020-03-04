package net.minecraft.world.level.levelgen.feature.blockplacers;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class ColumnPlacer extends BlockPlacer {
	private final int minSize;
	private final int extraSize;

	public ColumnPlacer(int i, int j) {
		super(BlockPlacerType.COLUMN_PLACER);
		this.minSize = i;
		this.extraSize = j;
	}

	public <T> ColumnPlacer(Dynamic<T> dynamic) {
		this(dynamic.get("min_size").asInt(1), dynamic.get("extra_size").asInt(2));
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

	@Override
	public <T> T serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
				dynamicOps,
				dynamicOps.createMap(
					ImmutableMap.of(
						dynamicOps.createString("type"),
						dynamicOps.createString(Registry.BLOCK_PLACER_TYPES.getKey(this.type).toString()),
						dynamicOps.createString("min_size"),
						dynamicOps.createInt(this.minSize),
						dynamicOps.createString("extra_size"),
						dynamicOps.createInt(this.extraSize)
					)
				)
			)
			.getValue();
	}
}
