package net.minecraft.world.phys.shapes;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;

public class MinecartCollisionContext extends EntityCollisionContext {
	@Nullable
	private BlockPos ingoreBelow;
	@Nullable
	private BlockPos slopeIgnore;

	protected MinecartCollisionContext(AbstractMinecart abstractMinecart, boolean bl) {
		super(abstractMinecart, bl);
		this.setupContext(abstractMinecart);
	}

	private void setupContext(AbstractMinecart abstractMinecart) {
		BlockPos blockPos = abstractMinecart.getCurrentBlockPosOrRailBelow();
		BlockState blockState = abstractMinecart.level().getBlockState(blockPos);
		boolean bl = BaseRailBlock.isRail(blockState);
		if (bl) {
			this.ingoreBelow = blockPos.below();
			RailShape railShape = blockState.getValue(((BaseRailBlock)blockState.getBlock()).getShapeProperty());
			if (railShape.isSlope()) {
				this.slopeIgnore = switch (railShape) {
					case ASCENDING_EAST -> blockPos.east();
					case ASCENDING_WEST -> blockPos.west();
					case ASCENDING_NORTH -> blockPos.north();
					case ASCENDING_SOUTH -> blockPos.south();
					default -> null;
				};
			}
		}
	}

	@Override
	public VoxelShape getCollisionShape(BlockState blockState, CollisionGetter collisionGetter, BlockPos blockPos) {
		return !blockPos.equals(this.ingoreBelow) && !blockPos.equals(this.slopeIgnore)
			? super.getCollisionShape(blockState, collisionGetter, blockPos)
			: Shapes.empty();
	}
}
