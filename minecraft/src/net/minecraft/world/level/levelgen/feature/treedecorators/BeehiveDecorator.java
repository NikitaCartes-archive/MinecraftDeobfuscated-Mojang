package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BeehiveDecorator extends TreeDecorator {
	public static final Codec<BeehiveDecorator> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(beehiveDecorator -> beehiveDecorator.probability),
					Codec.BOOL.fieldOf("fixed_height").orElse(true).forGetter(beehiveDecorator -> beehiveDecorator.fixedHeight)
				)
				.apply(instance, BeehiveDecorator::new)
	);
	private static final Direction WORLDGEN_FACING = Direction.SOUTH;
	private static final Direction[] SPAWN_DIRECTIONS = (Direction[])Direction.Plane.HORIZONTAL
		.stream()
		.filter(direction -> direction != WORLDGEN_FACING.getOpposite())
		.toArray(Direction[]::new);
	private final float probability;
	private final boolean fixedHeight;

	public BeehiveDecorator(float f, boolean bl) {
		this.probability = f;
		this.fixedHeight = bl;
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
			if (!list2.isEmpty()) {
				int i;
				if (this.fixedHeight) {
					i = !list.isEmpty()
						? Math.max(((BlockPos)list.get(0)).getY() - 1, ((BlockPos)list2.get(0)).getY() + 1)
						: Math.min(((BlockPos)list2.get(0)).getY() + 1 + randomSource.nextInt(3), ((BlockPos)list2.get(list2.size() - 1)).getY());
				} else {
					i = randomSource.nextIntBetweenInclusive(((BlockPos)list2.get(0)).getY() + 1, ((BlockPos)list2.get(list2.size() - 1)).getY() - 1);
				}

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
								beehiveBlockEntity.storeBee(BeehiveBlockEntity.Occupant.create(randomSource.nextInt(599)));
							}
						});
					}
				}
			}
		}
	}
}
