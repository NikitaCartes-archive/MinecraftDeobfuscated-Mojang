/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

public class EndGatewayBlock
extends BaseEntityBlock {
    protected EndGatewayBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new TheEndGatewayBlockEntity(blockPos, blockState);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return EndGatewayBlock.createTickerHelper(blockEntityType, BlockEntityType.END_GATEWAY, level.isClientSide ? TheEndGatewayBlockEntity::beamAnimationTick : TheEndGatewayBlockEntity::teleportTick);
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof TheEndGatewayBlockEntity)) {
            return;
        }
        int i = ((TheEndGatewayBlockEntity)blockEntity).getParticleAmount();
        for (int j = 0; j < i; ++j) {
            double d = (double)blockPos.getX() + randomSource.nextDouble();
            double e = (double)blockPos.getY() + randomSource.nextDouble();
            double f = (double)blockPos.getZ() + randomSource.nextDouble();
            double g = (randomSource.nextDouble() - 0.5) * 0.5;
            double h = (randomSource.nextDouble() - 0.5) * 0.5;
            double k = (randomSource.nextDouble() - 0.5) * 0.5;
            int l = randomSource.nextInt(2) * 2 - 1;
            if (randomSource.nextBoolean()) {
                f = (double)blockPos.getZ() + 0.5 + 0.25 * (double)l;
                k = randomSource.nextFloat() * 2.0f * (float)l;
            } else {
                d = (double)blockPos.getX() + 0.5 + 0.25 * (double)l;
                g = randomSource.nextFloat() * 2.0f * (float)l;
            }
            level.addParticle(ParticleTypes.PORTAL, d, e, f, g, h, k);
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canBeReplaced(BlockState blockState, Fluid fluid) {
        return false;
    }
}

