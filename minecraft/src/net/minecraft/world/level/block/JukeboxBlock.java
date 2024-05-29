package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.component.CustomData;
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
		CustomData customData = itemStack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);
		if (customData.contains("RecordItem")) {
			level.setBlock(blockPos, blockState.setValue(HAS_RECORD, Boolean.valueOf(true)), 2);
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
		if ((Boolean)blockState.getValue(HAS_RECORD) && level.getBlockEntity(blockPos) instanceof JukeboxBlockEntity jukeboxBlockEntity) {
			jukeboxBlockEntity.popOutTheItem();
			return InteractionResult.sidedSuccess(level.isClientSide);
		} else {
			return InteractionResult.PASS;
		}
	}

	@Override
	protected ItemInteractionResult useItemOn(
		ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		if ((Boolean)blockState.getValue(HAS_RECORD)) {
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		} else {
			ItemStack itemStack2 = player.getItemInHand(interactionHand);
			ItemInteractionResult itemInteractionResult = JukeboxPlayable.tryInsertIntoJukebox(level, blockPos, itemStack2, player);
			return !itemInteractionResult.consumesAction() ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION : itemInteractionResult;
		}
	}

	@Override
	protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState.is(blockState2.getBlock())) {
			if (level.getBlockEntity(blockPos) instanceof JukeboxBlockEntity jukeboxBlockEntity) {
				jukeboxBlockEntity.popOutTheItem();
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
		if (blockGetter.getBlockEntity(blockPos) instanceof JukeboxBlockEntity jukeboxBlockEntity && jukeboxBlockEntity.getSongPlayer().isPlaying()) {
			return 15;
		}

		return 0;
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		return level.getBlockEntity(blockPos) instanceof JukeboxBlockEntity jukeboxBlockEntity ? jukeboxBlockEntity.getComparatorOutput() : 0;
	}

	@Override
	protected RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HAS_RECORD);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
		return blockState.getValue(HAS_RECORD) ? createTickerHelper(blockEntityType, BlockEntityType.JUKEBOX, JukeboxBlockEntity::tick) : null;
	}
}
