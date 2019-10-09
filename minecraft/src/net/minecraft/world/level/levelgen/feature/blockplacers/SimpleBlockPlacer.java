package net.minecraft.world.level.levelgen.feature.blockplacers;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleBlockPlacer extends BlockPlacer {
	public SimpleBlockPlacer() {
		super(BlockPlacerType.SIMPLE_BLOCK_PLACER);
	}

	public <T> SimpleBlockPlacer(Dynamic<T> dynamic) {
		this();
	}

	@Override
	public void place(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, Random random) {
		levelAccessor.setBlock(blockPos, blockState, 2);
	}

	@Override
	public <T> T serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
				dynamicOps,
				dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("type"), dynamicOps.createString(Registry.BLOCK_PLACER_TYPES.getKey(this.type).toString())))
			)
			.getValue();
	}
}
