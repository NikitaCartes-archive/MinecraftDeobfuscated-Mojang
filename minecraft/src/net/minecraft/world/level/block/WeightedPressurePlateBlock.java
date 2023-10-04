package net.minecraft.world.level.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class WeightedPressurePlateBlock extends BasePressurePlateBlock {
	public static final MapCodec<WeightedPressurePlateBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					Codec.intRange(1, 1024).fieldOf("max_weight").forGetter(weightedPressurePlateBlock -> weightedPressurePlateBlock.maxWeight),
					BlockSetType.CODEC.fieldOf("block_set_type").forGetter(weightedPressurePlateBlock -> weightedPressurePlateBlock.type),
					propertiesCodec()
				)
				.apply(instance, WeightedPressurePlateBlock::new)
	);
	public static final IntegerProperty POWER = BlockStateProperties.POWER;
	private final int maxWeight;

	@Override
	public MapCodec<WeightedPressurePlateBlock> codec() {
		return CODEC;
	}

	protected WeightedPressurePlateBlock(int i, BlockSetType blockSetType, BlockBehaviour.Properties properties) {
		super(properties, blockSetType);
		this.registerDefaultState(this.stateDefinition.any().setValue(POWER, Integer.valueOf(0)));
		this.maxWeight = i;
	}

	@Override
	protected int getSignalStrength(Level level, BlockPos blockPos) {
		int i = Math.min(getEntityCount(level, TOUCH_AABB.move(blockPos), Entity.class), this.maxWeight);
		if (i > 0) {
			float f = (float)Math.min(this.maxWeight, i) / (float)this.maxWeight;
			return Mth.ceil(f * 15.0F);
		} else {
			return 0;
		}
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
