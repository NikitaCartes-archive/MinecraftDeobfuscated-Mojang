package net.minecraft.world.level.block;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.PositionImpl;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;

public class DispenserBlock extends Block {
	public static final DirectionProperty FACING = DirectionalBlock.FACING;
	public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
	private static final Map<Item, DispenseItemBehavior> DISPENSER_REGISTRY = Util.make(
		new Object2ObjectOpenHashMap<>(), object2ObjectOpenHashMap -> object2ObjectOpenHashMap.defaultReturnValue(new DefaultDispenseItemBehavior())
	);
	private static final int TRIGGER_DURATION = 4;

	protected DispenserBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TRIGGERED, Boolean.valueOf(false)));
	}

	public static void registerBehavior(ItemLike itemLike, DispenseItemBehavior dispenseItemBehavior) {
	}

	protected Vec3 getSpitMotion(BlockState blockState) {
		return Vec3.atLowerCornerOf(((Direction)blockState.getValue(FACING)).getNormal());
	}

	protected void dispenseFrom(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState) {
		BlockPos blockPos2 = blockPos.relative(blockState.getValue(FACING));
		BlockState blockState2 = serverLevel.getBlockState(blockPos2);
		if (!blockState2.isAir() && (!blockState2.is(Blocks.WATER) && !blockState2.is(Blocks.LAVA) || blockState2.getFluidState().isSource())) {
			FallingBlockEntity var6 = FallingBlockEntity.fall(serverLevel, blockPos2, blockState2, this.getSpitMotion(blockState).add(0.0, 0.1, 0.0));
		}
	}

	@Override
	public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		boolean bl2 = level.hasNeighborSignal(blockPos) || level.hasNeighborSignal(blockPos.above());
		boolean bl3 = (Boolean)blockState.getValue(TRIGGERED);
		if (bl2 && !bl3) {
			level.scheduleTick(blockPos, this, 4);
			level.setBlock(blockPos, blockState.setValue(TRIGGERED, Boolean.valueOf(true)), 4);
		} else if (!bl2 && bl3) {
			level.setBlock(blockPos, blockState.setValue(TRIGGERED, Boolean.valueOf(false)), 4);
		}
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		this.dispenseFrom(serverLevel, blockPos, blockState);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return this.defaultBlockState().setValue(FACING, blockPlaceContext.getNearestLookingDirection().getOpposite());
	}

	public static Position getDispensePosition(BlockSource blockSource) {
		Direction direction = blockSource.getBlockState().getValue(FACING);
		double d = blockSource.x() + 0.7 * (double)direction.getStepX();
		double e = blockSource.y() + 0.7 * (double)direction.getStepY();
		double f = blockSource.z() + 0.7 * (double)direction.getStepZ();
		return new PositionImpl(d, e, f);
	}

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState blockState, Mirror mirror) {
		return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, TRIGGERED);
	}
}
