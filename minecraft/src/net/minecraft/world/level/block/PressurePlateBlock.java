package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class PressurePlateBlock extends BasePressurePlateBlock {
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	private final PressurePlateBlock.Sensitivity sensitivity;

	protected PressurePlateBlock(PressurePlateBlock.Sensitivity sensitivity, BlockBehaviour.Properties properties, BlockSetType blockSetType) {
		super(properties, blockSetType);
		this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, Boolean.valueOf(false)));
		this.sensitivity = sensitivity;
	}

	@Override
	protected int getSignalForState(BlockState blockState) {
		return blockState.getValue(POWERED) ? 15 : 0;
	}

	@Override
	protected BlockState setSignalForState(BlockState blockState, int i) {
		return blockState.setValue(POWERED, Boolean.valueOf(i > 0));
	}

	@Override
	protected int getSignalStrength(Level level, BlockPos blockPos) {
		Class class_ = switch (this.sensitivity) {
			case EVERYTHING -> Entity.class;
			case MOBS -> LivingEntity.class;
		};
		return getEntityCount(level, TOUCH_AABB.move(blockPos), class_) > 0 ? 15 : 0;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(POWERED);
	}

	public static enum Sensitivity {
		EVERYTHING,
		MOBS;
	}
}
