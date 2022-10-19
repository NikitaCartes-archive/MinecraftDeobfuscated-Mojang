package net.minecraft.world.level.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;

public class PressurePlateBlock extends BasePressurePlateBlock {
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	private final PressurePlateBlock.Sensitivity sensitivity;
	private final SoundEvent soundOff;
	private final SoundEvent soundOn;

	protected PressurePlateBlock(PressurePlateBlock.Sensitivity sensitivity, BlockBehaviour.Properties properties, SoundEvent soundEvent, SoundEvent soundEvent2) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, Boolean.valueOf(false)));
		this.sensitivity = sensitivity;
		this.soundOff = soundEvent;
		this.soundOn = soundEvent2;
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
	protected void playOnSound(LevelAccessor levelAccessor, BlockPos blockPos) {
		levelAccessor.playSound(null, blockPos, this.soundOn, SoundSource.BLOCKS);
	}

	@Override
	protected void playOffSound(LevelAccessor levelAccessor, BlockPos blockPos) {
		levelAccessor.playSound(null, blockPos, this.soundOff, SoundSource.BLOCKS);
	}

	@Override
	protected int getSignalStrength(Level level, BlockPos blockPos) {
		AABB aABB = TOUCH_AABB.move(blockPos);
		List<? extends Entity> list;
		switch (this.sensitivity) {
			case EVERYTHING:
				list = level.getEntities(null, aABB);
				break;
			case MOBS:
				list = level.getEntitiesOfClass(LivingEntity.class, aABB);
				break;
			default:
				return 0;
		}

		if (!list.isEmpty()) {
			for (Entity entity : list) {
				if (!entity.isIgnoringBlockTriggers()) {
					return 15;
				}
			}
		}

		return 0;
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
