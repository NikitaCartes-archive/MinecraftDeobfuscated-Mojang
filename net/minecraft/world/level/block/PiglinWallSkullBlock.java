/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PiglinWallSkullBlock
extends WallSkullBlock {
    private static final Map<Direction, VoxelShape> AABBS = Maps.immutableEnumMap(Map.of(Direction.NORTH, Block.box(3.0, 4.0, 8.0, 13.0, 12.0, 16.0), Direction.SOUTH, Block.box(3.0, 4.0, 0.0, 13.0, 12.0, 8.0), Direction.EAST, Block.box(0.0, 4.0, 3.0, 8.0, 12.0, 13.0), Direction.WEST, Block.box(8.0, 4.0, 3.0, 16.0, 12.0, 13.0)));

    public PiglinWallSkullBlock(BlockBehaviour.Properties properties) {
        super(SkullBlock.Types.PIGLIN, properties);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return AABBS.get(blockState.getValue(FACING));
    }
}

