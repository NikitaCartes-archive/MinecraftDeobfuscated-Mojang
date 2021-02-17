/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.piston;

import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.piston.PistonMath;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PistonMovingBlockEntity
extends BlockEntity {
    private BlockState movedState;
    private Direction direction;
    private boolean extending;
    private boolean isSourcePiston;
    private static final ThreadLocal<Direction> NOCLIP = ThreadLocal.withInitial(() -> null);
    private float progress;
    private float progressO;
    private long lastTicked;
    private int deathTicks;

    public PistonMovingBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.PISTON, blockPos, blockState);
    }

    public PistonMovingBlockEntity(BlockPos blockPos, BlockState blockState, BlockState blockState2, Direction direction, boolean bl, boolean bl2) {
        this(blockPos, blockState);
        this.movedState = blockState2;
        this.direction = direction;
        this.extending = bl;
        this.isSourcePiston = bl2;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }

    public boolean isExtending() {
        return this.extending;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public boolean isSourcePiston() {
        return this.isSourcePiston;
    }

    public float getProgress(float f) {
        if (f > 1.0f) {
            f = 1.0f;
        }
        return Mth.lerp(f, this.progressO, this.progress);
    }

    @Environment(value=EnvType.CLIENT)
    public float getXOff(float f) {
        return (float)this.direction.getStepX() * this.getExtendedProgress(this.getProgress(f));
    }

    @Environment(value=EnvType.CLIENT)
    public float getYOff(float f) {
        return (float)this.direction.getStepY() * this.getExtendedProgress(this.getProgress(f));
    }

    @Environment(value=EnvType.CLIENT)
    public float getZOff(float f) {
        return (float)this.direction.getStepZ() * this.getExtendedProgress(this.getProgress(f));
    }

    private float getExtendedProgress(float f) {
        return this.extending ? f - 1.0f : 1.0f - f;
    }

    private BlockState getCollisionRelatedBlockState() {
        if (!this.isExtending() && this.isSourcePiston() && this.movedState.getBlock() instanceof PistonBaseBlock) {
            return (BlockState)((BlockState)((BlockState)Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.SHORT, this.progress > 0.25f)).setValue(PistonHeadBlock.TYPE, this.movedState.is(Blocks.STICKY_PISTON) ? PistonType.STICKY : PistonType.DEFAULT)).setValue(PistonHeadBlock.FACING, this.movedState.getValue(PistonBaseBlock.FACING));
        }
        return this.movedState;
    }

    private static void moveCollidedEntities(Level level, BlockPos blockPos, float f, PistonMovingBlockEntity pistonMovingBlockEntity) {
        Direction direction = pistonMovingBlockEntity.getMovementDirection();
        double d = f - pistonMovingBlockEntity.progress;
        VoxelShape voxelShape = pistonMovingBlockEntity.getCollisionRelatedBlockState().getCollisionShape(level, blockPos);
        if (voxelShape.isEmpty()) {
            return;
        }
        AABB aABB = PistonMovingBlockEntity.moveByPositionAndProgress(blockPos, voxelShape.bounds(), pistonMovingBlockEntity);
        List<Entity> list = level.getEntities(null, PistonMath.getMovementArea(aABB, direction, d).minmax(aABB));
        if (list.isEmpty()) {
            return;
        }
        List<AABB> list2 = voxelShape.toAabbs();
        boolean bl = pistonMovingBlockEntity.movedState.is(Blocks.SLIME_BLOCK);
        for (Entity entity : list) {
            AABB aABB4;
            AABB aABB2;
            AABB aABB3;
            if (entity.getPistonPushReaction() == PushReaction.IGNORE) continue;
            if (bl) {
                if (entity instanceof ServerPlayer) continue;
                Vec3 vec3 = entity.getDeltaMovement();
                double e = vec3.x;
                double g = vec3.y;
                double h = vec3.z;
                switch (direction.getAxis()) {
                    case X: {
                        e = direction.getStepX();
                        break;
                    }
                    case Y: {
                        g = direction.getStepY();
                        break;
                    }
                    case Z: {
                        h = direction.getStepZ();
                    }
                }
                entity.setDeltaMovement(e, g, h);
            }
            double i = 0.0;
            Iterator<AABB> iterator = list2.iterator();
            while (!(!iterator.hasNext() || (aABB3 = PistonMath.getMovementArea(PistonMovingBlockEntity.moveByPositionAndProgress(blockPos, aABB2 = iterator.next(), pistonMovingBlockEntity), direction, d)).intersects(aABB4 = entity.getBoundingBox()) && (i = Math.max(i, PistonMovingBlockEntity.getMovement(aABB3, direction, aABB4))) >= d)) {
            }
            if (i <= 0.0) continue;
            i = Math.min(i, d) + 0.01;
            PistonMovingBlockEntity.moveEntityByPiston(direction, entity, i, direction);
            if (pistonMovingBlockEntity.extending || !pistonMovingBlockEntity.isSourcePiston) continue;
            PistonMovingBlockEntity.fixEntityWithinPistonBase(blockPos, entity, direction, d);
        }
    }

    private static void moveEntityByPiston(Direction direction, Entity entity, double d, Direction direction2) {
        NOCLIP.set(direction);
        entity.move(MoverType.PISTON, new Vec3(d * (double)direction2.getStepX(), d * (double)direction2.getStepY(), d * (double)direction2.getStepZ()));
        NOCLIP.set(null);
    }

    private static void moveStuckEntities(Level level, BlockPos blockPos, float f, PistonMovingBlockEntity pistonMovingBlockEntity) {
        if (!pistonMovingBlockEntity.isStickyForEntities()) {
            return;
        }
        Direction direction = pistonMovingBlockEntity.getMovementDirection();
        if (!direction.getAxis().isHorizontal()) {
            return;
        }
        double d = pistonMovingBlockEntity.movedState.getCollisionShape(level, blockPos).max(Direction.Axis.Y);
        AABB aABB = PistonMovingBlockEntity.moveByPositionAndProgress(blockPos, new AABB(0.0, d, 0.0, 1.0, 1.5000000999999998, 1.0), pistonMovingBlockEntity);
        double e = f - pistonMovingBlockEntity.progress;
        List<Entity> list = level.getEntities((Entity)null, aABB, entity -> PistonMovingBlockEntity.matchesStickyCritera(aABB, entity));
        for (Entity entity2 : list) {
            PistonMovingBlockEntity.moveEntityByPiston(direction, entity2, e, direction);
        }
    }

    private static boolean matchesStickyCritera(AABB aABB, Entity entity) {
        return entity.getPistonPushReaction() == PushReaction.NORMAL && entity.isOnGround() && entity.getX() >= aABB.minX && entity.getX() <= aABB.maxX && entity.getZ() >= aABB.minZ && entity.getZ() <= aABB.maxZ;
    }

    private boolean isStickyForEntities() {
        return this.movedState.is(Blocks.HONEY_BLOCK);
    }

    public Direction getMovementDirection() {
        return this.extending ? this.direction : this.direction.getOpposite();
    }

    private static double getMovement(AABB aABB, Direction direction, AABB aABB2) {
        switch (direction) {
            case EAST: {
                return aABB.maxX - aABB2.minX;
            }
            case WEST: {
                return aABB2.maxX - aABB.minX;
            }
            default: {
                return aABB.maxY - aABB2.minY;
            }
            case DOWN: {
                return aABB2.maxY - aABB.minY;
            }
            case SOUTH: {
                return aABB.maxZ - aABB2.minZ;
            }
            case NORTH: 
        }
        return aABB2.maxZ - aABB.minZ;
    }

    private static AABB moveByPositionAndProgress(BlockPos blockPos, AABB aABB, PistonMovingBlockEntity pistonMovingBlockEntity) {
        double d = pistonMovingBlockEntity.getExtendedProgress(pistonMovingBlockEntity.progress);
        return aABB.move((double)blockPos.getX() + d * (double)pistonMovingBlockEntity.direction.getStepX(), (double)blockPos.getY() + d * (double)pistonMovingBlockEntity.direction.getStepY(), (double)blockPos.getZ() + d * (double)pistonMovingBlockEntity.direction.getStepZ());
    }

    private static void fixEntityWithinPistonBase(BlockPos blockPos, Entity entity, Direction direction, double d) {
        double f;
        Direction direction2;
        double e;
        AABB aABB2;
        AABB aABB = entity.getBoundingBox();
        if (aABB.intersects(aABB2 = Shapes.block().bounds().move(blockPos)) && Math.abs((e = PistonMovingBlockEntity.getMovement(aABB2, direction2 = direction.getOpposite(), aABB) + 0.01) - (f = PistonMovingBlockEntity.getMovement(aABB2, direction2, aABB.intersect(aABB2)) + 0.01)) < 0.01) {
            e = Math.min(e, d) + 0.01;
            PistonMovingBlockEntity.moveEntityByPiston(direction, entity, e, direction2);
        }
    }

    public BlockState getMovedState() {
        return this.movedState;
    }

    public void finalTick() {
        if (this.level != null && (this.progressO < 1.0f || this.level.isClientSide)) {
            this.progressO = this.progress = 1.0f;
            this.level.removeBlockEntity(this.worldPosition);
            this.setRemoved();
            if (this.level.getBlockState(this.worldPosition).is(Blocks.MOVING_PISTON)) {
                BlockState blockState = this.isSourcePiston ? Blocks.AIR.defaultBlockState() : Block.updateFromNeighbourShapes(this.movedState, this.level, this.worldPosition);
                this.level.setBlock(this.worldPosition, blockState, 3);
                this.level.neighborChanged(this.worldPosition, blockState.getBlock(), this.worldPosition);
            }
        }
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, PistonMovingBlockEntity pistonMovingBlockEntity) {
        pistonMovingBlockEntity.lastTicked = level.getGameTime();
        pistonMovingBlockEntity.progressO = pistonMovingBlockEntity.progress;
        if (pistonMovingBlockEntity.progressO >= 1.0f) {
            if (level.isClientSide && pistonMovingBlockEntity.deathTicks < 5) {
                ++pistonMovingBlockEntity.deathTicks;
                return;
            }
            level.removeBlockEntity(blockPos);
            pistonMovingBlockEntity.setRemoved();
            if (pistonMovingBlockEntity.movedState != null && level.getBlockState(blockPos).is(Blocks.MOVING_PISTON)) {
                BlockState blockState2 = Block.updateFromNeighbourShapes(pistonMovingBlockEntity.movedState, level, blockPos);
                if (blockState2.isAir()) {
                    level.setBlock(blockPos, pistonMovingBlockEntity.movedState, 84);
                    Block.updateOrDestroy(pistonMovingBlockEntity.movedState, blockState2, level, blockPos, 3);
                } else {
                    if (blockState2.hasProperty(BlockStateProperties.WATERLOGGED) && blockState2.getValue(BlockStateProperties.WATERLOGGED).booleanValue()) {
                        blockState2 = (BlockState)blockState2.setValue(BlockStateProperties.WATERLOGGED, false);
                    }
                    level.setBlock(blockPos, blockState2, 67);
                    level.neighborChanged(blockPos, blockState2.getBlock(), blockPos);
                }
            }
            return;
        }
        float f = pistonMovingBlockEntity.progress + 0.5f;
        PistonMovingBlockEntity.moveCollidedEntities(level, blockPos, f, pistonMovingBlockEntity);
        PistonMovingBlockEntity.moveStuckEntities(level, blockPos, f, pistonMovingBlockEntity);
        pistonMovingBlockEntity.progress = f;
        if (pistonMovingBlockEntity.progress >= 1.0f) {
            pistonMovingBlockEntity.progress = 1.0f;
        }
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        this.movedState = NbtUtils.readBlockState(compoundTag.getCompound("blockState"));
        this.direction = Direction.from3DDataValue(compoundTag.getInt("facing"));
        this.progressO = this.progress = compoundTag.getFloat("progress");
        this.extending = compoundTag.getBoolean("extending");
        this.isSourcePiston = compoundTag.getBoolean("source");
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        compoundTag.put("blockState", NbtUtils.writeBlockState(this.movedState));
        compoundTag.putInt("facing", this.direction.get3DDataValue());
        compoundTag.putFloat("progress", this.progressO);
        compoundTag.putBoolean("extending", this.extending);
        compoundTag.putBoolean("source", this.isSourcePiston);
        return compoundTag;
    }

    public VoxelShape getCollisionShape(BlockGetter blockGetter, BlockPos blockPos) {
        VoxelShape voxelShape = !this.extending && this.isSourcePiston ? ((BlockState)this.movedState.setValue(PistonBaseBlock.EXTENDED, true)).getCollisionShape(blockGetter, blockPos) : Shapes.empty();
        Direction direction = NOCLIP.get();
        if ((double)this.progress < 1.0 && direction == this.getMovementDirection()) {
            return voxelShape;
        }
        BlockState blockState = this.isSourcePiston() ? (BlockState)((BlockState)Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.FACING, this.direction)).setValue(PistonHeadBlock.SHORT, this.extending != 1.0f - this.progress < 0.25f) : this.movedState;
        float f = this.getExtendedProgress(this.progress);
        double d = (float)this.direction.getStepX() * f;
        double e = (float)this.direction.getStepY() * f;
        double g = (float)this.direction.getStepZ() * f;
        return Shapes.or(voxelShape, blockState.getCollisionShape(blockGetter, blockPos).move(d, e, g));
    }

    public long getLastTicked() {
        return this.lastTicked;
    }
}

