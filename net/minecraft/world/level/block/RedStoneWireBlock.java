/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
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
    protected static final int H = 1;
    protected static final int W = 3;
    protected static final int E = 13;
    protected static final int N = 3;
    protected static final int S = 13;
    private static final VoxelShape SHAPE_DOT = Block.box(3.0, 0.0, 3.0, 13.0, 1.0, 13.0);
    private static final Map<Direction, VoxelShape> SHAPES_FLOOR = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Block.box(3.0, 0.0, 0.0, 13.0, 1.0, 13.0), Direction.SOUTH, Block.box(3.0, 0.0, 3.0, 13.0, 1.0, 16.0), Direction.EAST, Block.box(3.0, 0.0, 3.0, 16.0, 1.0, 13.0), Direction.WEST, Block.box(0.0, 0.0, 3.0, 13.0, 1.0, 13.0)));
    private static final Map<Direction, VoxelShape> SHAPES_UP = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Shapes.or(SHAPES_FLOOR.get(Direction.NORTH), Block.box(3.0, 0.0, 0.0, 13.0, 16.0, 1.0)), Direction.SOUTH, Shapes.or(SHAPES_FLOOR.get(Direction.SOUTH), Block.box(3.0, 0.0, 15.0, 13.0, 16.0, 16.0)), Direction.EAST, Shapes.or(SHAPES_FLOOR.get(Direction.EAST), Block.box(15.0, 0.0, 3.0, 16.0, 16.0, 13.0)), Direction.WEST, Shapes.or(SHAPES_FLOOR.get(Direction.WEST), Block.box(0.0, 0.0, 3.0, 1.0, 16.0, 13.0))));
    private static final Map<BlockState, VoxelShape> SHAPES_CACHE = Maps.newHashMap();
    private static final Vec3[] COLORS = Util.make(new Vec3[16], vec3s -> {
        for (int i = 0; i <= 15; ++i) {
            float f;
            float g = f * 0.6f + ((f = (float)i / 15.0f) > 0.0f ? 0.4f : 0.3f);
            float h = Mth.clamp(f * f * 0.7f - 0.5f, 0.0f, 1.0f);
            float j = Mth.clamp(f * f * 0.6f - 0.7f, 0.0f, 1.0f);
            vec3s[i] = new Vec3(g, h, j);
        }
    });
    private static final float PARTICLE_DENSITY = 0.2f;
    private final BlockState crossState;
    private boolean shouldSignal = true;

    public RedStoneWireBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(NORTH, RedstoneSide.NONE)).setValue(EAST, RedstoneSide.NONE)).setValue(SOUTH, RedstoneSide.NONE)).setValue(WEST, RedstoneSide.NONE)).setValue(POWER, 0));
        this.crossState = (BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(NORTH, RedstoneSide.SIDE)).setValue(EAST, RedstoneSide.SIDE)).setValue(SOUTH, RedstoneSide.SIDE)).setValue(WEST, RedstoneSide.SIDE);
        for (BlockState blockState : this.getStateDefinition().getPossibleStates()) {
            if (blockState.getValue(POWER) != 0) continue;
            SHAPES_CACHE.put(blockState, this.calculateShape(blockState));
        }
    }

    private VoxelShape calculateShape(BlockState blockState) {
        VoxelShape voxelShape = SHAPE_DOT;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            RedstoneSide redstoneSide = (RedstoneSide)blockState.getValue(PROPERTY_BY_DIRECTION.get(direction));
            if (redstoneSide == RedstoneSide.SIDE) {
                voxelShape = Shapes.or(voxelShape, SHAPES_FLOOR.get(direction));
                continue;
            }
            if (redstoneSide != RedstoneSide.UP) continue;
            voxelShape = Shapes.or(voxelShape, SHAPES_UP.get(direction));
        }
        return voxelShape;
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPES_CACHE.get(blockState.setValue(POWER, 0));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return this.getConnectionState(blockPlaceContext.getLevel(), this.crossState, blockPlaceContext.getClickedPos());
    }

    private BlockState getConnectionState(BlockGetter blockGetter, BlockState blockState, BlockPos blockPos) {
        boolean bl7;
        boolean bl = RedStoneWireBlock.isDot(blockState);
        blockState = this.getMissingConnections(blockGetter, (BlockState)this.defaultBlockState().setValue(POWER, blockState.getValue(POWER)), blockPos);
        if (bl && RedStoneWireBlock.isDot(blockState)) {
            return blockState;
        }
        boolean bl2 = blockState.getValue(NORTH).isConnected();
        boolean bl3 = blockState.getValue(SOUTH).isConnected();
        boolean bl4 = blockState.getValue(EAST).isConnected();
        boolean bl5 = blockState.getValue(WEST).isConnected();
        boolean bl6 = !bl2 && !bl3;
        boolean bl8 = bl7 = !bl4 && !bl5;
        if (!bl5 && bl6) {
            blockState = (BlockState)blockState.setValue(WEST, RedstoneSide.SIDE);
        }
        if (!bl4 && bl6) {
            blockState = (BlockState)blockState.setValue(EAST, RedstoneSide.SIDE);
        }
        if (!bl2 && bl7) {
            blockState = (BlockState)blockState.setValue(NORTH, RedstoneSide.SIDE);
        }
        if (!bl3 && bl7) {
            blockState = (BlockState)blockState.setValue(SOUTH, RedstoneSide.SIDE);
        }
        return blockState;
    }

    private BlockState getMissingConnections(BlockGetter blockGetter, BlockState blockState, BlockPos blockPos) {
        boolean bl = !blockGetter.getBlockState(blockPos.above()).isRedstoneConductor(blockGetter, blockPos);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (((RedstoneSide)blockState.getValue(PROPERTY_BY_DIRECTION.get(direction))).isConnected()) continue;
            RedstoneSide redstoneSide = this.getConnectingSide(blockGetter, blockPos, direction, bl);
            blockState = (BlockState)blockState.setValue(PROPERTY_BY_DIRECTION.get(direction), redstoneSide);
        }
        return blockState;
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (direction == Direction.DOWN) {
            return blockState;
        }
        if (direction == Direction.UP) {
            return this.getConnectionState(levelAccessor, blockState, blockPos);
        }
        RedstoneSide redstoneSide = this.getConnectingSide(levelAccessor, blockPos, direction);
        if (redstoneSide.isConnected() == ((RedstoneSide)blockState.getValue(PROPERTY_BY_DIRECTION.get(direction))).isConnected() && !RedStoneWireBlock.isCross(blockState)) {
            return (BlockState)blockState.setValue(PROPERTY_BY_DIRECTION.get(direction), redstoneSide);
        }
        return this.getConnectionState(levelAccessor, (BlockState)((BlockState)this.crossState.setValue(POWER, blockState.getValue(POWER))).setValue(PROPERTY_BY_DIRECTION.get(direction), redstoneSide), blockPos);
    }

    private static boolean isCross(BlockState blockState) {
        return blockState.getValue(NORTH).isConnected() && blockState.getValue(SOUTH).isConnected() && blockState.getValue(EAST).isConnected() && blockState.getValue(WEST).isConnected();
    }

    private static boolean isDot(BlockState blockState) {
        return !blockState.getValue(NORTH).isConnected() && !blockState.getValue(SOUTH).isConnected() && !blockState.getValue(EAST).isConnected() && !blockState.getValue(WEST).isConnected();
    }

    @Override
    public void updateIndirectNeighbourShapes(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, int i, int j) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            RedstoneSide redstoneSide = (RedstoneSide)blockState.getValue(PROPERTY_BY_DIRECTION.get(direction));
            if (redstoneSide == RedstoneSide.NONE || levelAccessor.getBlockState(mutableBlockPos.setWithOffset((Vec3i)blockPos, direction)).is(this)) continue;
            mutableBlockPos.move(Direction.DOWN);
            BlockState blockState2 = levelAccessor.getBlockState(mutableBlockPos);
            if (blockState2.is(this)) {
                Vec3i blockPos2 = mutableBlockPos.relative(direction.getOpposite());
                levelAccessor.neighborShapeChanged(direction.getOpposite(), levelAccessor.getBlockState((BlockPos)blockPos2), mutableBlockPos, (BlockPos)blockPos2, i, j);
            }
            mutableBlockPos.setWithOffset((Vec3i)blockPos, direction).move(Direction.UP);
            BlockState blockState3 = levelAccessor.getBlockState(mutableBlockPos);
            if (!blockState3.is(this)) continue;
            Vec3i blockPos3 = mutableBlockPos.relative(direction.getOpposite());
            levelAccessor.neighborShapeChanged(direction.getOpposite(), levelAccessor.getBlockState((BlockPos)blockPos3), mutableBlockPos, (BlockPos)blockPos3, i, j);
        }
    }

    private RedstoneSide getConnectingSide(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return this.getConnectingSide(blockGetter, blockPos, direction, !blockGetter.getBlockState(blockPos.above()).isRedstoneConductor(blockGetter, blockPos));
    }

    private RedstoneSide getConnectingSide(BlockGetter blockGetter, BlockPos blockPos, Direction direction, boolean bl) {
        boolean bl2;
        BlockPos blockPos2 = blockPos.relative(direction);
        BlockState blockState = blockGetter.getBlockState(blockPos2);
        if (bl && (bl2 = this.canSurviveOn(blockGetter, blockPos2, blockState)) && RedStoneWireBlock.shouldConnectTo(blockGetter.getBlockState(blockPos2.above()))) {
            if (blockState.isFaceSturdy(blockGetter, blockPos2, direction.getOpposite())) {
                return RedstoneSide.UP;
            }
            return RedstoneSide.SIDE;
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
        return this.canSurviveOn(levelReader, blockPos2, blockState2);
    }

    private boolean canSurviveOn(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        return blockState.isFaceSturdy(blockGetter, blockPos, Direction.UP) || blockState.is(Blocks.HOPPER);
    }

    private void updatePowerStrength(Level level, BlockPos blockPos, BlockState blockState) {
        int i = this.calculateTargetStrength(level, blockPos);
        if (blockState.getValue(POWER) != i) {
            if (level.getBlockState(blockPos) == blockState) {
                level.setBlock(blockPos, (BlockState)blockState.setValue(POWER, i), 2);
            }
            HashSet<BlockPos> set = Sets.newHashSet();
            set.add(blockPos);
            for (Direction direction : Direction.values()) {
                set.add(blockPos.relative(direction));
            }
            for (BlockPos blockPos2 : set) {
                level.updateNeighborsAt(blockPos2, this);
            }
        }
    }

    private int calculateTargetStrength(Level level, BlockPos blockPos) {
        this.shouldSignal = false;
        int i = level.getBestNeighborSignal(blockPos);
        this.shouldSignal = true;
        int j = 0;
        if (i < 15) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos blockPos2 = blockPos.relative(direction);
                BlockState blockState = level.getBlockState(blockPos2);
                j = Math.max(j, this.getWireSignal(blockState));
                BlockPos blockPos3 = blockPos.above();
                if (blockState.isRedstoneConductor(level, blockPos2) && !level.getBlockState(blockPos3).isRedstoneConductor(level, blockPos3)) {
                    j = Math.max(j, this.getWireSignal(level.getBlockState(blockPos2.above())));
                    continue;
                }
                if (blockState.isRedstoneConductor(level, blockPos2)) continue;
                j = Math.max(j, this.getWireSignal(level.getBlockState(blockPos2.below())));
            }
        }
        return Math.max(i, j - 1);
    }

    private int getWireSignal(BlockState blockState) {
        return blockState.is(this) ? blockState.getValue(POWER) : 0;
    }

    private void checkCornerChangeAt(Level level, BlockPos blockPos) {
        if (!level.getBlockState(blockPos).is(this)) {
            return;
        }
        level.updateNeighborsAt(blockPos, this);
        for (Direction direction : Direction.values()) {
            level.updateNeighborsAt(blockPos.relative(direction), this);
        }
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (blockState2.is(blockState.getBlock()) || level.isClientSide) {
            return;
        }
        this.updatePowerStrength(level, blockPos, blockState);
        for (Direction direction : Direction.Plane.VERTICAL) {
            level.updateNeighborsAt(blockPos.relative(direction), this);
        }
        this.updateNeighborsOfNeighboringWires(level, blockPos);
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (bl || blockState.is(blockState2.getBlock())) {
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
        this.updateNeighborsOfNeighboringWires(level, blockPos);
    }

    private void updateNeighborsOfNeighboringWires(Level level, BlockPos blockPos) {
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
        if (!this.shouldSignal || direction == Direction.DOWN) {
            return 0;
        }
        int i = blockState.getValue(POWER);
        if (i == 0) {
            return 0;
        }
        if (direction == Direction.UP || ((RedstoneSide)this.getConnectionState(blockGetter, blockState, blockPos).getValue(PROPERTY_BY_DIRECTION.get(direction.getOpposite()))).isConnected()) {
            return i;
        }
        return 0;
    }

    protected static boolean shouldConnectTo(BlockState blockState) {
        return RedStoneWireBlock.shouldConnectTo(blockState, null);
    }

    protected static boolean shouldConnectTo(BlockState blockState, @Nullable Direction direction) {
        if (blockState.is(Blocks.REDSTONE_WIRE)) {
            return true;
        }
        if (blockState.is(Blocks.REPEATER)) {
            Direction direction2 = blockState.getValue(RepeaterBlock.FACING);
            return direction2 == direction || direction2.getOpposite() == direction;
        }
        if (blockState.is(Blocks.OBSERVER)) {
            return direction == blockState.getValue(ObserverBlock.FACING);
        }
        return blockState.isSignalSource() && direction != null;
    }

    @Override
    public boolean isSignalSource(BlockState blockState) {
        return this.shouldSignal;
    }

    public static int getColorForPower(int i) {
        Vec3 vec3 = COLORS[i];
        return Mth.color((float)vec3.x(), (float)vec3.y(), (float)vec3.z());
    }

    private void spawnParticlesAlongLine(Level level, RandomSource randomSource, BlockPos blockPos, Vec3 vec3, Direction direction, Direction direction2, float f, float g) {
        float h = g - f;
        if (randomSource.nextFloat() >= 0.2f * h) {
            return;
        }
        float i = 0.4375f;
        float j = f + h * randomSource.nextFloat();
        double d = 0.5 + (double)(0.4375f * (float)direction.getStepX()) + (double)(j * (float)direction2.getStepX());
        double e = 0.5 + (double)(0.4375f * (float)direction.getStepY()) + (double)(j * (float)direction2.getStepY());
        double k = 0.5 + (double)(0.4375f * (float)direction.getStepZ()) + (double)(j * (float)direction2.getStepZ());
        level.addParticle(new DustParticleOptions(vec3.toVector3f(), 1.0f), (double)blockPos.getX() + d, (double)blockPos.getY() + e, (double)blockPos.getZ() + k, 0.0, 0.0, 0.0);
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        int i = blockState.getValue(POWER);
        if (i == 0) {
            return;
        }
        block4: for (Direction direction : Direction.Plane.HORIZONTAL) {
            RedstoneSide redstoneSide = (RedstoneSide)blockState.getValue(PROPERTY_BY_DIRECTION.get(direction));
            switch (redstoneSide) {
                case UP: {
                    this.spawnParticlesAlongLine(level, randomSource, blockPos, COLORS[i], direction, Direction.UP, -0.5f, 0.5f);
                }
                case SIDE: {
                    this.spawnParticlesAlongLine(level, randomSource, blockPos, COLORS[i], Direction.DOWN, direction, 0.0f, 0.5f);
                    continue block4;
                }
            }
            this.spawnParticlesAlongLine(level, randomSource, blockPos, COLORS[i], Direction.DOWN, direction, 0.0f, 0.3f);
        }
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

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (!player.getAbilities().mayBuild) {
            return InteractionResult.PASS;
        }
        if (RedStoneWireBlock.isCross(blockState) || RedStoneWireBlock.isDot(blockState)) {
            BlockState blockState2 = RedStoneWireBlock.isCross(blockState) ? this.defaultBlockState() : this.crossState;
            blockState2 = (BlockState)blockState2.setValue(POWER, blockState.getValue(POWER));
            if ((blockState2 = this.getConnectionState(level, blockState2, blockPos)) != blockState) {
                level.setBlock(blockPos, blockState2, 3);
                this.updatesOnShapeChange(level, blockPos, blockState, blockState2);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    private void updatesOnShapeChange(Level level, BlockPos blockPos, BlockState blockState, BlockState blockState2) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockPos2 = blockPos.relative(direction);
            if (((RedstoneSide)blockState.getValue(PROPERTY_BY_DIRECTION.get(direction))).isConnected() == ((RedstoneSide)blockState2.getValue(PROPERTY_BY_DIRECTION.get(direction))).isConnected() || !level.getBlockState(blockPos2).isRedstoneConductor(level, blockPos2)) continue;
            level.updateNeighborsAtExceptFromFacing(blockPos2, blockState2.getBlock(), direction.getOpposite());
        }
    }
}

