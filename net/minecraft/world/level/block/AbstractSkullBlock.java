/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractSkullBlock
extends BaseEntityBlock {
    private final SkullBlock.Type type;

    public AbstractSkullBlock(SkullBlock.Type type, Block.Properties properties) {
        super(properties);
        this.type = type;
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public boolean hasCustomBreakingProgress(BlockState blockState) {
        return true;
    }

    @Override
    public BlockEntity newBlockEntity(BlockGetter blockGetter) {
        return new SkullBlockEntity();
    }

    @Environment(value=EnvType.CLIENT)
    public SkullBlock.Type getType() {
        return this.type;
    }
}

