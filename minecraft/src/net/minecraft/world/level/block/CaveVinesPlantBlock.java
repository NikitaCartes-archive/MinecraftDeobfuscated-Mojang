package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

public class CaveVinesPlantBlock extends GrowingPlantBodyBlock implements BonemealableBlock, CaveVines {
	public CaveVinesPlantBlock(BlockBehaviour.Properties properties) {
		super(properties, Direction.DOWN, SHAPE, false);
		this.registerDefaultState(this.stateDefinition.any().setValue(BERRIES, Boolean.valueOf(false)));
	}

	@Override
	protected GrowingPlantHeadBlock getHeadBlock() {
		return (GrowingPlantHeadBlock)Blocks.CAVE_VINES;
	}

	@Override
	protected BlockState updateHeadAfterConvertedFromBody(BlockState blockState, BlockState blockState2) {
		return blockState2.setValue(BERRIES, (Boolean)blockState.getValue(BERRIES));
	}

	@Override
	public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
		return new ItemStack(Items.GLOW_BERRIES);
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		return CaveVines.use(blockState, level, blockPos);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BERRIES);
	}

	@Override
	public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean bl) {
		return !(Boolean)blockState.getValue(BERRIES);
	}

	@Override
	public boolean isBonemealSuccess(Level level, Random random, BlockPos blockPos, BlockState blockState) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, Random random, BlockPos blockPos, BlockState blockState) {
		serverLevel.setBlock(blockPos, blockState.setValue(BERRIES, Boolean.valueOf(true)), 2);
	}
}
