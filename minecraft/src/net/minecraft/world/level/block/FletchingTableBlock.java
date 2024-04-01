package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.FletchingBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class FletchingTableBlock extends BaseEntityBlock {
	public static final MapCodec<FletchingTableBlock> CODEC = simpleCodec(FletchingTableBlock::new);

	@Override
	public MapCodec<FletchingTableBlock> codec() {
		return CODEC;
	}

	protected FletchingTableBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		} else {
			if (level.getBlockEntity(blockPos) instanceof FletchingBlockEntity fletchingBlockEntity) {
				player.openMenu(fletchingBlockEntity);
				player.awardStat(Stats.INTERACT_WITH_FLETCHING);
			}

			return InteractionResult.CONSUME;
		}
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new FletchingBlockEntity(blockPos, blockState);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
		return level.isClientSide ? null : createTickerHelper(blockEntityType, BlockEntityType.FLETCHING, FletchingBlockEntity::serverTick);
	}

	@Override
	protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		Containers.dropContentsOnDestroy(blockState, blockState2, level, blockPos);
		super.onRemove(blockState, level, blockPos, blockState2, bl);
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState blockState) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(blockPos));
	}

	@Override
	protected RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}
}
