package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.BlockHitResult;

public class JigsawBlock extends OrientationBlock implements EntityBlock, GameMasterBlock {
	public static final EnumProperty<FrontAndTop> ORIENTATION = BlockStateProperties.ORIENTATION;

	protected JigsawBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(ORIENTATION, FrontAndTop.NORTH_UP));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(ORIENTATION);
	}

	@Override
	public BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState.setValue(ORIENTATION, rotation.rotation().rotate(blockState.getValue(ORIENTATION)));
	}

	@Override
	public BlockState mirror(BlockState blockState, Mirror mirror) {
		return blockState.setValue(ORIENTATION, mirror.rotation().rotate(blockState.getValue(ORIENTATION)));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		Direction direction = blockPlaceContext.getClickedFace();
		Direction direction2;
		if (direction.getAxis() == Direction.Axis.Y) {
			direction2 = blockPlaceContext.getHorizontalDirection().getOpposite();
		} else {
			direction2 = Direction.UP;
		}

		return this.defaultBlockState().setValue(ORIENTATION, FrontAndTop.fromFrontAndTop(direction, direction2));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new JigsawBlockEntity(blockPos, blockState);
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (blockEntity instanceof JigsawBlockEntity && player.canUseGameMasterBlocks()) {
			player.openJigsawBlock((JigsawBlockEntity)blockEntity);
			return InteractionResult.sidedSuccess(level.isClientSide);
		} else {
			return InteractionResult.PASS;
		}
	}

	public static boolean canAttach(StructureTemplate.StructureBlockInfo structureBlockInfo, StructureTemplate.StructureBlockInfo structureBlockInfo2) {
		Direction direction = getFrontFacing(structureBlockInfo.state);
		Direction direction2 = getFrontFacing(structureBlockInfo2.state);
		Direction direction3 = getTopFacing(structureBlockInfo.state);
		Direction direction4 = getTopFacing(structureBlockInfo2.state);
		JigsawBlockEntity.JointType jointType = (JigsawBlockEntity.JointType)JigsawBlockEntity.JointType.byName(structureBlockInfo.nbt.getString("joint"))
			.orElseGet(() -> direction.getAxis().isHorizontal() ? JigsawBlockEntity.JointType.ALIGNED : JigsawBlockEntity.JointType.ROLLABLE);
		boolean bl = jointType == JigsawBlockEntity.JointType.ROLLABLE;
		return direction == direction2.getOpposite()
			&& (bl || direction3 == direction4)
			&& structureBlockInfo.nbt.getString("target").equals(structureBlockInfo2.nbt.getString("name"));
	}

	public static Direction getFrontFacing(BlockState blockState) {
		return ((FrontAndTop)blockState.getValue(ORIENTATION)).front();
	}

	public static Direction getTopFacing(BlockState blockState) {
		return ((FrontAndTop)blockState.getValue(ORIENTATION)).top();
	}
}
