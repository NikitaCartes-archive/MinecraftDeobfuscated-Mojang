/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractChestBlock<E extends BlockEntity>
extends BaseEntityBlock {
    protected final Supplier<BlockEntityType<? extends E>> blockEntityType;

    protected AbstractChestBlock(BlockBehaviour.Properties properties, Supplier<BlockEntityType<? extends E>> supplier) {
        super(properties);
        this.blockEntityType = supplier;
    }

    @Environment(value=EnvType.CLIENT)
    public abstract DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combine(BlockState var1, Level var2, BlockPos var3, boolean var4);
}

