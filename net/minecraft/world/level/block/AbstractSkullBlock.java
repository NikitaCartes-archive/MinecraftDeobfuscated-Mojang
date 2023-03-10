/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractSkullBlock
extends BaseEntityBlock
implements Equipable {
    private final SkullBlock.Type type;

    public AbstractSkullBlock(SkullBlock.Type type, BlockBehaviour.Properties properties) {
        super(properties);
        this.type = type;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new SkullBlockEntity(blockPos, blockState);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            boolean bl;
            boolean bl2 = bl = blockState.is(Blocks.DRAGON_HEAD) || blockState.is(Blocks.DRAGON_WALL_HEAD) || blockState.is(Blocks.PIGLIN_HEAD) || blockState.is(Blocks.PIGLIN_WALL_HEAD);
            if (bl) {
                return AbstractSkullBlock.createTickerHelper(blockEntityType, BlockEntityType.SKULL, SkullBlockEntity::animation);
            }
        }
        return null;
    }

    public SkullBlock.Type getType() {
        return this.type;
    }

    @Override
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }
}

