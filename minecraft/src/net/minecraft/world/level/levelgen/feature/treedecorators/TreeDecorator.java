package net.minecraft.world.level.levelgen.feature.treedecorators;

import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Serializable;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public abstract class TreeDecorator implements Serializable {
	protected final TreeDecoratorType<?> type;

	protected TreeDecorator(TreeDecoratorType<?> treeDecoratorType) {
		this.type = treeDecoratorType;
	}

	public abstract void place(LevelAccessor levelAccessor, Random random, List<BlockPos> list, List<BlockPos> list2, Set<BlockPos> set, BoundingBox boundingBox);

	protected void placeVine(LevelWriter levelWriter, BlockPos blockPos, BooleanProperty booleanProperty, Set<BlockPos> set, BoundingBox boundingBox) {
		this.setBlock(levelWriter, blockPos, Blocks.VINE.defaultBlockState().setValue(booleanProperty, Boolean.valueOf(true)), set, boundingBox);
	}

	protected void setBlock(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState, Set<BlockPos> set, BoundingBox boundingBox) {
		levelWriter.setBlock(blockPos, blockState, 19);
		set.add(blockPos);
		boundingBox.expand(new BoundingBox(blockPos, blockPos));
	}
}
