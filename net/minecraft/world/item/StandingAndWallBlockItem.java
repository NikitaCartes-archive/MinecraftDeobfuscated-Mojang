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
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;

public class StandingAndWallBlockItem
extends BlockItem {
    protected final Block wallBlock;
    private final Direction attachmentDirection;

    public StandingAndWallBlockItem(Block block, Block block2, Item.Properties properties, Direction direction) {
        super(block, properties);
        this.wallBlock = block2;
        this.attachmentDirection = direction;
    }

    protected boolean canPlace(LevelReader levelReader, BlockState blockState, BlockPos blockPos) {
        return blockState.canSurvive(levelReader, blockPos);
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
            if (direction == this.attachmentDirection.getOpposite()) continue;
            BlockState blockState4 = blockState3 = direction == this.attachmentDirection ? this.getBlock().getStateForPlacement(blockPlaceContext) : blockState;
            if (blockState3 == null || !this.canPlace(levelReader, blockState3, blockPos)) continue;
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

