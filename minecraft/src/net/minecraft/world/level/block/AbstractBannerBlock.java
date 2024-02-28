package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
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
	public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		return levelReader.getBlockEntity(blockPos) instanceof BannerBlockEntity bannerBlockEntity
			? bannerBlockEntity.getItem()
			: super.getCloneItemStack(levelReader, blockPos, blockState);
	}

	public DyeColor getColor() {
		return this.color;
	}
}
