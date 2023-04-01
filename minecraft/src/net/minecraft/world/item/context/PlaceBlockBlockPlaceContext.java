package net.minecraft.world.item.context;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class PlaceBlockBlockPlaceContext extends BlockPlaceContext {
	public PlaceBlockBlockPlaceContext(Level level, InteractionHand interactionHand, ItemStack itemStack, BlockHitResult blockHitResult) {
		super(level, null, interactionHand, itemStack, blockHitResult);
		this.replaceClicked = level.getBlockState(blockHitResult.getBlockPos()).canBeReplaced(this);
	}

	public static PlaceBlockBlockPlaceContext at(Level level, BlockPos blockPos, Direction direction, ItemStack itemStack) {
		return new PlaceBlockBlockPlaceContext(
			level,
			InteractionHand.MAIN_HAND,
			itemStack,
			new BlockHitResult(
				new Vec3(
					(double)blockPos.getX() + 0.5 + (double)direction.getStepX() * 0.5,
					(double)blockPos.getY() + 0.5 + (double)direction.getStepY() * 0.5,
					(double)blockPos.getZ() + 0.5 + (double)direction.getStepZ() * 0.5
				),
				direction,
				blockPos,
				false
			)
		);
	}

	@Override
	public Direction getNearestLookingDirection() {
		return this.getHitResult().getDirection();
	}

	@Override
	public Direction getNearestLookingVerticalDirection() {
		return this.getHitResult().getDirection() == Direction.UP ? Direction.UP : Direction.DOWN;
	}

	@Override
	public Direction[] getNearestLookingDirections() {
		Direction direction = this.getHitResult().getDirection();
		Direction[] directions = new Direction[]{direction, null, null, null, null, direction.getOpposite()};
		int i = 0;

		for (Direction direction2 : Direction.values()) {
			if (direction2 != direction && direction2 != direction.getOpposite()) {
				i++;
				directions[i] = direction;
			}
		}

		return directions;
	}
}
