package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractBannerBlock extends BaseEntityBlock {
	private final DyeColor color;

	protected AbstractBannerBlock(DyeColor dyeColor, Block.Properties properties) {
		super(properties);
		this.color = dyeColor;
	}

	@Override
	public boolean isPossibleToRespawnInThis() {
		return true;
	}

	@Override
	public BlockEntity newBlockEntity(BlockGetter blockGetter) {
		return new BannerBlockEntity(this.color);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
		if (itemStack.hasCustomHoverName()) {
			BlockEntity blockEntity = level.getBlockEntity(blockPos);
			if (blockEntity instanceof BannerBlockEntity) {
				((BannerBlockEntity)blockEntity).setCustomName(itemStack.getHoverName());
			}
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
		BlockEntity blockEntity = blockGetter.getBlockEntity(blockPos);
		return blockEntity instanceof BannerBlockEntity
			? ((BannerBlockEntity)blockEntity).getItem(blockState)
			: super.getCloneItemStack(blockGetter, blockPos, blockState);
	}

	public DyeColor getColor() {
		return this.color;
	}
}
