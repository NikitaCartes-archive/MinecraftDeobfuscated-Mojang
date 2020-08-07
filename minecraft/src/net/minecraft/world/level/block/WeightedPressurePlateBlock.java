package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class WeightedPressurePlateBlock extends BasePressurePlateBlock {
	public static final IntegerProperty POWER = BlockStateProperties.POWER;
	private final int maxWeight;

	protected WeightedPressurePlateBlock(int i, BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(POWER, Integer.valueOf(0)));
		this.maxWeight = i;
	}

	@Override
	protected int getSignalStrength(Level level, BlockPos blockPos) {
		int i = Math.min(level.getEntitiesOfClass(Entity.class, TOUCH_AABB.move(blockPos)).size(), this.maxWeight);
		if (i > 0) {
			float f = (float)Math.min(this.maxWeight, i) / (float)this.maxWeight;
			return Mth.ceil(f * 15.0F);
		} else {
			return 0;
		}
	}

	@Override
	protected void playOnSound(LevelAccessor levelAccessor, BlockPos blockPos) {
		levelAccessor.playSound(null, blockPos, SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON, SoundSource.BLOCKS, 0.3F, 0.90000004F);
	}

	@Override
	protected void playOffSound(LevelAccessor levelAccessor, BlockPos blockPos) {
		levelAccessor.playSound(null, blockPos, SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF, SoundSource.BLOCKS, 0.3F, 0.75F);
	}

	@Override
	protected int getSignalForState(BlockState blockState) {
		return (Integer)blockState.getValue(POWER);
	}

	@Override
	protected BlockState setSignalForState(BlockState blockState, int i) {
		return blockState.setValue(POWER, Integer.valueOf(i));
	}

	@Override
	protected int getPressedTime() {
		return 10;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(POWER);
	}
}
