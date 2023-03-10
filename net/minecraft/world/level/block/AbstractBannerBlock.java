/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractBannerBlock
extends BaseEntityBlock {
    private final DyeColor color;

    protected AbstractBannerBlock(DyeColor dyeColor, BlockBehaviour.Properties properties) {
        super(properties);
        this.color = dyeColor;
    }

    @Override
    public boolean isPossibleToRespawnInThis() {
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
    public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        BlockEntity blockEntity = blockGetter.getBlockEntity(blockPos);
        if (blockEntity instanceof BannerBlockEntity) {
            return ((BannerBlockEntity)blockEntity).getItem();
        }
        return super.getCloneItemStack(blockGetter, blockPos, blockState);
    }

    public DyeColor getColor() {
        return this.color;
    }
}

