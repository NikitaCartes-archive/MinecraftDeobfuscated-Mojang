package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractBannerBlock extends BaseEntityBlock {
	private final DyeColor color;

	protected AbstractBannerBlock(DyeColor dyeColor, BlockBehaviour.Properties properties) {
		super(properties);
		this.color = dyeColor;
	}

	@Override
	protected abstract MapCodec<? extends AbstractBannerBlock> codec();

	@Override
	public boolean isPossibleToRespawnInThis(BlockState blockState) {
		return true;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new BannerBlockEntity(blockPos, blockState, this.color);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
		if (level.isClientSide) {
			level.getBlockEntity(blockPos, BlockEntityType.BANNER).ifPresent(bannerBlockEntity -> bannerBlockEntity.fromItem(itemStack));
		} else if (itemStack.hasCustomHoverName()) {
			level.getBlockEntity(blockPos, BlockEntityType.BANNER).ifPresent(bannerBlockEntity -> bannerBlockEntity.setCustomName(itemStack.getHoverName()));
		}
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		BlockEntity blockEntity = levelReader.getBlockEntity(blockPos);
		return blockEntity instanceof BannerBlockEntity ? ((BannerBlockEntity)blockEntity).getItem() : super.getCloneItemStack(levelReader, blockPos, blockState);
	}

	public DyeColor getColor() {
		return this.color;
	}
}
