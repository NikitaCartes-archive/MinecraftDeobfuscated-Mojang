/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class RedStoneWireBlock
extends Block {
    public static final EnumProperty<RedstoneSide> NORTH = BlockStateProperties.NORTH_REDSTONE;
    public static final EnumProperty<RedstoneSide> EAST = BlockStateProperties.EAST_REDSTONE;
    public static final EnumProperty<RedstoneSide> SOUTH = BlockStateProperties.SOUTH_REDSTONE;
    public static final EnumProperty<RedstoneSide> WEST = BlockStateProperties.WEST_REDSTONE;
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final Map<Direction, EnumProperty<RedstoneSide>> PROPERTY_BY_DIRECTION = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST));
    protected static final VoxelShape[] SHAPE_BY_INDEX = new VoxelShape[]{Block.box(3.0, 0.0, 3.0, 13.0, 1.0, 13.0), Block.box(3.0, 0.0, 3.0, 13.0, 1.0, 16.0), Block.box(0.0, 0.0, 3.0, 13.0, 1.0, 13.0), Block.box(0.0, 0.0, 3.0, 13.0, 1.0, 16.0), Block.box(3.0, 0.0, 0.0, 13.0, 1.0, 13.0), Block.box(3.0, 0.0, 0.0, 13.0, 1.0, 16.0), Block.box(0.0, 0.0, 0.0, 13.0, 1.0, 13.0), Block.box(0.0, 0.0, 0.0, 13.0, 1.0, 16.0), Block.box(3.0, 0.0, 3.0, 16.0, 1.0, 13.0), Block.box(3.0, 0.0, 3.0, 16.0, 1.0, 16.0), Block.box(0.0, 0.0, 3.0, 16.0, 1.0, 13.0), Block.box(0.0, 0.0, 3.0, 16.0, 1.0, 16.0), Block.box(3.0, 0.0, 0.0, 16.0, 1.0, 13.0), Block.box(3.0, 0.0, 0.0, 16.0, 1.0, 16.0), Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 13.0), Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0)};
    private boolean shouldSignal = true;
    private final Set<BlockPos> toUpdate = Sets.newHashSet();

    public RedStoneWireBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(NORTH, RedstoneSide.NONE)).setValue(EAST, RedstoneSide.NONE)).setValue(SOUTH, RedstoneSide.NONE)).setValue(WEST, RedstoneSide.NONE)).setValue(POWER, 0));
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE_BY_INDEX[RedStoneWireBlock.getAABBIndex(blockState)];
    }

    private static int getAABBIndex(BlockState blockState) {
        boolean bl4;
        int i = 0;
        boolean bl = blockState.getValue(NORTH) != RedstoneSide.NONE;
        boolean bl2 = blockState.getValue(EAST) != RedstoneSide.NONE;
        boolean bl3 = blockState.getValue(SOUTH) != RedstoneSide.NONE;
        boolean bl5 = bl4 = blockState.getValue(WEST) != RedstoneSide.NONE;
        if (bl || bl3 && !bl && !bl2 && !bl4) {
            i |= 1 << Direction.NORTH.get2DDataValue();
        }
        if (bl2 || bl4 && !bl && !bl2 && !bl3) {
            i |= 1 << Direction.EAST.get2DDataValue();
        }
        if (bl3 || bl && !bl2 && !bl3 && !bl4) {
            i |= 1 << Direction.SOUTH.get2DDataValue();
        }
        if (bl4 || bl2 && !bl && !bl3 && !bl4) {
            i |= 1 << Direction.WEST.get2DDataValue();
        }
        return i;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Level blockGetter = blockPlaceContext.getLevel();
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        return (BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(WEST, this.getConnectingSide(blockGetter, blockPos, Direction.WEST))).setValue(EAST, this.getConnectingSide(blockGetter, blockPos, Direction.EAST))).setValue(NORTH, this.getConnectingSide(blockGetter, blockPos, Direction.NORTH))).setValue(SOUTH, this.getConnectingSide(blockGetter, blockPos, Direction.SOUTH));
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (direction == Direction.DOWN) {
            return blockState;
        }
        if (direction == Direction.UP) {
            return (BlockState)((BlockState)((BlockState)((BlockState)blockState.setValue(WEST, this.getConnectingSide(levelAccessor, blockPos, Direction.WEST))).setValue(EAST, this.getConnectingSide(levelAccessor, blockPos, Direction.EAST))).setValue(NORTH, this.getConnectingSide(levelAccessor, blockPos, Direction.NORTH))).setValue(SOUTH, this.getConnectingSide(levelAccessor, blockPos, Direction.SOUTH));
        }
        return (BlockState)blockState.setValue(PROPERTY_BY_DIRECTION.get(direction), this.getConnectingSide(levelAccessor, blockPos, direction));
    }

    @Override
    public void updateIndirectNeighbourShapes(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, int i) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            RedstoneSide redstoneSide = (RedstoneSide)blockState.getValue(PROPERTY_BY_DIRECTION.get(direction));
            if (redstoneSide == RedstoneSide.NONE || levelAccessor.getBlockState(mutableBlockPos.setWithOffset(blockPos, direction)).getBlock() == this) continue;
            mutableBlockPos.move(Direction.DOWN);
            BlockState blockState2 = levelAccessor.getBlockState(mutableBlockPos);
            if (blockState2.getBlock() != Blocks.OBSERVER) {
                BlockPos blockPos2 = mutableBlockPos.relative(direction.getOpposite());
                BlockState blockState3 = blockState2.updateShape(direction.getOpposite(), levelAccessor.getBlockState(blockPos2), levelAccessor, mutableBlockPos, blockPos2);
                RedStoneWireBlock.updateOrDestroy(blockState2, blockState3, levelAccessor, mutableBlockPos, i);
            }
            mutableBlockPos.setWithOffset(blockPos, direction).move(Direction.UP);
            BlockState blockState4 = levelAccessor.getBlockState(mutableBlockPos);
            if (blockState4.getBlock() == Blocks.OBSERVER) continue;
            BlockPos blockPos3 = mutableBlockPos.relative(direction.getOpposite());
            BlockState blockState5 = blockState4.updateShape(direction.getOpposite(), levelAccessor.getBlockState(blockPos3), levelAccessor, mutableBlockPos, blockPos3);
            RedStoneWireBlock.updateOrDestroy(blockState4, blockState5, levelAccessor, mutableBlockPos, i);
        }
    }

    private RedstoneSide getConnectingSide(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        BlockPos blockPos2 = blockPos.relative(direction);
        BlockState blockState = blockGetter.getBlockState(blockPos2);
        BlockPos blockPos3 = blockPos.above();
        BlockState blockState2 = blockGetter.getBlockState(blockPos3);
        if (!blockState2.isRedstoneConductor(blockGetter, blockPos3)) {
            boolean bl;
            boolean bl2 = bl = blockState.isFaceSturdy(blockGetter, blockPos2, Direction.UP) || blockState.getBlock() == Blocks.HOPPER;
            if (bl && RedStoneWireBlock.shouldConnectTo(blockGetter.getBlockState(blockPos2.above()))) {
                if (blockState.isCollisionShapeFullBlock(blockGetter, blockPos2)) {
                    return RedstoneSide.UP;
                }
                return RedstoneSide.SIDE;
            }
        }
        if (RedStoneWireBlock.shouldConnectTo(blockState, direction) || !blockState.isRedstoneConductor(blockGetter, blockPos2) && RedStoneWireBlock.shouldConnectTo(blockGetter.getBlockState(blockPos2.below()))) {
            return RedstoneSide.SIDE;
        }
        return RedstoneSide.NONE;
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.below();
        BlockState blockState2 = levelReader.getBlockState(blockPos2);
        return blockState2.isFaceSturdy(levelReader, blockPos2, Direction.UP) || blockState2.getBlock() == Blocks.HOPPER;
    }

    private BlockState updatePowerStrength(Level level, BlockPos blockPos, BlockState blockState) {
        blockState = this.updatePowerStrengthImpl(level, blockPos, blockState);
        ArrayList<BlockPos> list = Lists.newArrayList(this.toUpdate);
        this.toUpdate.clear();
        for (BlockPos blockPos2 : list) {
            level.updateNeighborsAt(blockPos2, this);
        }
        return blockState;
    }

    private BlockState updatePowerStrengthImpl(Level level, BlockPos blockPos, BlockState blockState) {
        int l;
        BlockState blockState2 = blockState;
        int i = blockState2.getValue(POWER);
        this.shouldSignal = false;
        int j = level.getBestNeighborSignal(blockPos);
        this.shouldSignal = true;
        int k = 0;
        if (j < 15) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos blockPos2 = blockPos.relative(direction);
                BlockState blockState3 = level.getBlockState(blockPos2);
                k = this.checkTarget(k, blockState3);
                BlockPos blockPos3 = blockPos.above();
                if (blockState3.isRedstoneConductor(level, blockPos2) && !level.getBlockState(blockPos3).isRedstoneConductor(level, blockPos3)) {
                    k = this.checkTarget(k, level.getBlockState(blockPos2.above()));
                    continue;
                }
                if (blockState3.isRedstoneConductor(level, blockPos2)) continue;
                k = this.checkTarget(k, level.getBlockState(blockPos2.below()));
            }
        }
        if (j > (l = k - 1)) {
            l = j;
        }
        if (i != l) {
            blockState = (BlockState)blockState.setValue(POWER, l);
            if (level.getBlockState(blockPos) == blockState2) {
                level.setBlock(blockPos, blockState, 2);
            }
            this.toUpdate.add(blockPos);
            for (Direction direction2 : Direction.values()) {
                this.toUpdate.add(blockPos.relative(direction2));
            }
        }
        return blockState;
    }

    private void checkCornerChangeAt(Level level, BlockPos blockPos) {
        if (level.getBlockState(blockPos).getBlock() != this) {
            return;
        }
        level.updateNeighborsAt(blockPos, this);
        for (Direction direction : Direction.values()) {
            level.updateNeighborsAt(blockPos.relative(direction), this);
        }
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (blockState2.getBlock() == blockState.getBlock() || level.isClientSide) {
            return;
        }
        this.updatePowerStrength(level, blockPos, blockState);
        for (Direction direction : Direction.Plane.VERTICAL) {
            level.updateNeighborsAt(blockPos.relative(direction), this);
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            this.checkCornerChangeAt(level, blockPos.relative(direction));
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockPos2 = blockPos.relative(direction);
            if (level.getBlockState(blockPos2).isRedstoneConductor(level, blockPos2)) {
                this.checkCornerChangeAt(level, blockPos2.above());
                continue;
            }
            this.checkCornerChangeAt(level, blockPos2.below());
        }
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (bl || blockState.getBlock() == blockState2.getBlock()) {
            return;
        }
        super.onRemove(blockState, level, blockPos, blockState2, bl);
        if (level.isClientSide) {
            return;
        }
        for (Direction direction : Direction.values()) {
            level.updateNeighborsAt(blockPos.relative(direction), this);
        }
        this.updatePowerStrength(level, blockPos, blockState);
        for (Direction direction2 : Direction.Plane.HORIZONTAL) {
            this.checkCornerChangeAt(level, blockPos.relative(direction2));
        }
        for (Direction direction2 : Direction.Plane.HORIZONTAL) {
            BlockPos blockPos2 = blockPos.relative(direction2);
            if (level.getBlockState(blockPos2).isRedstoneConductor(level, blockPos2)) {
                this.checkCornerChangeAt(level, blockPos2.above());
                continue;
            }
            this.checkCornerChangeAt(level, blockPos2.below());
        }
    }

    private int checkTarget(int i, BlockState blockState) {
        if (blockState.getBlock() != this) {
            return i;
        }
        int j = blockState.getValue(POWER);
        if (j > i) {
            return j;
        }
        return i;
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        if (level.isClientSide) {
            return;
        }
        if (blockState.canSurvive(level, blockPos)) {
            this.updatePowerStrength(level, blockPos, blockState);
        } else {
            RedStoneWireBlock.dropResources(blockState, level, blockPos);
            level.removeBlock(blockPos, false);
        }
    }

    @Override
    public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (!this.shouldSignal) {
            return 0;
        }
        return blockState.getSignal(blockGetter, blockPos, direction);
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (!this.shouldSignal) {
            return 0;
        }
        int i = blockState.getValue(POWER);
        if (i == 0) {
            return 0;
        }
        if (direction == Direction.UP) {
            return i;
        }
        EnumSet<Direction> enumSet = EnumSet.noneOf(Direction.class);
        for (Direction direction2 : Direction.Plane.HORIZONTAL) {
            if (!this.isPowerSourceAt(blockGetter, blockPos, direction2)) continue;
            enumSet.add(direction2);
        }
        if (direction.getAxis().isHorizontal() && enumSet.isEmpty()) {
            return i;
        }
        if (enumSet.contains(direction) && !enumSet.contains(direction.getCounterClockWise()) && !enumSet.contains(direction.getClockWise())) {
            return i;
        }
        return 0;
    }

    private boolean isPowerSourceAt(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        BlockPos blockPos2 = blockPos.relative(direction);
        BlockState blockState = blockGetter.getBlockState(blockPos2);
        boolean bl = blockState.isRedstoneConductor(blockGetter, blockPos2);
        BlockPos blockPos3 = blockPos.above();
        boolean bl2 = blockGetter.getBlockState(blockPos3).isRedstoneConductor(blockGetter, blockPos3);
        if (!bl2 && bl && RedStoneWireBlock.shouldConnectTo(blockGetter, blockPos2.above())) {
            return true;
        }
        if (RedStoneWireBlock.shouldConnectTo(blockState, direction)) {
            return true;
        }
        if (blockState.getBlock() == Blocks.REPEATER && blockState.getValue(DiodeBlock.POWERED).booleanValue() && blockState.getValue(DiodeBlock.FACING) == direction) {
            return true;
        }
        return !bl && RedStoneWireBlock.shouldConnectTo(blockGetter, blockPos2.below());
    }

    protected static boolean shouldConnectTo(BlockGetter blockGetter, BlockPos blockPos) {
        return RedStoneWireBlock.shouldConnectTo(blockGetter.getBlockState(blockPos));
    }

    protected static boolean shouldConnectTo(BlockState blockState) {
        return RedStoneWireBlock.shouldConnectTo(blockState, null);
    }

    protected static boolean shouldConnectTo(BlockState blockState, @Nullable Direction direction) {
        Block block = blockState.getBlock();
        if (block == Blocks.REDSTONE_WIRE) {
            return true;
        }
        if (blockState.getBlock() == Blocks.REPEATER) {
            Direction direction2 = blockState.getValue(RepeaterBlock.FACING);
            return direction2 == direction || direction2.getOpposite() == direction;
        }
        if (Blocks.OBSERVER == blockState.getBlock()) {
            return direction == blockState.getValue(ObserverBlock.FACING);
        }
        return blockState.isSignalSource() && direction != null;
    }

    @Override
    public boolean isSignalSource(BlockState blockState) {
        return this.shouldSignal;
    }

    @Environment(value=EnvType.CLIENT)
    public static int getColorForData(int i) {
        float f = (float)i / 15.0f;
        float g = f * 0.6f + 0.4f;
        if (i == 0) {
            g = 0.3f;
        }
        float h = f * f * 0.7f - 0.5f;
        float j = f * f * 0.6f - 0.7f;
        if (h < 0.0f) {
            h = 0.0f;
        }
        if (j < 0.0f) {
            j = 0.0f;
        }
        int k = Mth.clamp((int)(g * 255.0f), 0, 255);
        int l = Mth.clamp((int)(h * 255.0f), 0, 255);
        int m = Mth.clamp((int)(j * 255.0f), 0, 255);
        return 0xFF000000 | k << 16 | l << 8 | m;
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        int i = blockState.getValue(POWER);
        if (i == 0) {
            return;
        }
        double d = (double)blockPos.getX() + 0.5 + ((double)random.nextFloat() - 0.5) * 0.2;
        double e = (float)blockPos.getY() + 0.0625f;
        double f = (double)blockPos.getZ() + 0.5 + ((double)random.nextFloat() - 0.5) * 0.2;
        float g = (float)i / 15.0f;
        float h = g * 0.6f + 0.4f;
        float j = Math.max(0.0f, g * g * 0.7f - 0.5f);
        float k = Math.max(0.0f, g * g * 0.6f - 0.7f);
        level.addParticle(new DustParticleOptions(h, j, k, 1.0f), d, e, f, 0.0, 0.0, 0.0);
    }

    @Override
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        switch (rotation) {
            case CLOCKWISE_180: {
                return (BlockState)((BlockState)((BlockState)((BlockState)blockState.setValue(NORTH, blockState.getValue(SOUTH))).setValue(EAST, blockState.getValue(WEST))).setValue(SOUTH, blockState.getValue(NORTH))).setValue(WEST, blockState.getValue(EAST));
            }
            case COUNTERCLOCKWISE_90: {
                return (BlockState)((BlockState)((BlockState)((BlockState)blockState.setValue(NORTH, blockState.getValue(EAST))).setValue(EAST, blockState.getValue(SOUTH))).setValue(SOUTH, blockState.getValue(WEST))).setValue(WEST, blockState.getValue(NORTH));
            }
            case CLOCKWISE_90: {
                return (BlockState)((BlockState)((BlockState)((BlockState)blockState.setValue(NORTH, blockState.getValue(WEST))).setValue(EAST, blockState.getValue(NORTH))).setValue(SOUTH, blockState.getValue(EAST))).setValue(WEST, blockState.getValue(SOUTH));
            }
        }
        return blockState;
    }

    @Override
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        switch (mirror) {
            case LEFT_RIGHT: {
                return (BlockState)((BlockState)blockState.setValue(NORTH, blockState.getValue(SOUTH))).setValue(SOUTH, blockState.getValue(NORTH));
            }
            case FRONT_BACK: {
                return (BlockState)((BlockState)blockState.setValue(EAST, blockState.getValue(WEST))).setValue(WEST, blockState.getValue(EAST));
            }
        }
        return super.mirror(blockState, mirror);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, POWER);
    }
}

