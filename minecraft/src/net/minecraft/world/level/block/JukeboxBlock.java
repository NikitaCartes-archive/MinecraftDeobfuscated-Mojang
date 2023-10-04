package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class JukeboxBlock extends BaseEntityBlock {
	public static final MapCodec<JukeboxBlock> CODEC = simpleCodec(JukeboxBlock::new);
	public static final BooleanProperty HAS_RECORD = BlockStateProperties.HAS_RECORD;

	@Override
	public MapCodec<JukeboxBlock> codec() {
		return CODEC;
	}

	protected JukeboxBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(HAS_RECORD, Boolean.valueOf(false)));
	}

	@Override
	public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
		super.setPlacedBy(level, blockPos, blockState, livingEntity, itemStack);
		CompoundTag compoundTag = BlockItem.getBlockEntityData(itemStack);
		if (compoundTag != null && compoundTag.contains("RecordItem")) {
			level.setBlock(blockPos, blockState.setValue(HAS_RECORD, Boolean.valueOf(true)), 2);
		}
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		if ((Boolean)blockState.getValue(HAS_RECORD) && level.getBlockEntity(blockPos) instanceof JukeboxBlockEntity jukeboxBlockEntity) {
			jukeboxBlockEntity.popOutRecord();
			return InteractionResult.sidedSuccess(level.isClientSide);
		} else {
			return InteractionResult.PASS;
		}
	}

	@Override
	public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState.is(blockState2.getBlock())) {
			if (level.getBlockEntity(blockPos) instanceof JukeboxBlockEntity jukeboxBlockEntity) {
				jukeboxBlockEntity.popOutRecord();
			}

			super.onRemove(blockState, level, blockPos, blockState2, bl);
		}
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new JukeboxBlockEntity(blockPos, blockState);
	}

	@Override
	public boolean isSignalSource(BlockState blockState) {
		return true;
	}

	@Override
	public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		if (blockGetter.getBlockEntity(blockPos) instanceof JukeboxBlockEntity jukeboxBlockEntity && jukeboxBlockEntity.isRecordPlaying()) {
			return 15;
		}

		return 0;
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		if (level.getBlockEntity(blockPos) instanceof JukeboxBlockEntity jukeboxBlockEntity
			&& jukeboxBlockEntity.getTheItem().getItem() instanceof RecordItem recordItem) {
			return recordItem.getAnalogOutput();
		}

		return 0;
	}

	@Override
	public RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HAS_RECORD);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
		return blockState.getValue(HAS_RECORD) ? createTickerHelper(blockEntityType, BlockEntityType.JUKEBOX, JukeboxBlockEntity::playRecordTick) : null;
	}
}
