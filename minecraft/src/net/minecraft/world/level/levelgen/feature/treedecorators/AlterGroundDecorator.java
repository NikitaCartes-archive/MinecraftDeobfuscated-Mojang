package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class AlterGroundDecorator extends TreeDecorator {
	public static final Codec<AlterGroundDecorator> CODEC = BlockStateProvider.CODEC
		.fieldOf("provider")
		.<AlterGroundDecorator>xmap(AlterGroundDecorator::new, alterGroundDecorator -> alterGroundDecorator.provider)
		.codec();
	private final BlockStateProvider provider;

	public AlterGroundDecorator(BlockStateProvider blockStateProvider) {
		this.provider = blockStateProvider;
	}

	@Override
	protected TreeDecoratorType<?> type() {
		return TreeDecoratorType.ALTER_GROUND;
	}

	@Override
	public void place(
		LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, Random random, List<BlockPos> list, List<BlockPos> list2
	) {
		if (!list.isEmpty()) {
			int i = ((BlockPos)list.get(0)).getY();
			list.stream().filter(blockPos -> blockPos.getY() == i).forEach(blockPos -> {
				this.placeCircle(levelSimulatedReader, biConsumer, random, blockPos.west().north());
				this.placeCircle(levelSimulatedReader, biConsumer, random, blockPos.east(2).north());
				this.placeCircle(levelSimulatedReader, biConsumer, random, blockPos.west().south(2));
				this.placeCircle(levelSimulatedReader, biConsumer, random, blockPos.east(2).south(2));

				for (int ix = 0; ix < 5; ix++) {
					int j = random.nextInt(64);
					int k = j % 8;
					int l = j / 8;
					if (k == 0 || k == 7 || l == 0 || l == 7) {
						this.placeCircle(levelSimulatedReader, biConsumer, random, blockPos.offset(-3 + k, 0, -3 + l));
					}
				}
			});
		}
	}

	private void placeCircle(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, Random random, BlockPos blockPos) {
		for (int i = -2; i <= 2; i++) {
			for (int j = -2; j <= 2; j++) {
				if (Math.abs(i) != 2 || Math.abs(j) != 2) {
					this.placeBlockAt(levelSimulatedReader, biConsumer, random, blockPos.offset(i, 0, j));
				}
			}
		}
	}

	private void placeBlockAt(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, Random random, BlockPos blockPos) {
		for (int i = 2; i >= -3; i--) {
			BlockPos blockPos2 = blockPos.above(i);
			if (Feature.isGrassOrDirt(levelSimulatedReader, blockPos2)) {
				biConsumer.accept(blockPos2, this.provider.getState(random, blockPos));
				break;
			}

			if (!Feature.isAir(levelSimulatedReader, blockPos2) && i < 0) {
				break;
			}
		}
	}
}
