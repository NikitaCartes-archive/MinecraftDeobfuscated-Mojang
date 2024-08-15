package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class AbstractCauldronBlock extends Block {
	private static final int SIDE_THICKNESS = 2;
	private static final int LEG_WIDTH = 4;
	private static final int LEG_HEIGHT = 3;
	private static final int LEG_DEPTH = 2;
	protected static final int FLOOR_LEVEL = 4;
	private static final VoxelShape INSIDE = box(2.0, 4.0, 2.0, 14.0, 16.0, 14.0);
	protected static final VoxelShape SHAPE = Shapes.join(
		Shapes.block(),
		Shapes.or(box(0.0, 0.0, 4.0, 16.0, 3.0, 12.0), box(4.0, 0.0, 0.0, 12.0, 3.0, 16.0), box(2.0, 0.0, 2.0, 14.0, 3.0, 14.0), INSIDE),
		BooleanOp.ONLY_FIRST
	);
	protected final CauldronInteraction.InteractionMap interactions;

	@Override
	protected abstract MapCodec<? extends AbstractCauldronBlock> codec();

	public AbstractCauldronBlock(BlockBehaviour.Properties properties, CauldronInteraction.InteractionMap interactionMap) {
		super(properties);
		this.interactions = interactionMap;
	}

	protected double getContentHeight(BlockState blockState) {
		return 0.0;
	}

	protected boolean isEntityInsideContent(BlockState blockState, BlockPos blockPos, Entity entity) {
		return entity.getY() < (double)blockPos.getY() + this.getContentHeight(blockState) && entity.getBoundingBox().maxY > (double)blockPos.getY() + 0.25;
	}

	@Override
	protected InteractionResult useItemOn(
		ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		CauldronInteraction cauldronInteraction = (CauldronInteraction)this.interactions.map().get(itemStack.getItem());
		return cauldronInteraction.interact(blockState, level, blockPos, player, interactionHand, itemStack);
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	protected VoxelShape getInteractionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return INSIDE;
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}

	@Override
	protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
		return false;
	}

	public abstract boolean isFull(BlockState blockState);

	@Override
	protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		BlockPos blockPos2 = PointedDripstoneBlock.findStalactiteTipAboveCauldron(serverLevel, blockPos);
		if (blockPos2 != null) {
			Fluid fluid = PointedDripstoneBlock.getCauldronFillFluidType(serverLevel, blockPos2);
			if (fluid != Fluids.EMPTY && this.canReceiveStalactiteDrip(fluid)) {
				this.receiveStalactiteDrip(blockState, serverLevel, blockPos, fluid);
			}
		}
	}

	protected boolean canReceiveStalactiteDrip(Fluid fluid) {
		return false;
	}

	protected void receiveStalactiteDrip(BlockState blockState, Level level, BlockPos blockPos, Fluid fluid) {
	}
}
