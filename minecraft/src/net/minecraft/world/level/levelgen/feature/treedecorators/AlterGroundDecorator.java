package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

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
	public void place(WorldGenLevel worldGenLevel, Random random, List<BlockPos> list, List<BlockPos> list2, Set<BlockPos> set, BoundingBox boundingBox) {
		int i = ((BlockPos)list.get(0)).getY();
		list.stream().filter(blockPos -> blockPos.getY() == i).forEach(blockPos -> {
			this.placeCircle(worldGenLevel, random, blockPos.west().north());
			this.placeCircle(worldGenLevel, random, blockPos.east(2).north());
			this.placeCircle(worldGenLevel, random, blockPos.west().south(2));
			this.placeCircle(worldGenLevel, random, blockPos.east(2).south(2));

			for (int ix = 0; ix < 5; ix++) {
				int j = random.nextInt(64);
				int k = j % 8;
				int l = j / 8;
				if (k == 0 || k == 7 || l == 0 || l == 7) {
					this.placeCircle(worldGenLevel, random, blockPos.offset(-3 + k, 0, -3 + l));
				}
			}
		});
	}

	private void placeCircle(LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos) {
		for (int i = -2; i <= 2; i++) {
			for (int j = -2; j <= 2; j++) {
				if (Math.abs(i) != 2 || Math.abs(j) != 2) {
					this.placeBlockAt(levelSimulatedRW, random, blockPos.offset(i, 0, j));
				}
			}
		}
	}

	private void placeBlockAt(LevelSimulatedRW levelSimulatedRW, Random random, BlockPos blockPos) {
		for (int i = 2; i >= -3; i--) {
			BlockPos blockPos2 = blockPos.above(i);
			if (Feature.isGrassOrDirt(levelSimulatedRW, blockPos2)) {
				levelSimulatedRW.setBlock(blockPos2, this.provider.getState(random, blockPos), 19);
				break;
			}

			if (!Feature.isAir(levelSimulatedRW, blockPos2) && i < 0) {
				break;
			}
		}
	}
}
