/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import com.google.common.collect.Iterators;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public enum Direction implements StringRepresentable
{
    DOWN(0, 1, -1, "down", AxisDirection.NEGATIVE, Axis.Y, new Vec3i(0, -1, 0)),
    UP(1, 0, -1, "up", AxisDirection.POSITIVE, Axis.Y, new Vec3i(0, 1, 0)),
    NORTH(2, 3, 2, "north", AxisDirection.NEGATIVE, Axis.Z, new Vec3i(0, 0, -1)),
    SOUTH(3, 2, 0, "south", AxisDirection.POSITIVE, Axis.Z, new Vec3i(0, 0, 1)),
    WEST(4, 5, 1, "west", AxisDirection.NEGATIVE, Axis.X, new Vec3i(-1, 0, 0)),
    EAST(5, 4, 3, "east", AxisDirection.POSITIVE, Axis.X, new Vec3i(1, 0, 0));

    public static final Codec<Direction> CODEC;
    public static final Codec<Direction> VERTICAL_CODEC;
    private final int data3d;
    private final int oppositeIndex;
    private final int data2d;
    private final String name;
    private final Axis axis;
    private final AxisDirection axisDirection;
    private final Vec3i normal;
    private static final Direction[] VALUES;
    private static final Map<String, Direction> BY_NAME;
    private static final Direction[] BY_3D_DATA;
    private static final Direction[] BY_2D_DATA;
    private static final Long2ObjectMap<Direction> BY_NORMAL;

    private Direction(int j, int k, int l, String string2, AxisDirection axisDirection, Axis axis, Vec3i vec3i) {
        this.data3d = j;
        this.data2d = l;
        this.oppositeIndex = k;
        this.name = string2;
        this.axis = axis;
        this.axisDirection = axisDirection;
        this.normal = vec3i;
    }

    public static Direction[] orderedByNearest(Entity entity) {
        Direction direction3;
        float f = entity.getViewXRot(1.0f) * ((float)Math.PI / 180);
        float g = -entity.getViewYRot(1.0f) * ((float)Math.PI / 180);
        float h = Mth.sin(f);
        float i = Mth.cos(f);
        float j = Mth.sin(g);
        float k = Mth.cos(g);
        boolean bl = j > 0.0f;
        boolean bl2 = h < 0.0f;
        boolean bl3 = k > 0.0f;
        float l = bl ? j : -j;
        float m = bl2 ? -h : h;
        float n = bl3 ? k : -k;
        float o = l * i;
        float p = n * i;
        Direction direction = bl ? EAST : WEST;
        Direction direction2 = bl2 ? UP : DOWN;
        Direction direction4 = direction3 = bl3 ? SOUTH : NORTH;
        if (l > n) {
            if (m > o) {
                return Direction.makeDirectionArray(direction2, direction, direction3);
            }
            if (p > m) {
                return Direction.makeDirectionArray(direction, direction3, direction2);
            }
            return Direction.makeDirectionArray(direction, direction2, direction3);
        }
        if (m > p) {
            return Direction.makeDirectionArray(direction2, direction3, direction);
        }
        if (o > m) {
            return Direction.makeDirectionArray(direction3, direction, direction2);
        }
        return Direction.makeDirectionArray(direction3, direction2, direction);
    }

    private static Direction[] makeDirectionArray(Direction direction, Direction direction2, Direction direction3) {
        return new Direction[]{direction, direction2, direction3, direction3.getOpposite(), direction2.getOpposite(), direction.getOpposite()};
    }

    public static Direction rotate(Matrix4f matrix4f, Direction direction) {
        Vec3i vec3i = direction.getNormal();
        Vector4f vector4f = new Vector4f(vec3i.getX(), vec3i.getY(), vec3i.getZ(), 0.0f);
        vector4f.transform(matrix4f);
        return Direction.getNearest(vector4f.x(), vector4f.y(), vector4f.z());
    }

    public Quaternion getRotation() {
        Quaternion quaternion = Vector3f.XP.rotationDegrees(90.0f);
        return switch (this) {
            case DOWN -> Vector3f.XP.rotationDegrees(180.0f);
            case UP -> Quaternion.ONE.copy();
            case NORTH -> {
                quaternion.mul(Vector3f.ZP.rotationDegrees(180.0f));
                yield quaternion;
            }
            case SOUTH -> quaternion;
            case WEST -> {
                quaternion.mul(Vector3f.ZP.rotationDegrees(90.0f));
                yield quaternion;
            }
            case EAST -> {
                quaternion.mul(Vector3f.ZP.rotationDegrees(-90.0f));
                yield quaternion;
            }
            default -> throw new IncompatibleClassChangeError();
        };
    }

    public int get3DDataValue() {
        return this.data3d;
    }

    public int get2DDataValue() {
        return this.data2d;
    }

    public AxisDirection getAxisDirection() {
        return this.axisDirection;
    }

    public static Direction getFacingAxis(Entity entity, Axis axis) {
        return switch (axis) {
            case Axis.X -> {
                if (EAST.isFacingAngle(entity.getViewYRot(1.0f))) {
                    yield EAST;
                }
                yield WEST;
            }
            case Axis.Z -> {
                if (SOUTH.isFacingAngle(entity.getViewYRot(1.0f))) {
                    yield SOUTH;
                }
                yield NORTH;
            }
            case Axis.Y -> {
                if (entity.getViewXRot(1.0f) < 0.0f) {
                    yield UP;
                }
                yield DOWN;
            }
            default -> throw new IncompatibleClassChangeError();
        };
    }

    public Direction getOpposite() {
        return Direction.from3DDataValue(this.oppositeIndex);
    }

    public Direction getClockWise(Axis axis) {
        return switch (axis) {
            case Axis.X -> {
                if (this == WEST || this == EAST) {
                    yield this;
                }
                yield this.getClockWiseX();
            }
            case Axis.Y -> {
                if (this == UP || this == DOWN) {
                    yield this;
                }
                yield this.getClockWise();
            }
            case Axis.Z -> {
                if (this == NORTH || this == SOUTH) {
                    yield this;
                }
                yield this.getClockWiseZ();
            }
            default -> throw new IncompatibleClassChangeError();
        };
    }

    public Direction getCounterClockWise(Axis axis) {
        return switch (axis) {
            case Axis.X -> {
                if (this == WEST || this == EAST) {
                    yield this;
                }
                yield this.getCounterClockWiseX();
            }
            case Axis.Y -> {
                if (this == UP || this == DOWN) {
                    yield this;
                }
                yield this.getCounterClockWise();
            }
            case Axis.Z -> {
                if (this == NORTH || this == SOUTH) {
                    yield this;
                }
                yield this.getCounterClockWiseZ();
            }
            default -> throw new IncompatibleClassChangeError();
        };
    }

    public Direction getClockWise() {
        return switch (this) {
            case NORTH -> EAST;
            case EAST -> SOUTH;
            case SOUTH -> WEST;
            case WEST -> NORTH;
            default -> throw new IllegalStateException("Unable to get Y-rotated facing of " + this);
        };
    }

    private Direction getClockWiseX() {
        return switch (this) {
            case UP -> NORTH;
            case NORTH -> DOWN;
            case DOWN -> SOUTH;
            case SOUTH -> UP;
            default -> throw new IllegalStateException("Unable to get X-rotated facing of " + this);
        };
    }

    private Direction getCounterClockWiseX() {
        return switch (this) {
            case UP -> SOUTH;
            case SOUTH -> DOWN;
            case DOWN -> NORTH;
            case NORTH -> UP;
            default -> throw new IllegalStateException("Unable to get X-rotated facing of " + this);
        };
    }

    private Direction getClockWiseZ() {
        return switch (this) {
            case UP -> EAST;
            case EAST -> DOWN;
            case DOWN -> WEST;
            case WEST -> UP;
            default -> throw new IllegalStateException("Unable to get Z-rotated facing of " + this);
        };
    }

    private Direction getCounterClockWiseZ() {
        return switch (this) {
            case UP -> WEST;
            case WEST -> DOWN;
            case DOWN -> EAST;
            case EAST -> UP;
            default -> throw new IllegalStateException("Unable to get Z-rotated facing of " + this);
        };
    }

    public Direction getCounterClockWise() {
        return switch (this) {
            case NORTH -> WEST;
            case EAST -> NORTH;
            case SOUTH -> EAST;
            case WEST -> SOUTH;
            default -> throw new IllegalStateException("Unable to get CCW facing of " + this);
        };
    }

    public int getStepX() {
        return this.normal.getX();
    }

    public int getStepY() {
        return this.normal.getY();
    }

    public int getStepZ() {
        return this.normal.getZ();
    }

    public Vector3f step() {
        return new Vector3f(this.getStepX(), this.getStepY(), this.getStepZ());
    }

    public String getName() {
        return this.name;
    }

    public Axis getAxis() {
        return this.axis;
    }

    @Nullable
    public static Direction byName(@Nullable String string) {
        if (string == null) {
            return null;
        }
        return BY_NAME.get(string.toLowerCase(Locale.ROOT));
    }

    public static Direction from3DDataValue(int i) {
        return BY_3D_DATA[Mth.abs(i % BY_3D_DATA.length)];
    }

    public static Direction from2DDataValue(int i) {
        return BY_2D_DATA[Mth.abs(i % BY_2D_DATA.length)];
    }

    @Nullable
    public static Direction fromNormal(BlockPos blockPos) {
        return (Direction)BY_NORMAL.get(blockPos.asLong());
    }

    @Nullable
    public static Direction fromNormal(int i, int j, int k) {
        return (Direction)BY_NORMAL.get(BlockPos.asLong(i, j, k));
    }

    public static Direction fromYRot(double d) {
        return Direction.from2DDataValue(Mth.floor(d / 90.0 + 0.5) & 3);
    }

    public static Direction fromAxisAndDirection(Axis axis, AxisDirection axisDirection) {
        return switch (axis) {
            case Axis.X -> {
                if (axisDirection == AxisDirection.POSITIVE) {
                    yield EAST;
                }
                yield WEST;
            }
            case Axis.Y -> {
                if (axisDirection == AxisDirection.POSITIVE) {
                    yield UP;
                }
                yield DOWN;
            }
            case Axis.Z -> {
                if (axisDirection == AxisDirection.POSITIVE) {
                    yield SOUTH;
                }
                yield NORTH;
            }
            default -> throw new IncompatibleClassChangeError();
        };
    }

    public float toYRot() {
        return (this.data2d & 3) * 90;
    }

    public static Direction getRandom(Random random) {
        return Util.getRandom(VALUES, random);
    }

    public static Direction getNearest(double d, double e, double f) {
        return Direction.getNearest((float)d, (float)e, (float)f);
    }

    public static Direction getNearest(float f, float g, float h) {
        Direction direction = NORTH;
        float i = Float.MIN_VALUE;
        for (Direction direction2 : VALUES) {
            float j = f * (float)direction2.normal.getX() + g * (float)direction2.normal.getY() + h * (float)direction2.normal.getZ();
            if (!(j > i)) continue;
            i = j;
            direction = direction2;
        }
        return direction;
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    private static DataResult<Direction> verifyVertical(Direction direction) {
        return direction.getAxis().isVertical() ? DataResult.success(direction) : DataResult.error("Expected a vertical direction");
    }

    public static Direction get(AxisDirection axisDirection, Axis axis) {
        for (Direction direction : VALUES) {
            if (direction.getAxisDirection() != axisDirection || direction.getAxis() != axis) continue;
            return direction;
        }
        throw new IllegalArgumentException("No such direction: " + axisDirection + " " + axis);
    }

    public Vec3i getNormal() {
        return this.normal;
    }

    public boolean isFacingAngle(float f) {
        float g = f * ((float)Math.PI / 180);
        float h = -Mth.sin(g);
        float i = Mth.cos(g);
        return (float)this.normal.getX() * h + (float)this.normal.getZ() * i > 0.0f;
    }

    static {
        CODEC = StringRepresentable.fromEnum(Direction::values, Direction::byName);
        VERTICAL_CODEC = CODEC.flatXmap(Direction::verifyVertical, Direction::verifyVertical);
        VALUES = Direction.values();
        BY_NAME = Arrays.stream(VALUES).collect(Collectors.toMap(Direction::getName, direction -> direction));
        BY_3D_DATA = (Direction[])Arrays.stream(VALUES).sorted(Comparator.comparingInt(direction -> direction.data3d)).toArray(Direction[]::new);
        BY_2D_DATA = (Direction[])Arrays.stream(VALUES).filter(direction -> direction.getAxis().isHorizontal()).sorted(Comparator.comparingInt(direction -> direction.data2d)).toArray(Direction[]::new);
        BY_NORMAL = Arrays.stream(VALUES).collect(Collectors.toMap(direction -> new BlockPos(direction.getNormal()).asLong(), direction -> direction, (direction, direction2) -> {
            throw new IllegalArgumentException("Duplicate keys");
        }, Long2ObjectOpenHashMap::new));
    }

    public static enum Axis implements StringRepresentable,
    Predicate<Direction>
    {
        X("x"){

            @Override
            public int choose(int i, int j, int k) {
                return i;
            }

            @Override
            public double choose(double d, double e, double f) {
                return d;
            }

            @Override
            public /* synthetic */ boolean test(@Nullable Object object) {
                return super.test((Direction)object);
            }
        }
        ,
        Y("y"){

            @Override
            public int choose(int i, int j, int k) {
                return j;
            }

            @Override
            public double choose(double d, double e, double f) {
                return e;
            }

            @Override
            public /* synthetic */ boolean test(@Nullable Object object) {
                return super.test((Direction)object);
            }
        }
        ,
        Z("z"){

            @Override
            public int choose(int i, int j, int k) {
                return k;
            }

            @Override
            public double choose(double d, double e, double f) {
                return f;
            }

            @Override
            public /* synthetic */ boolean test(@Nullable Object object) {
                return super.test((Direction)object);
            }
        };

        public static final Axis[] VALUES;
        public static final Codec<Axis> CODEC;
        private static final Map<String, Axis> BY_NAME;
        private final String name;

        Axis(String string2) {
            this.name = string2;
        }

        @Nullable
        public static Axis byName(String string) {
            return BY_NAME.get(string.toLowerCase(Locale.ROOT));
        }

        public String getName() {
            return this.name;
        }

        public boolean isVertical() {
            return this == Y;
        }

        public boolean isHorizontal() {
            return this == X || this == Z;
        }

        public String toString() {
            return this.name;
        }

        public static Axis getRandom(Random random) {
            return Util.getRandom(VALUES, random);
        }

        @Override
        public boolean test(@Nullable Direction direction) {
            return direction != null && direction.getAxis() == this;
        }

        public Plane getPlane() {
            return switch (this) {
                case X, Z -> Plane.HORIZONTAL;
                case Y -> Plane.VERTICAL;
                default -> throw new IncompatibleClassChangeError();
            };
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public abstract int choose(int var1, int var2, int var3);

        public abstract double choose(double var1, double var3, double var5);

        @Override
        public /* synthetic */ boolean test(@Nullable Object object) {
            return this.test((Direction)object);
        }

        static {
            VALUES = Axis.values();
            CODEC = StringRepresentable.fromEnum(Axis::values, Axis::byName);
            BY_NAME = Arrays.stream(VALUES).collect(Collectors.toMap(Axis::getName, axis -> axis));
        }
    }

    public static enum AxisDirection {
        POSITIVE(1, "Towards positive"),
        NEGATIVE(-1, "Towards negative");

        private final int step;
        private final String name;

        private AxisDirection(int j, String string2) {
            this.step = j;
            this.name = string2;
        }

        public int getStep() {
            return this.step;
        }

        public String getName() {
            return this.name;
        }

        public String toString() {
            return this.name;
        }

        public AxisDirection opposite() {
            return this == POSITIVE ? NEGATIVE : POSITIVE;
        }
    }

    public static enum Plane implements Iterable<Direction>,
    Predicate<Direction>
    {
        HORIZONTAL(new Direction[]{NORTH, EAST, SOUTH, WEST}, new Axis[]{Axis.X, Axis.Z}),
        VERTICAL(new Direction[]{UP, DOWN}, new Axis[]{Axis.Y});

        private final Direction[] faces;
        private final Axis[] axis;

        private Plane(Direction[] directions, Axis[] axiss) {
            this.faces = directions;
            this.axis = axiss;
        }

        public Direction getRandomDirection(Random random) {
            return Util.getRandom(this.faces, random);
        }

        public Axis getRandomAxis(Random random) {
            return Util.getRandom(this.axis, random);
        }

        @Override
        public boolean test(@Nullable Direction direction) {
            return direction != null && direction.getAxis().getPlane() == this;
        }

        @Override
        public Iterator<Direction> iterator() {
            return Iterators.forArray(this.faces);
        }

        public Stream<Direction> stream() {
            return Arrays.stream(this.faces);
        }

        @Override
        public /* synthetic */ boolean test(@Nullable Object object) {
            return this.test((Direction)object);
        }
    }
}

