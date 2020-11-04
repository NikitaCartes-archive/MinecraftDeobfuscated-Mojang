package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class JukeboxBlock extends BaseEntityBlock {
	public static final BooleanProperty HAS_RECORD = BlockStateProperties.HAS_RECORD;

	protected JukeboxBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(HAS_RECORD, Boolean.valueOf(false)));
	}

	@Override
	public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
		super.setPlacedBy(level, blockPos, blockState, livingEntity, itemStack);
		CompoundTag compoundTag = itemStack.getOrCreateTag();
		if (compoundTag.contains("BlockEntityTag")) {
			CompoundTag compoundTag2 = compoundTag.getCompound("BlockEntityTag");
			if (compoundTag2.contains("RecordItem")) {
				level.setBlock(blockPos, blockState.setValue(HAS_RECORD, Boolean.valueOf(true)), 2);
			}
		}
	}

	@Override
	public InteractionResult use(
		BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult
	) {
		if ((Boolean)blockState.getValue(HAS_RECORD)) {
			this.dropRecording(level, blockPos);
			blockState = blockState.setValue(HAS_RECORD, Boolean.valueOf(false));
			level.setBlock(blockPos, blockState, 2);
			return InteractionResult.sidedSuccess(level.isClientSide);
		} else {
			return InteractionResult.PASS;
		}
	}

	public void setRecord(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, ItemStack itemStack) {
		BlockEntity blockEntity = levelAccessor.getBlockEntity(blockPos);
		if (blockEntity instanceof JukeboxBlockEntity) {
			((JukeboxBlockEntity)blockEntity).setRecord(itemStack.copy());
			levelAccessor.setBlock(blockPos, blockState.setValue(HAS_RECORD, Boolean.valueOf(true)), 2);
		}
	}

	private void dropRecording(Level level, BlockPos blockPos) {
		if (!level.isClientSide) {
			BlockEntity blockEntity = level.getBlockEntity(blockPos);
			if (blockEntity instanceof JukeboxBlockEntity) {
				JukeboxBlockEntity jukeboxBlockEntity = (JukeboxBlockEntity)blockEntity;
				ItemStack itemStack = jukeboxBlockEntity.getRecord();
				if (!itemStack.isEmpty()) {
					level.levelEvent(1010, blockPos, 0);
					jukeboxBlockEntity.clearContent();
					float f = 0.7F;
					double d = (double)(level.random.nextFloat() * 0.7F) + 0.15F;
					double e = (double)(level.random.nextFloat() * 0.7F) + 0.060000002F + 0.6;
					double g = (double)(level.random.nextFloat() * 0.7F) + 0.15F;
					ItemStack itemStack2 = itemStack.copy();
					ItemEntity itemEntity = new ItemEntity(level, (double)blockPos.getX() + d, (double)blockPos.getY() + e, (double)blockPos.getZ() + g, itemStack2);
					itemEntity.setDefaultPickUpDelay();
					level.addFreshEntity(itemEntity);
				}
			}
		}
	}

	@Override
	public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!blockState.is(blockState2.getBlock())) {
			this.dropRecording(level, blockPos);
			super.onRemove(blockState, level, blockPos, blockState2, bl);
		}
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new JukeboxBlockEntity(blockPos, blockState);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (blockEntity instanceof JukeboxBlockEntity) {
			Item item = ((JukeboxBlockEntity)blockEntity).getRecord().getItem();
			if (item instanceof RecordItem) {
				return ((RecordItem)item).getAnalogOutput();
			}
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
}
