package net.minecraft.world.item;

import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

public class StandingAndWallBlockItem extends BlockItem {
	protected final Block wallBlock;
	private final Direction attachmentDirection;

	public StandingAndWallBlockItem(Block block, Block block2, Direction direction, Item.Properties properties) {
		super(block, properties);
		this.wallBlock = block2;
		this.attachmentDirection = direction;
	}

	protected boolean canPlace(LevelReader levelReader, BlockState blockState, BlockPos blockPos) {
		return blockState.canSurvive(levelReader, blockPos);
	}

	@Nullable
	@Override
	protected BlockState getPlacementState(BlockPlaceContext blockPlaceContext) {
		BlockState blockState = this.wallBlock.getStateForPlacement(blockPlaceContext);
		BlockState blockState2 = null;
		LevelReader levelReader = blockPlaceContext.getLevel();
		BlockPos blockPos = blockPlaceContext.getClickedPos();

		for (Direction direction : blockPlaceContext.getNearestLookingDirections()) {
			if (direction != this.attachmentDirection.getOpposite()) {
				BlockState blockState3 = direction == this.attachmentDirection ? this.getBlock().getStateForPlacement(blockPlaceContext) : blockState;
				if (blockState3 != null && this.canPlace(levelReader, blockState3, blockPos)) {
					blockState2 = blockState3;
					break;
				}
			}
		}

		return blockState2 != null && levelReader.isUnobstructed(blockState2, blockPos, CollisionContext.empty()) ? blockState2 : null;
	}

	@Override
	public void registerBlocks(Map<Block, Item> map, Item item) {
		super.registerBlocks(map, item);
		map.put(this.wallBlock, item);
	}
}
