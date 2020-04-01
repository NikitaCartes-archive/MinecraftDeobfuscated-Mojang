package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class CocoaDecorator extends TreeDecorator {
	private final float probability;

	public CocoaDecorator(float f) {
		super(TreeDecoratorType.COCOA);
		this.probability = f;
	}

	public <T> CocoaDecorator(Dynamic<T> dynamic) {
		this(dynamic.get("probability").asFloat(0.0F));
	}

	@Override
	public void place(LevelAccessor levelAccessor, Random random, List<BlockPos> list, List<BlockPos> list2, Set<BlockPos> set, BoundingBox boundingBox) {
		if (!(random.nextFloat() >= this.probability)) {
			int i = ((BlockPos)list.get(0)).getY();
			list.stream()
				.filter(blockPos -> blockPos.getY() - i <= 2)
				.forEach(
					blockPos -> {
						for (Direction direction : Direction.Plane.HORIZONTAL) {
							if (random.nextFloat() <= 0.25F) {
								Direction direction2 = direction.getOpposite();
								BlockPos blockPos2 = blockPos.offset(direction2.getStepX(), 0, direction2.getStepZ());
								if (AbstractTreeFeature.isAir(levelAccessor, blockPos2)) {
									BlockState blockState = Blocks.COCOA
										.defaultBlockState()
										.setValue(CocoaBlock.AGE, Integer.valueOf(random.nextInt(3)))
										.setValue(CocoaBlock.FACING, direction);
									this.setBlock(levelAccessor, blockPos2, blockState, set, boundingBox);
								}
							}
						}
					}
				);
		}
	}

	@Override
	public <T> T serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
				dynamicOps,
				dynamicOps.createMap(
					ImmutableMap.of(
						dynamicOps.createString("type"),
						dynamicOps.createString(Registry.TREE_DECORATOR_TYPES.getKey(this.type).toString()),
						dynamicOps.createString("probability"),
						dynamicOps.createFloat(this.probability)
					)
				)
			)
			.getValue();
	}

	public static CocoaDecorator random(Random random) {
		return new CocoaDecorator(random.nextFloat() / 2.0F);
	}
}
