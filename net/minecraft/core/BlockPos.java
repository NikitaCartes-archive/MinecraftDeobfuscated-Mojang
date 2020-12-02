/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import com.google.common.collect.AbstractIterator;
import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.Util;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Unmodifiable;

@Unmodifiable
public class BlockPos
extends Vec3i {
    public static final Codec<BlockPos> CODEC = Codec.INT_STREAM.comapFlatMap(intStream -> Util.fixedSize(intStream, 3).map(is -> new BlockPos(is[0], is[1], is[2])), blockPos -> IntStream.of(blockPos.getX(), blockPos.getY(), blockPos.getZ())).stable();
    private static final Logger LOGGER = LogManager.getLogger();
    public static final BlockPos ZERO = new BlockPos(0, 0, 0);
    private static final int PACKED_X_LENGTH;
    private static final int PACKED_Z_LENGTH;
    public static final int PACKED_Y_LENGTH;
    private static final long PACKED_X_MASK;
    private static final long PACKED_Y_MASK;
    private static final long PACKED_Z_MASK;
    private static final int Z_OFFSET;
    private static final int X_OFFSET;

    public BlockPos(int i, int j, int k) {
        super(i, j, k);
    }

    public BlockPos(double d, double e, double f) {
        super(d, e, f);
    }

    public BlockPos(Vec3 vec3) {
        this(vec3.x, vec3.y, vec3.z);
    }

    public BlockPos(Position position) {
        this(position.x(), position.y(), position.z());
    }

    public BlockPos(Vec3i vec3i) {
        this(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    public static long offset(long l, Direction direction) {
        return BlockPos.offset(l, direction.getStepX(), direction.getStepY(), direction.getStepZ());
    }

    public static long offset(long l, int i, int j, int k) {
        return BlockPos.asLong(BlockPos.getX(l) + i, BlockPos.getY(l) + j, BlockPos.getZ(l) + k);
    }

    public static int getX(long l) {
        return (int)(l << 64 - X_OFFSET - PACKED_X_LENGTH >> 64 - PACKED_X_LENGTH);
    }

    public static int getY(long l) {
        return (int)(l << 64 - PACKED_Y_LENGTH >> 64 - PACKED_Y_LENGTH);
    }

    public static int getZ(long l) {
        return (int)(l << 64 - Z_OFFSET - PACKED_Z_LENGTH >> 64 - PACKED_Z_LENGTH);
    }

    public static BlockPos of(long l) {
        return new BlockPos(BlockPos.getX(l), BlockPos.getY(l), BlockPos.getZ(l));
    }

    public long asLong() {
        return BlockPos.asLong(this.getX(), this.getY(), this.getZ());
    }

    public static long asLong(int i, int j, int k) {
        long l = 0L;
        l |= ((long)i & PACKED_X_MASK) << X_OFFSET;
        l |= ((long)j & PACKED_Y_MASK) << 0;
        return l |= ((long)k & PACKED_Z_MASK) << Z_OFFSET;
    }

    public static long getFlatIndex(long l) {
        return l & 0xFFFFFFFFFFFFFFF0L;
    }

    public BlockPos offset(double d, double e, double f) {
        if (d == 0.0 && e == 0.0 && f == 0.0) {
            return this;
        }
        return new BlockPos((double)this.getX() + d, (double)this.getY() + e, (double)this.getZ() + f);
    }

    public BlockPos offset(int i, int j, int k) {
        if (i == 0 && j == 0 && k == 0) {
            return this;
        }
        return new BlockPos(this.getX() + i, this.getY() + j, this.getZ() + k);
    }

    public BlockPos offset(Vec3i vec3i) {
        return this.offset(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    public BlockPos subtract(Vec3i vec3i) {
        return this.offset(-vec3i.getX(), -vec3i.getY(), -vec3i.getZ());
    }

    @Override
    public BlockPos above() {
        return this.relative(Direction.UP);
    }

    @Override
    public BlockPos above(int i) {
        return this.relative(Direction.UP, i);
    }

    @Override
    public BlockPos below() {
        return this.relative(Direction.DOWN);
    }

    @Override
    public BlockPos below(int i) {
        return this.relative(Direction.DOWN, i);
    }

    public BlockPos north() {
        return this.relative(Direction.NORTH);
    }

    public BlockPos north(int i) {
        return this.relative(Direction.NORTH, i);
    }

    public BlockPos south() {
        return this.relative(Direction.SOUTH);
    }

    public BlockPos south(int i) {
        return this.relative(Direction.SOUTH, i);
    }

    public BlockPos west() {
        return this.relative(Direction.WEST);
    }

    public BlockPos west(int i) {
        return this.relative(Direction.WEST, i);
    }

    public BlockPos east() {
        return this.relative(Direction.EAST);
    }

    public BlockPos east(int i) {
        return this.relative(Direction.EAST, i);
    }

    public BlockPos relative(Direction direction) {
        return new BlockPos(this.getX() + direction.getStepX(), this.getY() + direction.getStepY(), this.getZ() + direction.getStepZ());
    }

    @Override
    public BlockPos relative(Direction direction, int i) {
        if (i == 0) {
            return this;
        }
        return new BlockPos(this.getX() + direction.getStepX() * i, this.getY() + direction.getStepY() * i, this.getZ() + direction.getStepZ() * i);
    }

    public BlockPos relative(Direction.Axis axis, int i) {
        if (i == 0) {
            return this;
        }
        int j = axis == Direction.Axis.X ? i : 0;
        int k = axis == Direction.Axis.Y ? i : 0;
        int l = axis == Direction.Axis.Z ? i : 0;
        return new BlockPos(this.getX() + j, this.getY() + k, this.getZ() + l);
    }

    public BlockPos rotate(Rotation rotation) {
        switch (rotation) {
            default: {
                return this;
            }
            case CLOCKWISE_90: {
                return new BlockPos(-this.getZ(), this.getY(), this.getX());
            }
            case CLOCKWISE_180: {
                return new BlockPos(-this.getX(), this.getY(), -this.getZ());
            }
            case COUNTERCLOCKWISE_90: 
        }
        return new BlockPos(this.getZ(), this.getY(), -this.getX());
    }

    @Override
    public BlockPos cross(Vec3i vec3i) {
        return new BlockPos(this.getY() * vec3i.getZ() - this.getZ() * vec3i.getY(), this.getZ() * vec3i.getX() - this.getX() * vec3i.getZ(), this.getX() * vec3i.getY() - this.getY() * vec3i.getX());
    }

    public BlockPos atY(int i) {
        return new BlockPos(this.getX(), i, this.getZ());
    }

    public BlockPos immutable() {
        return this;
    }

    public MutableBlockPos mutable() {
        return new MutableBlockPos(this.getX(), this.getY(), this.getZ());
    }

    public static Iterable<BlockPos> randomBetweenClosed(final Random random, final int i, final int j, final int k, final int l, int m, int n, int o) {
        final int p = m - j + 1;
        final int q = n - k + 1;
        final int r = o - l + 1;
        return () -> new AbstractIterator<BlockPos>(){
            final MutableBlockPos nextPos = new MutableBlockPos();
            int counter = i;

            @Override
            protected BlockPos computeNext() {
                if (this.counter <= 0) {
                    return (BlockPos)this.endOfData();
                }
                MutableBlockPos blockPos = this.nextPos.set(j + random.nextInt(p), k + random.nextInt(q), l + random.nextInt(r));
                --this.counter;
                return blockPos;
            }

            @Override
            protected /* synthetic */ Object computeNext() {
                return this.computeNext();
            }
        };
    }

    public static Iterable<BlockPos> withinManhattan(BlockPos blockPos, final int i, final int j, final int k) {
        final int l = i + j + k;
        final int m = blockPos.getX();
        final int n = blockPos.getY();
        final int o = blockPos.getZ();
        return () -> new AbstractIterator<BlockPos>(){
            private final MutableBlockPos cursor = new MutableBlockPos();
            private int currentDepth;
            private int maxX;
            private int maxY;
            private int x;
            private int y;
            private boolean zMirror;

            @Override
            protected BlockPos computeNext() {
                if (this.zMirror) {
                    this.zMirror = false;
                    this.cursor.setZ(o - (this.cursor.getZ() - o));
                    return this.cursor;
                }
                MutableBlockPos blockPos = null;
                while (blockPos == null) {
                    if (this.y > this.maxY) {
                        ++this.x;
                        if (this.x > this.maxX) {
                            ++this.currentDepth;
                            if (this.currentDepth > l) {
                                return (BlockPos)this.endOfData();
                            }
                            this.maxX = Math.min(i, this.currentDepth);
                            this.x = -this.maxX;
                        }
                        this.maxY = Math.min(j, this.currentDepth - Math.abs(this.x));
                        this.y = -this.maxY;
                    }
                    int i2 = this.x;
                    int j2 = this.y;
                    int k2 = this.currentDepth - Math.abs(i2) - Math.abs(j2);
                    if (k2 <= k) {
                        this.zMirror = k2 != 0;
                        blockPos = this.cursor.set(m + i2, n + j2, o + k2);
                    }
                    ++this.y;
                }
                return blockPos;
            }

            @Override
            protected /* synthetic */ Object computeNext() {
                return this.computeNext();
            }
        };
    }

    public static Optional<BlockPos> findClosestMatch(BlockPos blockPos, int i, int j, Predicate<BlockPos> predicate) {
        return BlockPos.withinManhattanStream(blockPos, i, j, i).filter(predicate).findFirst();
    }

    public static Stream<BlockPos> withinManhattanStream(BlockPos blockPos, int i, int j, int k) {
        return StreamSupport.stream(BlockPos.withinManhattan(blockPos, i, j, k).spliterator(), false);
    }

    public static Iterable<BlockPos> betweenClosed(BlockPos blockPos, BlockPos blockPos2) {
        return BlockPos.betweenClosed(Math.min(blockPos.getX(), blockPos2.getX()), Math.min(blockPos.getY(), blockPos2.getY()), Math.min(blockPos.getZ(), blockPos2.getZ()), Math.max(blockPos.getX(), blockPos2.getX()), Math.max(blockPos.getY(), blockPos2.getY()), Math.max(blockPos.getZ(), blockPos2.getZ()));
    }

    public static Stream<BlockPos> betweenClosedStream(BlockPos blockPos, BlockPos blockPos2) {
        return StreamSupport.stream(BlockPos.betweenClosed(blockPos, blockPos2).spliterator(), false);
    }

    public static Stream<BlockPos> betweenClosedStream(BoundingBox boundingBox) {
        return BlockPos.betweenClosedStream(Math.min(boundingBox.x0, boundingBox.x1), Math.min(boundingBox.y0, boundingBox.y1), Math.min(boundingBox.z0, boundingBox.z1), Math.max(boundingBox.x0, boundingBox.x1), Math.max(boundingBox.y0, boundingBox.y1), Math.max(boundingBox.z0, boundingBox.z1));
    }

    public static Stream<BlockPos> betweenClosedStream(AABB aABB) {
        return BlockPos.betweenClosedStream(Mth.floor(aABB.minX), Mth.floor(aABB.minY), Mth.floor(aABB.minZ), Mth.floor(aABB.maxX), Mth.floor(aABB.maxY), Mth.floor(aABB.maxZ));
    }

    public static Stream<BlockPos> betweenClosedStream(int i, int j, int k, int l, int m, int n) {
        return StreamSupport.stream(BlockPos.betweenClosed(i, j, k, l, m, n).spliterator(), false);
    }

    public static Iterable<BlockPos> betweenClosed(final int i, final int j, final int k, int l, int m, int n) {
        final int o = l - i + 1;
        final int p = m - j + 1;
        int q = n - k + 1;
        final int r = o * p * q;
        return () -> new AbstractIterator<BlockPos>(){
            private final MutableBlockPos cursor = new MutableBlockPos();
            private int index;

            @Override
            protected BlockPos computeNext() {
                if (this.index == r) {
                    return (BlockPos)this.endOfData();
                }
                int i2 = this.index % o;
                int j2 = this.index / o;
                int k2 = j2 % p;
                int l = j2 / p;
                ++this.index;
                return this.cursor.set(i + i2, j + k2, k + l);
            }

            @Override
            protected /* synthetic */ Object computeNext() {
                return this.computeNext();
            }
        };
    }

    public static Iterable<MutableBlockPos> spiralAround(final BlockPos blockPos, final int i, final Direction direction, final Direction direction2) {
        Validate.validState(direction.getAxis() != direction2.getAxis(), "The two directions cannot be on the same axis", new Object[0]);
        return () -> new AbstractIterator<MutableBlockPos>(){
            private final Direction[] directions;
            private final MutableBlockPos cursor;
            private final int legs;
            private int leg;
            private int legSize;
            private int legIndex;
            private int lastX;
            private int lastY;
            private int lastZ;
            {
                this.directions = new Direction[]{direction, direction2, direction.getOpposite(), direction2.getOpposite()};
                this.cursor = blockPos.mutable().move(direction2);
                this.legs = 4 * i;
                this.leg = -1;
                this.lastX = this.cursor.getX();
                this.lastY = this.cursor.getY();
                this.lastZ = this.cursor.getZ();
            }

            @Override
            protected MutableBlockPos computeNext() {
                this.cursor.set(this.lastX, this.lastY, this.lastZ).move(this.directions[(this.leg + 4) % 4]);
                this.lastX = this.cursor.getX();
                this.lastY = this.cursor.getY();
                this.lastZ = this.cursor.getZ();
                if (this.legIndex >= this.legSize) {
                    if (this.leg >= this.legs) {
                        return (MutableBlockPos)this.endOfData();
                    }
                    ++this.leg;
                    this.legIndex = 0;
                    this.legSize = this.leg / 2 + 1;
                }
                ++this.legIndex;
                return this.cursor;
            }

            @Override
            protected /* synthetic */ Object computeNext() {
                return this.computeNext();
            }
        };
    }

    @Override
    public /* synthetic */ Vec3i cross(Vec3i vec3i) {
        return this.cross(vec3i);
    }

    @Override
    public /* synthetic */ Vec3i relative(Direction direction, int i) {
        return this.relative(direction, i);
    }

    @Override
    public /* synthetic */ Vec3i below(int i) {
        return this.below(i);
    }

    @Override
    public /* synthetic */ Vec3i below() {
        return this.below();
    }

    @Override
    public /* synthetic */ Vec3i above(int i) {
        return this.above(i);
    }

    @Override
    public /* synthetic */ Vec3i above() {
        return this.above();
    }

    static {
        PACKED_Z_LENGTH = PACKED_X_LENGTH = 1 + Mth.log2(Mth.smallestEncompassingPowerOfTwo(30000000));
        PACKED_Y_LENGTH = 64 - PACKED_X_LENGTH - PACKED_Z_LENGTH;
        PACKED_X_MASK = (1L << PACKED_X_LENGTH) - 1L;
        PACKED_Y_MASK = (1L << PACKED_Y_LENGTH) - 1L;
        PACKED_Z_MASK = (1L << PACKED_Z_LENGTH) - 1L;
        Z_OFFSET = PACKED_Y_LENGTH;
        X_OFFSET = PACKED_Y_LENGTH + PACKED_Z_LENGTH;
    }

    public static class MutableBlockPos
    extends BlockPos {
        public MutableBlockPos() {
            this(0, 0, 0);
        }

        public MutableBlockPos(int i, int j, int k) {
            super(i, j, k);
        }

        public MutableBlockPos(double d, double e, double f) {
            this(Mth.floor(d), Mth.floor(e), Mth.floor(f));
        }

        @Override
        public BlockPos offset(double d, double e, double f) {
            return super.offset(d, e, f).immutable();
        }

        @Override
        public BlockPos offset(int i, int j, int k) {
            return super.offset(i, j, k).immutable();
        }

        @Override
        public BlockPos relative(Direction direction, int i) {
            return super.relative(direction, i).immutable();
        }

        @Override
        public BlockPos relative(Direction.Axis axis, int i) {
            return super.relative(axis, i).immutable();
        }

        @Override
        public BlockPos rotate(Rotation rotation) {
            return super.rotate(rotation).immutable();
        }

        public MutableBlockPos set(int i, int j, int k) {
            this.setX(i);
            this.setY(j);
            this.setZ(k);
            return this;
        }

        public MutableBlockPos set(double d, double e, double f) {
            return this.set(Mth.floor(d), Mth.floor(e), Mth.floor(f));
        }

        public MutableBlockPos set(Vec3i vec3i) {
            return this.set(vec3i.getX(), vec3i.getY(), vec3i.getZ());
        }

        public MutableBlockPos set(long l) {
            return this.set(MutableBlockPos.getX(l), MutableBlockPos.getY(l), MutableBlockPos.getZ(l));
        }

        public MutableBlockPos set(AxisCycle axisCycle, int i, int j, int k) {
            return this.set(axisCycle.cycle(i, j, k, Direction.Axis.X), axisCycle.cycle(i, j, k, Direction.Axis.Y), axisCycle.cycle(i, j, k, Direction.Axis.Z));
        }

        public MutableBlockPos setWithOffset(Vec3i vec3i, Direction direction) {
            return this.set(vec3i.getX() + direction.getStepX(), vec3i.getY() + direction.getStepY(), vec3i.getZ() + direction.getStepZ());
        }

        public MutableBlockPos setWithOffset(Vec3i vec3i, int i, int j, int k) {
            return this.set(vec3i.getX() + i, vec3i.getY() + j, vec3i.getZ() + k);
        }

        public MutableBlockPos move(Direction direction) {
            return this.move(direction, 1);
        }

        public MutableBlockPos move(Direction direction, int i) {
            return this.set(this.getX() + direction.getStepX() * i, this.getY() + direction.getStepY() * i, this.getZ() + direction.getStepZ() * i);
        }

        public MutableBlockPos move(int i, int j, int k) {
            return this.set(this.getX() + i, this.getY() + j, this.getZ() + k);
        }

        public MutableBlockPos move(Vec3i vec3i) {
            return this.set(this.getX() + vec3i.getX(), this.getY() + vec3i.getY(), this.getZ() + vec3i.getZ());
        }

        public MutableBlockPos clamp(Direction.Axis axis, int i, int j) {
            switch (axis) {
                case X: {
                    return this.set(Mth.clamp(this.getX(), i, j), this.getY(), this.getZ());
                }
                case Y: {
                    return this.set(this.getX(), Mth.clamp(this.getY(), i, j), this.getZ());
                }
                case Z: {
                    return this.set(this.getX(), this.getY(), Mth.clamp(this.getZ(), i, j));
                }
            }
            throw new IllegalStateException("Unable to clamp axis " + axis);
        }

        @Override
        public MutableBlockPos setX(int i) {
            super.setX(i);
            return this;
        }

        @Override
        public MutableBlockPos setY(int i) {
            super.setY(i);
            return this;
        }

        @Override
        public MutableBlockPos setZ(int i) {
            super.setZ(i);
            return this;
        }

        @Override
        public BlockPos immutable() {
            return new BlockPos(this);
        }

        @Override
        public /* synthetic */ Vec3i cross(Vec3i vec3i) {
            return super.cross(vec3i);
        }

        @Override
        public /* synthetic */ Vec3i relative(Direction direction, int i) {
            return this.relative(direction, i);
        }

        @Override
        public /* synthetic */ Vec3i below(int i) {
            return super.below(i);
        }

        @Override
        public /* synthetic */ Vec3i below() {
            return super.below();
        }

        @Override
        public /* synthetic */ Vec3i above(int i) {
            return super.above(i);
        }

        @Override
        public /* synthetic */ Vec3i above() {
            return super.above();
        }

        @Override
        public /* synthetic */ Vec3i setZ(int i) {
            return this.setZ(i);
        }

        @Override
        public /* synthetic */ Vec3i setY(int i) {
            return this.setY(i);
        }

        @Override
        public /* synthetic */ Vec3i setX(int i) {
            return this.setX(i);
        }
    }
}

