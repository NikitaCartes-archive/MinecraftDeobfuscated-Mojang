package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;

public class TrunkVineDecorator extends TreeDecorator {
	public static final Codec<TrunkVineDecorator> CODEC = Codec.unit((Supplier<TrunkVineDecorator>)(() -> TrunkVineDecorator.INSTANCE));
	public static final TrunkVineDecorator INSTANCE = new TrunkVineDecorator();

	@Override
	protected TreeDecoratorType<?> type() {
		return TreeDecoratorType.TRUNK_VINE;
	}

	@Override
	public void place(
		LevelSimulatedReader levelSimulatedReader,
		BiConsumer<BlockPos, BlockState> biConsumer,
		RandomSource randomSource,
		List<BlockPos> list,
		List<BlockPos> list2,
		List<BlockPos> list3
	) {
		list.forEach(blockPos -> {
			if (randomSource.nextInt(3) > 0) {
				BlockPos blockPos2 = blockPos.west();
				if (Feature.isAir(levelSimulatedReader, blockPos2)) {
					placeVine(biConsumer, blockPos2, VineBlock.EAST);
				}
			}

			if (randomSource.nextInt(3) > 0) {
				BlockPos blockPos2 = blockPos.east();
				if (Feature.isAir(levelSimulatedReader, blockPos2)) {
					placeVine(biConsumer, blockPos2, VineBlock.WEST);
				}
			}

			if (randomSource.nextInt(3) > 0) {
				BlockPos blockPos2 = blockPos.north();
				if (Feature.isAir(levelSimulatedReader, blockPos2)) {
					placeVine(biConsumer, blockPos2, VineBlock.SOUTH);
				}
			}

			if (randomSource.nextInt(3) > 0) {
				BlockPos blockPos2 = blockPos.south();
				if (Feature.isAir(levelSimulatedReader, blockPos2)) {
					placeVine(biConsumer, blockPos2, VineBlock.NORTH);
				}
			}
		});
	}
}
