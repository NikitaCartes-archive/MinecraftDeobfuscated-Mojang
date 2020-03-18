/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class ShearableDoublePlantBlock
extends DoublePlantBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = DoublePlantBlock.HALF;

    public ShearableDoublePlantBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        boolean bl = super.canBeReplaced(blockState, blockPlaceContext);
        if (bl && blockPlaceContext.getItemInHand().getItem() == this.asItem()) {
            return false;
        }
        return bl;
    }
}

