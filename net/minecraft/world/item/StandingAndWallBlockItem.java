/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;

public class StandingAndWallBlockItem
extends BlockItem {
    protected final Block wallBlock;

    public StandingAndWallBlockItem(Block block, Block block2, Item.Properties properties) {
        super(block, properties);
        this.wallBlock = block2;
    }

    @Override
    @Nullable
    protected BlockState getPlacementState(BlockPlaceContext blockPlaceContext) {
        BlockState blockState = this.wallBlock.getStateForPlacement(blockPlaceContext);
        BlockState blockState2 = null;
        Level levelReader = blockPlaceContext.getLevel();
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        for (Direction direction : blockPlaceContext.getNearestLookingDirections()) {
            BlockState blockState3;
            if (direction == Direction.UP) continue;
            BlockState blockState4 = blockState3 = direction == Direction.DOWN ? this.getBlock().getStateForPlacement(blockPlaceContext) : blockState;
            if (blockState3 == null || !blockState3.canSurvive(levelReader, blockPos)) continue;
            blockState2 = blockState3;
            break;
        }
        return blockState2 != null && levelReader.isUnobstructed(blockState2, blockPos, CollisionContext.empty()) ? blockState2 : null;
    }

    @Override
    public void registerBlocks(Map<Block, Item> map, Item item) {
        super.registerBlocks(map, item);
        map.put(this.wallBlock, item);
    }
}

