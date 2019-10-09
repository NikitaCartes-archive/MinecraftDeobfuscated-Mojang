/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.util.Serializable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Unmodifiable;

@Unmodifiable
public class BlockPos
extends Vec3i
implements Serializable {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final BlockPos ZERO = new BlockPos(0, 0, 0);
    private static final int PACKED_X_LENGTH;
    private static final int PACKED_Z_LENGTH;
    private static final int PACKED_Y_LENGTH;
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

    public BlockPos(Entity entity) {
        this(entity.getX(), entity.getY(), entity.getZ());
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

    public static <T> BlockPos deserialize(Dynamic<T> dynamic) {
        int[] is;
        Spliterator.OfInt ofInt = dynamic.asIntStream().spliterator();
        if (ofInt.tryAdvance(arg_0 -> BlockPos.method_19441(is = new int[3], arg_0)) && ofInt.tryAdvance(i -> {
            is[1] = i;
        })) {
            ofInt.tryAdvance(i -> {
                is[2] = i;
            });
        }
        return new BlockPos(is[0], is[1], is[2]);
    }

    @Override
    public <T> T serialize(DynamicOps<T> dynamicOps) {
        return dynamicOps.createIntList(IntStream.of(this.getX(), this.getY(), this.getZ()));
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

    public static long asLong(int i, int j, int k) {
        long l = 0L;
        l |= ((long)i & PACKED_X_MASK) << X_OFFSET;
        l |= ((long)j & PACKED_Y_MASK) << 0;
        return l |= ((long)k & PACKED_Z_MASK) << Z_OFFSET;
    }

    public static long getFlatIndex(long l) {
        return l & 0xFFFFFFFFFFFFFFF0L;
    }

    public long asLong() {
        return BlockPos.asLong(this.getX(), this.getY(), this.getZ());
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

    public BlockPos above() {
        return this.above(1);
    }

    public BlockPos above(int i) {
        return this.relative(Direction.UP, i);
    }

    @Override
    public BlockPos below() {
        return this.below(1);
    }

    @Override
    public BlockPos below(int i) {
        return this.relative(Direction.DOWN, i);
    }

    public BlockPos north() {
        return this.north(1);
    }

    public BlockPos north(int i) {
        return this.relative(Direction.NORTH, i);
    }

    public BlockPos south() {
        return this.south(1);
    }

    public BlockPos south(int i) {
        return this.relative(Direction.SOUTH, i);
    }

    public BlockPos west() {
        return this.west(1);
    }

    public BlockPos west(int i) {
        return this.relative(Direction.WEST, i);
    }

    public BlockPos east() {
        return this.east(1);
    }

    public BlockPos east(int i) {
        return this.relative(Direction.EAST, i);
    }

    public BlockPos relative(Direction direction) {
        return this.relative(direction, 1);
    }

    @Override
    public BlockPos relative(Direction direction, int i) {
        if (i == 0) {
            return this;
        }
        return new BlockPos(this.getX() + direction.getStepX() * i, this.getY() + direction.getStepY() * i, this.getZ() + direction.getStepZ() * i);
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

    public BlockPos immutable() {
        return this;
    }

    public static Iterable<BlockPos> betweenClosed(BlockPos blockPos, BlockPos blockPos2) {
        return BlockPos.betweenClosed(Math.min(blockPos.getX(), blockPos2.getX()), Math.min(blockPos.getY(), blockPos2.getY()), Math.min(blockPos.getZ(), blockPos2.getZ()), Math.max(blockPos.getX(), blockPos2.getX()), Math.max(blockPos.getY(), blockPos2.getY()), Math.max(blockPos.getZ(), blockPos2.getZ()));
    }

    public static Stream<BlockPos> betweenClosedStream(BlockPos blockPos, BlockPos blockPos2) {
        return BlockPos.betweenClosedStream(Math.min(blockPos.getX(), blockPos2.getX()), Math.min(blockPos.getY(), blockPos2.getY()), Math.min(blockPos.getZ(), blockPos2.getZ()), Math.max(blockPos.getX(), blockPos2.getX()), Math.max(blockPos.getY(), blockPos2.getY()), Math.max(blockPos.getZ(), blockPos2.getZ()));
    }

    public static Stream<BlockPos> betweenClosedStream(BoundingBox boundingBox) {
        return BlockPos.betweenClosedStream(Math.min(boundingBox.x0, boundingBox.x1), Math.min(boundingBox.y0, boundingBox.y1), Math.min(boundingBox.z0, boundingBox.z1), Math.max(boundingBox.x0, boundingBox.x1), Math.max(boundingBox.y0, boundingBox.y1), Math.max(boundingBox.z0, boundingBox.z1));
    }

    public static Stream<BlockPos> betweenClosedStream(final int i, final int j, final int k, final int l, final int m, final int n) {
        return StreamSupport.stream(new Spliterators.AbstractSpliterator<BlockPos>((long)((l - i + 1) * (m - j + 1) * (n - k + 1)), 64){
            final Cursor3D cursor;
            final MutableBlockPos nextPos;
            {
                super(l2, i2);
                this.cursor = new Cursor3D(i, j, k, l, m, n);
                this.nextPos = new MutableBlockPos();
            }

            @Override
            public boolean tryAdvance(Consumer<? super BlockPos> consumer) {
                if (this.cursor.advance()) {
                    consumer.accept(this.nextPos.set(this.cursor.nextX(), this.cursor.nextY(), this.cursor.nextZ()));
                    return true;
                }
                return false;
            }
        }, false);
    }

    public static Iterable<BlockPos> betweenClosed(final int i, final int j, final int k, final int l, final int m, final int n) {
        return () -> new AbstractIterator<BlockPos>(){
            final Cursor3D cursor;
            final MutableBlockPos nextPos;
            {
                this.cursor = new Cursor3D(i, j, k, l, m, n);
                this.nextPos = new MutableBlockPos();
            }

            @Override
            protected BlockPos computeNext() {
                return this.cursor.advance() ? this.nextPos.set(this.cursor.nextX(), this.cursor.nextY(), this.cursor.nextZ()) : (BlockPos)this.endOfData();
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

    private static /* synthetic */ void method_19441(int[] is, int i) {
        is[0] = i;
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

    public static final class PooledMutableBlockPos
    extends MutableBlockPos
    implements AutoCloseable {
        private boolean free;
        private static final List<PooledMutableBlockPos> POOL = Lists.newArrayList();

        private PooledMutableBlockPos(int i, int j, int k) {
            super(i, j, k);
        }

        public static PooledMutableBlockPos acquire() {
            return PooledMutableBlockPos.acquire(0, 0, 0);
        }

        public static PooledMutableBlockPos acquire(Entity entity) {
            return PooledMutableBlockPos.acquire(entity.getX(), entity.getY(), entity.getZ());
        }

        public static PooledMutableBlockPos acquire(double d, double e, double f) {
            return PooledMutableBlockPos.acquire(Mth.floor(d), Mth.floor(e), Mth.floor(f));
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public static PooledMutableBlockPos acquire(int i, int j, int k) {
            List<PooledMutableBlockPos> list = POOL;
            synchronized (list) {
                PooledMutableBlockPos pooledMutableBlockPos;
                if (!POOL.isEmpty() && (pooledMutableBlockPos = POOL.remove(POOL.size() - 1)) != null && pooledMutableBlockPos.free) {
                    pooledMutableBlockPos.free = false;
                    pooledMutableBlockPos.set(i, j, k);
                    return pooledMutableBlockPos;
                }
            }
            return new PooledMutableBlockPos(i, j, k);
        }

        @Override
        public PooledMutableBlockPos set(int i, int j, int k) {
            return (PooledMutableBlockPos)super.set(i, j, k);
        }

        @Override
        public PooledMutableBlockPos set(Entity entity) {
            return (PooledMutableBlockPos)super.set(entity);
        }

        @Override
        public PooledMutableBlockPos set(double d, double e, double f) {
            return (PooledMutableBlockPos)super.set(d, e, f);
        }

        @Override
        public PooledMutableBlockPos set(Vec3i vec3i) {
            return (PooledMutableBlockPos)super.set(vec3i);
        }

        @Override
        public PooledMutableBlockPos move(Direction direction) {
            return (PooledMutableBlockPos)super.move(direction);
        }

        @Override
        public PooledMutableBlockPos move(Direction direction, int i) {
            return (PooledMutableBlockPos)super.move(direction, i);
        }

        @Override
        public PooledMutableBlockPos move(int i, int j, int k) {
            return (PooledMutableBlockPos)super.move(i, j, k);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void close() {
            List<PooledMutableBlockPos> list = POOL;
            synchronized (list) {
                if (POOL.size() < 100) {
                    POOL.add(this);
                }
                this.free = true;
            }
        }

        @Override
        public /* synthetic */ MutableBlockPos move(int i, int j, int k) {
            return this.move(i, j, k);
        }

        @Override
        public /* synthetic */ MutableBlockPos move(Direction direction, int i) {
            return this.move(direction, i);
        }

        @Override
        public /* synthetic */ MutableBlockPos move(Direction direction) {
            return this.move(direction);
        }

        @Override
        public /* synthetic */ MutableBlockPos set(Vec3i vec3i) {
            return this.set(vec3i);
        }

        @Override
        public /* synthetic */ MutableBlockPos set(double d, double e, double f) {
            return this.set(d, e, f);
        }

        @Override
        public /* synthetic */ MutableBlockPos set(Entity entity) {
            return this.set(entity);
        }

        @Override
        public /* synthetic */ MutableBlockPos set(int i, int j, int k) {
            return this.set(i, j, k);
        }
    }

    public static class MutableBlockPos
    extends BlockPos {
        protected int x;
        protected int y;
        protected int z;

        public MutableBlockPos() {
            this(0, 0, 0);
        }

        public MutableBlockPos(BlockPos blockPos) {
            this(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }

        public MutableBlockPos(int i, int j, int k) {
            super(0, 0, 0);
            this.x = i;
            this.y = j;
            this.z = k;
        }

        public MutableBlockPos(double d, double e, double f) {
            this(Mth.floor(d), Mth.floor(e), Mth.floor(f));
        }

        public MutableBlockPos(Entity entity) {
            this(entity.getX(), entity.getY(), entity.getZ());
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
        public BlockPos rotate(Rotation rotation) {
            return super.rotate(rotation).immutable();
        }

        @Override
        public int getX() {
            return this.x;
        }

        @Override
        public int getY() {
            return this.y;
        }

        @Override
        public int getZ() {
            return this.z;
        }

        public MutableBlockPos set(int i, int j, int k) {
            this.x = i;
            this.y = j;
            this.z = k;
            return this;
        }

        public MutableBlockPos set(Entity entity) {
            return this.set(entity.getX(), entity.getY(), entity.getZ());
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

        public MutableBlockPos move(Direction direction) {
            return this.move(direction, 1);
        }

        public MutableBlockPos move(Direction direction, int i) {
            return this.set(this.x + direction.getStepX() * i, this.y + direction.getStepY() * i, this.z + direction.getStepZ() * i);
        }

        public MutableBlockPos move(int i, int j, int k) {
            return this.set(this.x + i, this.y + j, this.z + k);
        }

        public void setX(int i) {
            this.x = i;
        }

        public void setY(int i) {
            this.y = i;
        }

        public void setZ(int i) {
            this.z = i;
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
    }
}

