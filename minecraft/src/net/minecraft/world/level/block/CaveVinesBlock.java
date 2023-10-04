package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

public class CaveVinesBlock extends GrowingPlantHeadBlock implements BonemealableBlock, CaveVines {
	public static final MapCodec<CaveVinesBlock> CODEC = simpleCodec(CaveVinesBlock::new);
	private static final float CHANCE_OF_BERRIES_ON_GROWTH = 0.11F;

	@Override
	public MapCodec<CaveVinesBlock> codec() {
		return CODEC;
	}

	public CaveVinesBlock(BlockBehaviour.Properties properties) {
		super(properties, Direction.DOWN, SHAPE, false, 0.1);
		this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)).setValue(BERRIES, Boolean.valueOf(false)));
	}

	@Override
	protected int getBlocksToGrowWhenBonemealed(RandomSource randomSource) {
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
	protected BlockState getGrowIntoState(BlockState blockState, RandomSource randomSource) {
		return super.getGrowIntoState(blockState, randomSource).setValue(BERRIES, Boolean.valueOf(randomSource.nextFloat() < 0.11F));
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		return new ItemStack(Items.GLOW_BERRIES);
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		return CaveVines.use(player, blockState, level, blockPos);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(BERRIES);
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		return !(Boolean)blockState.getValue(BERRIES);
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
		serverLevel.setBlock(blockPos, blockState.setValue(BERRIES, Boolean.valueOf(true)), 2);
	}
}
