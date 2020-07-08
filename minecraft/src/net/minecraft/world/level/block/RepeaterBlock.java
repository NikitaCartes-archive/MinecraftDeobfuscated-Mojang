package net.minecraft.world.level.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public class RepeaterBlock extends DiodeBlock {
	public static final BooleanProperty LOCKED = BlockStateProperties.LOCKED;
	public static final IntegerProperty DELAY = BlockStateProperties.DELAY;

	protected RepeaterBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(
			this.stateDefinition
				.any()
				.setValue(FACING, Direction.NORTH)
				.setValue(DELAY, Integer.valueOf(1))
				.setValue(LOCKED, Boolean.valueOf(false))
				.setValue(POWERED, Boolean.valueOf(false))
		);
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		if (!player.abilities.mayBuild) {
			return InteractionResult.PASS;
		} else {
			level.setBlock(blockPos, blockState.cycle(DELAY), 3);
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
	}

	@Override
	protected int getDelay(BlockState blockState) {
		return (Integer)blockState.getValue(DELAY) * 2;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		BlockState blockState = super.getStateForPlacement(blockPlaceContext);
		return blockState.setValue(LOCKED, Boolean.valueOf(this.isLocked(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos(), blockState)));
	}

	@Override
	public BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		return !levelAccessor.isClientSide() && direction.getAxis() != ((Direction)blockState.getValue(FACING)).getAxis()
			? blockState.setValue(LOCKED, Boolean.valueOf(this.isLocked(levelAccessor, blockPos, blockState)))
			: super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	public boolean isLocked(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		return this.getAlternateSignal(levelReader, blockPos, blockState) > 0;
	}

	@Override
	protected boolean isAlternateInput(BlockState blockState) {
		return isDiode(blockState);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
		if ((Boolean)blockState.getValue(POWERED)) {
			Direction direction = blockState.getValue(FACING);
			double d = (double)blockPos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.2;
			double e = (double)blockPos.getY() + 0.4 + (random.nextDouble() - 0.5) * 0.2;
			double f = (double)blockPos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.2;
			float g = -5.0F;
			if (random.nextBoolean()) {
				g = (float)((Integer)blockState.getValue(DELAY) * 2 - 1);
			}

			g /= 16.0F;
			double h = (double)(g * (float)direction.getStepX());
			double i = (double)(g * (float)direction.getStepZ());
			level.addParticle(DustParticleOptions.REDSTONE, d + h, e, f + i, 0.0, 0.0, 0.0);
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, DELAY, LOCKED, POWERED);
	}
}
