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

public class CaveVinesBlock extends GrowingPlantHeadBlock implements BonemealableBlock, CaveVines {
	private static final float CHANCE_OF_BERRIES_ON_GROWTH = 0.11F;

	public CaveVinesBlock(BlockBehaviour.Properties properties) {
		super(properties, Direction.DOWN, SHAPE, false, 0.1);
		this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)).setValue(BERRIES, Boolean.valueOf(false)));
	}

	@Override
	protected int getBlocksToGrowWhenBonemealed(Random random) {
		return 1;
	}

	@Override
	protected boolean canGrowInto(BlockState blockState) {
		return blockState.isAir();
	}

	@Override
	protected Block getBodyBlock() {
		return Blocks.CAVE_VINES_PLANT;
	}

	@Override
	protected BlockState updateBodyAfterConvertedFromHead(BlockState blockState, BlockState blockState2) {
		return blockState2.setValue(BERRIES, (Boolean)blockState.getValue(BERRIES));
	}

	@Override
	protected BlockState getGrowIntoState(BlockState blockState, Random random) {
		return super.getGrowIntoState(blockState, random).setValue(BERRIES, Boolean.valueOf(random.nextFloat() < 0.11F));
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
		super.createBlockStateDefinition(builder);
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
