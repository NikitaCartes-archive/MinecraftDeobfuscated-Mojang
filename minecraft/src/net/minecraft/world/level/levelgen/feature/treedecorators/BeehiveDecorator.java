package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BeehiveDecorator extends TreeDecorator {
	public static final Codec<BeehiveDecorator> CODEC = Codec.floatRange(0.0F, 1.0F)
		.fieldOf("probability")
		.<BeehiveDecorator>xmap(BeehiveDecorator::new, beehiveDecorator -> beehiveDecorator.probability)
		.codec();
	private static final Direction WORLDGEN_FACING = Direction.SOUTH;
	private static final Direction[] SPAWN_DIRECTIONS = (Direction[])Direction.Plane.HORIZONTAL
		.stream()
		.filter(direction -> direction != WORLDGEN_FACING.getOpposite())
		.toArray(Direction[]::new);
	private final float probability;

	public BeehiveDecorator(float f) {
		this.probability = f;
	}

	@Override
	protected TreeDecoratorType<?> type() {
		return TreeDecoratorType.BEEHIVE;
	}

	@Override
	public void place(TreeDecorator.Context context) {
		RandomSource randomSource = context.random();
		if (!(randomSource.nextFloat() >= this.probability)) {
			List<BlockPos> list = context.leaves();
			List<BlockPos> list2 = context.logs();
			int i = !list.isEmpty()
				? Math.max(((BlockPos)list.get(0)).getY() - 1, ((BlockPos)list2.get(0)).getY() + 1)
				: Math.min(((BlockPos)list2.get(0)).getY() + 1 + randomSource.nextInt(3), ((BlockPos)list2.get(list2.size() - 1)).getY());
			List<BlockPos> list3 = (List<BlockPos>)list2.stream()
				.filter(blockPos -> blockPos.getY() == i)
				.flatMap(blockPos -> Stream.of(SPAWN_DIRECTIONS).map(blockPos::relative))
				.collect(Collectors.toList());
			if (!list3.isEmpty()) {
				Collections.shuffle(list3);
				Optional<BlockPos> optional = list3.stream().filter(blockPos -> context.isAir(blockPos) && context.isAir(blockPos.relative(WORLDGEN_FACING))).findFirst();
				if (!optional.isEmpty()) {
					context.setBlock((BlockPos)optional.get(), Blocks.BEE_NEST.defaultBlockState().setValue(BeehiveBlock.FACING, WORLDGEN_FACING));
					context.level().getBlockEntity((BlockPos)optional.get(), BlockEntityType.BEEHIVE).ifPresent(beehiveBlockEntity -> {
						int ix = 2 + randomSource.nextInt(2);

						for (int j = 0; j < ix; j++) {
							CompoundTag compoundTag = new CompoundTag();
							compoundTag.putString("id", Registry.ENTITY_TYPE.getKey(EntityType.BEE).toString());
							beehiveBlockEntity.storeBee(compoundTag, randomSource.nextInt(599), false);
						}
					});
				}
			}
		}
	}
}
