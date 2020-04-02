package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class BeehiveDecorator extends TreeDecorator {
	private final float probability;

	public BeehiveDecorator(float f) {
		super(TreeDecoratorType.BEEHIVE);
		this.probability = f;
	}

	public <T> BeehiveDecorator(Dynamic<T> dynamic) {
		this(dynamic.get("probability").asFloat(0.0F));
	}

	@Override
	public void place(LevelAccessor levelAccessor, Random random, List<BlockPos> list, List<BlockPos> list2, Set<BlockPos> set, BoundingBox boundingBox) {
		if (!(random.nextFloat() >= this.probability)) {
			Direction direction = BeehiveBlock.getRandomOffset(random);
			int i = !list2.isEmpty()
				? Math.max(((BlockPos)list2.get(0)).getY() - 1, ((BlockPos)list.get(0)).getY())
				: Math.min(((BlockPos)list.get(0)).getY() + 1 + random.nextInt(3), ((BlockPos)list.get(list.size() - 1)).getY());
			List<BlockPos> list3 = (List<BlockPos>)list.stream().filter(blockPosx -> blockPosx.getY() == i).collect(Collectors.toList());
			if (!list3.isEmpty()) {
				BlockPos blockPos = (BlockPos)list3.get(random.nextInt(list3.size()));
				BlockPos blockPos2 = blockPos.relative(direction);
				if (AbstractTreeFeature.isAir(levelAccessor, blockPos2) && AbstractTreeFeature.isAir(levelAccessor, blockPos2.relative(Direction.SOUTH))) {
					BlockState blockState = Blocks.BEE_NEST.defaultBlockState().setValue(BeehiveBlock.FACING, Direction.SOUTH);
					this.setBlock(levelAccessor, blockPos2, blockState, set, boundingBox);
					BlockEntity blockEntity = levelAccessor.getBlockEntity(blockPos2);
					if (blockEntity instanceof BeehiveBlockEntity) {
						BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity)blockEntity;
						int j = 2 + random.nextInt(2);

						for (int k = 0; k < j; k++) {
							Bee bee = new Bee(EntityType.BEE, levelAccessor.getLevel());
							beehiveBlockEntity.addOccupantWithPresetTicks(bee, false, random.nextInt(599));
						}
					}
				}
			}
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
}
