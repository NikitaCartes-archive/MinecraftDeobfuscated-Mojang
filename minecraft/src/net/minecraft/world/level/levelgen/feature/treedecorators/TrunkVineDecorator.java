package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class TrunkVineDecorator extends TreeDecorator {
	public static final Codec<TrunkVineDecorator> CODEC = Codec.unit((Supplier<TrunkVineDecorator>)(() -> TrunkVineDecorator.INSTANCE));
	public static final TrunkVineDecorator INSTANCE = new TrunkVineDecorator();

	@Override
	protected TreeDecoratorType<?> type() {
		return TreeDecoratorType.TRUNK_VINE;
	}

	@Override
	public void place(WorldGenLevel worldGenLevel, Random random, List<BlockPos> list, List<BlockPos> list2, Set<BlockPos> set, BoundingBox boundingBox) {
		list.forEach(blockPos -> {
			if (random.nextInt(3) > 0) {
				BlockPos blockPos2 = blockPos.west();
				if (Feature.isAir(worldGenLevel, blockPos2)) {
					this.placeVine(worldGenLevel, blockPos2, VineBlock.EAST, set, boundingBox);
				}
			}

			if (random.nextInt(3) > 0) {
				BlockPos blockPos2 = blockPos.east();
				if (Feature.isAir(worldGenLevel, blockPos2)) {
					this.placeVine(worldGenLevel, blockPos2, VineBlock.WEST, set, boundingBox);
				}
			}

			if (random.nextInt(3) > 0) {
				BlockPos blockPos2 = blockPos.north();
				if (Feature.isAir(worldGenLevel, blockPos2)) {
					this.placeVine(worldGenLevel, blockPos2, VineBlock.SOUTH, set, boundingBox);
				}
			}

			if (random.nextInt(3) > 0) {
				BlockPos blockPos2 = blockPos.south();
				if (Feature.isAir(worldGenLevel, blockPos2)) {
					this.placeVine(worldGenLevel, blockPos2, VineBlock.NORTH, set, boundingBox);
				}
			}
		});
	}
}
