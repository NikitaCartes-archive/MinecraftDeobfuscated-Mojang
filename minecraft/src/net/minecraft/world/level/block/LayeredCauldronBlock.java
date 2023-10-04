package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class LayeredCauldronBlock extends AbstractCauldronBlock {
	public static final MapCodec<LayeredCauldronBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					Biome.Precipitation.CODEC.fieldOf("precipitation").forGetter(layeredCauldronBlock -> layeredCauldronBlock.precipitationType),
					CauldronInteraction.CODEC.fieldOf("interactions").forGetter(layeredCauldronBlock -> layeredCauldronBlock.interactions),
					propertiesCodec()
				)
				.apply(instance, LayeredCauldronBlock::new)
	);
	public static final int MIN_FILL_LEVEL = 1;
	public static final int MAX_FILL_LEVEL = 3;
	public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_CAULDRON;
	private static final int BASE_CONTENT_HEIGHT = 6;
	private static final double HEIGHT_PER_LEVEL = 3.0;
	private final Biome.Precipitation precipitationType;

	@Override
	public MapCodec<LayeredCauldronBlock> codec() {
		return CODEC;
	}

	public LayeredCauldronBlock(Biome.Precipitation precipitation, CauldronInteraction.InteractionMap interactionMap, BlockBehaviour.Properties properties) {
		super(properties, interactionMap);
		this.precipitationType = precipitation;
		this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, Integer.valueOf(1)));
	}

	@Override
	public boolean isFull(BlockState blockState) {
		return (Integer)blockState.getValue(LEVEL) == 3;
	}

	@Override
	protected boolean canReceiveStalactiteDrip(Fluid fluid) {
		return fluid == Fluids.WATER && this.precipitationType == Biome.Precipitation.RAIN;
	}

	@Override
	protected double getContentHeight(BlockState blockState) {
		return (6.0 + (double)((Integer)blockState.getValue(LEVEL)).intValue() * 3.0) / 16.0;
	}

	@Override
	public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (!level.isClientSide && entity.isOnFire() && this.isEntityInsideContent(blockState, blockPos, entity)) {
			entity.clearFire();
			if (entity.mayInteract(level, blockPos)) {
				this.handleEntityOnFireInside(blockState, level, blockPos);
			}
		}
	}

	private void handleEntityOnFireInside(BlockState blockState, Level level, BlockPos blockPos) {
		if (this.precipitationType == Biome.Precipitation.SNOW) {
			lowerFillLevel(Blocks.WATER_CAULDRON.defaultBlockState().setValue(LEVEL, (Integer)blockState.getValue(LEVEL)), level, blockPos);
		} else {
			lowerFillLevel(blockState, level, blockPos);
		}
	}

	public static void lowerFillLevel(BlockState blockState, Level level, BlockPos blockPos) {
		int i = (Integer)blockState.getValue(LEVEL) - 1;
		BlockState blockState2 = i == 0 ? Blocks.CAULDRON.defaultBlockState() : blockState.setValue(LEVEL, Integer.valueOf(i));
		level.setBlockAndUpdate(blockPos, blockState2);
		level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(blockState2));
	}

	@Override
	public void handlePrecipitation(BlockState blockState, Level level, BlockPos blockPos, Biome.Precipitation precipitation) {
		if (CauldronBlock.shouldHandlePrecipitation(level, precipitation) && (Integer)blockState.getValue(LEVEL) != 3 && precipitation == this.precipitationType) {
			BlockState blockState2 = blockState.cycle(LEVEL);
			level.setBlockAndUpdate(blockPos, blockState2);
			level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(blockState2));
		}
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		return (Integer)blockState.getValue(LEVEL);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LEVEL);
	}

	@Override
	protected void receiveStalactiteDrip(BlockState blockState, Level level, BlockPos blockPos, Fluid fluid) {
		if (!this.isFull(blockState)) {
			BlockState blockState2 = blockState.setValue(LEVEL, Integer.valueOf((Integer)blockState.getValue(LEVEL) + 1));
			level.setBlockAndUpdate(blockPos, blockState2);
			level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(blockState2));
			level.levelEvent(1047, blockPos, 0);
		}
	}
}
