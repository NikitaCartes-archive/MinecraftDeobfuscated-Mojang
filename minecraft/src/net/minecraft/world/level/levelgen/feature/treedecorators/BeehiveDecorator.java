package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class BeehiveDecorator extends TreeDecorator {
	public static final Codec<BeehiveDecorator> CODEC = Codec.floatRange(0.0F, 1.0F)
		.fieldOf("probability")
		.<BeehiveDecorator>xmap(BeehiveDecorator::new, beehiveDecorator -> beehiveDecorator.probability)
		.codec();
	private final float probability;

	public BeehiveDecorator(float f) {
		this.probability = f;
	}

	@Override
	protected TreeDecoratorType<?> type() {
		return TreeDecoratorType.BEEHIVE;
	}

	@Override
	public void place(WorldGenLevel worldGenLevel, Random random, List<BlockPos> list, List<BlockPos> list2, Set<BlockPos> set, BoundingBox boundingBox) {
		if (!(random.nextFloat() >= this.probability)) {
			Direction direction = BeehiveBlock.getRandomOffset(random);
			int i = !list2.isEmpty()
				? Math.max(((BlockPos)list2.get(0)).getY() - 1, ((BlockPos)list.get(0)).getY())
				: Math.min(((BlockPos)list.get(0)).getY() + 1 + random.nextInt(3), ((BlockPos)list.get(list.size() - 1)).getY());
			List<BlockPos> list3 = (List<BlockPos>)list.stream().filter(blockPosx -> blockPosx.getY() == i).collect(Collectors.toList());
			if (!list3.isEmpty()) {
				BlockPos blockPos = (BlockPos)list3.get(random.nextInt(list3.size()));
				BlockPos blockPos2 = blockPos.relative(direction);
				if (Feature.isAir(worldGenLevel, blockPos2) && Feature.isAir(worldGenLevel, blockPos2.relative(Direction.SOUTH))) {
					BlockState blockState = Blocks.BEE_NEST.defaultBlockState().setValue(BeehiveBlock.FACING, Direction.SOUTH);
					this.setBlock(worldGenLevel, blockPos2, blockState, set, boundingBox);
					BlockEntity blockEntity = worldGenLevel.getBlockEntity(blockPos2);
					if (blockEntity instanceof BeehiveBlockEntity) {
						BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity)blockEntity;
						int j = 2 + random.nextInt(2);

						for (int k = 0; k < j; k++) {
							Bee bee = new Bee(EntityType.BEE, worldGenLevel.getLevel());
							beehiveBlockEntity.addOccupantWithPresetTicks(bee, false, random.nextInt(599));
						}
					}
				}
			}
		}
	}
}
