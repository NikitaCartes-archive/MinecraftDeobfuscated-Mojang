/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

public class FaceAttachedHorizontalDirectionalBlock
extends HorizontalDirectionalBlock {
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;

    protected FaceAttachedHorizontalDirectionalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return FaceAttachedHorizontalDirectionalBlock.canAttach(levelReader, blockPos, FaceAttachedHorizontalDirectionalBlock.getConnectedDirection(blockState).getOpposite());
    }

    public static boolean canAttach(LevelReader levelReader, BlockPos blockPos, Direction direction) {
        BlockPos blockPos2 = blockPos.relative(direction);
        return levelReader.getBlockState(blockPos2).isFaceSturdy(levelReader, blockPos2, direction.getOpposite());
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        for (Direction direction : blockPlaceContext.getNearestLookingDirections()) {
            BlockState blockState = direction.getAxis() == Direction.Axis.Y ? (BlockState)((BlockState)this.defaultBlockState().setValue(FACE, direction == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR)).setValue(FACING, blockPlaceContext.getHorizontalDirection()) : (BlockState)((BlockState)this.defaultBlockState().setValue(FACE, AttachFace.WALL)).setValue(FACING, direction.getOpposite());
            if (!blockState.canSurvive(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos())) continue;
            return blockState;
        }
        return null;
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (FaceAttachedHorizontalDirectionalBlock.getConnectedDirection(blockState).getOpposite() == direction && !blockState.canSurvive(levelAccessor, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    protected static Direction getConnectedDirection(BlockState blockState) {
        switch (blockState.getValue(FACE)) {
            case CEILING: {
                return Direction.DOWN;
            }
            case FLOOR: {
                return Direction.UP;
            }
        }
        return blockState.getValue(FACING);
    }
}

