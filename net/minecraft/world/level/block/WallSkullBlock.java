/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WallSkullBlock
extends AbstractSkullBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final Map<Direction, VoxelShape> AABBS = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Block.box(4.0, 4.0, 8.0, 12.0, 12.0, 16.0), Direction.SOUTH, Block.box(4.0, 4.0, 0.0, 12.0, 12.0, 8.0), Direction.EAST, Block.box(0.0, 4.0, 4.0, 8.0, 12.0, 12.0), Direction.WEST, Block.box(8.0, 4.0, 4.0, 16.0, 12.0, 12.0)));

    protected WallSkullBlock(SkullBlock.Type type, Block.Properties properties) {
        super(type, properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH));
    }

    @Override
    public String getDescriptionId() {
        return this.asItem().getDescriptionId();
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return AABBS.get(blockState.getValue(FACING));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Direction[] directions;
        BlockState blockState = this.defaultBlockState();
        Level blockGetter = blockPlaceContext.getLevel();
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        for (Direction direction : directions = blockPlaceContext.getNearestLookingDirections()) {
            if (!direction.getAxis().isHorizontal()) continue;
            Direction direction2 = direction.getOpposite();
            blockState = (BlockState)blockState.setValue(FACING, direction2);
            if (blockGetter.getBlockState(blockPos.relative(direction)).canBeReplaced(blockPlaceContext)) continue;
            return blockState;
        }
        return null;
    }

    @Override
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState)blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}

