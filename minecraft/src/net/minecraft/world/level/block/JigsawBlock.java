package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.BlockHitResult;

public class JigsawBlock extends DirectionalBlock implements EntityBlock {
	protected JigsawBlock(Block.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return this.defaultBlockState().setValue(FACING, blockPlaceContext.getClickedFace());
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockGetter blockGetter) {
		return new JigsawBlockEntity();
	}

	@Override
	public boolean use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (blockEntity instanceof JigsawBlockEntity && player.canUseGameMasterBlocks()) {
			player.openJigsawBlock((JigsawBlockEntity)blockEntity);
			return true;
		} else {
			return false;
		}
	}

	public static boolean canAttach(StructureTemplate.StructureBlockInfo structureBlockInfo, StructureTemplate.StructureBlockInfo structureBlockInfo2) {
		return structureBlockInfo.state.getValue(FACING) == ((Direction)structureBlockInfo2.state.getValue(FACING)).getOpposite()
			&& structureBlockInfo.nbt.getString("attachement_type").equals(structureBlockInfo2.nbt.getString("attachement_type"));
	}
}
