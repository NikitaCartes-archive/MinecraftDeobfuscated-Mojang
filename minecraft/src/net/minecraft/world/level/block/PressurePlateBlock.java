package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
	public static final MapCodec<PressurePlateBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(BlockSetType.CODEC.fieldOf("block_set_type").forGetter(pressurePlateBlock -> pressurePlateBlock.type), propertiesCodec())
				.apply(instance, PressurePlateBlock::new)
	);
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	@Override
	public MapCodec<PressurePlateBlock> codec() {
		return CODEC;
	}

	protected PressurePlateBlock(BlockSetType blockSetType, BlockBehaviour.Properties properties) {
		super(properties, blockSetType);
		this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, Boolean.valueOf(false)));
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
		Class<? extends Entity> class_ = switch (this.type.pressurePlateSensitivity()) {
			case EVERYTHING -> Entity.class;
			case MOBS -> LivingEntity.class;
		};
		return getEntityCount(level, TOUCH_AABB.move(blockPos), class_) > 0 ? 15 : 0;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(POWERED);
	}
}
