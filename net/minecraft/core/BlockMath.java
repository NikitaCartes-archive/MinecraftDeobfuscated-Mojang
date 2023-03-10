/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.math.Transformation;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;

public class BlockMath {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Map<Direction, Transformation> VANILLA_UV_TRANSFORM_LOCAL_TO_GLOBAL = Util.make(Maps.newEnumMap(Direction.class), enumMap -> {
        enumMap.put(Direction.SOUTH, Transformation.identity());
        enumMap.put(Direction.EAST, new Transformation(null, new Quaternionf().rotateY(1.5707964f), null, null));
        enumMap.put(Direction.WEST, new Transformation(null, new Quaternionf().rotateY(-1.5707964f), null, null));
        enumMap.put(Direction.NORTH, new Transformation(null, new Quaternionf().rotateY((float)Math.PI), null, null));
        enumMap.put(Direction.UP, new Transformation(null, new Quaternionf().rotateX(-1.5707964f), null, null));
        enumMap.put(Direction.DOWN, new Transformation(null, new Quaternionf().rotateX(1.5707964f), null, null));
    });
    public static final Map<Direction, Transformation> VANILLA_UV_TRANSFORM_GLOBAL_TO_LOCAL = Util.make(Maps.newEnumMap(Direction.class), enumMap -> {
        for (Direction direction : Direction.values()) {
            enumMap.put(direction, VANILLA_UV_TRANSFORM_LOCAL_TO_GLOBAL.get(direction).inverse());
        }
    });

    public static Transformation blockCenterToCorner(Transformation transformation) {
        Matrix4f matrix4f = new Matrix4f().translation(0.5f, 0.5f, 0.5f);
        matrix4f.mul(transformation.getMatrix());
        matrix4f.translate(-0.5f, -0.5f, -0.5f);
        return new Transformation(matrix4f);
    }

    public static Transformation blockCornerToCenter(Transformation transformation) {
        Matrix4f matrix4f = new Matrix4f().translation(-0.5f, -0.5f, -0.5f);
        matrix4f.mul(transformation.getMatrix());
        matrix4f.translate(0.5f, 0.5f, 0.5f);
        return new Transformation(matrix4f);
    }

    public static Transformation getUVLockTransform(Transformation transformation, Direction direction, Supplier<String> supplier) {
        Direction direction2 = Direction.rotate(transformation.getMatrix(), direction);
        Transformation transformation2 = transformation.inverse();
        if (transformation2 == null) {
            LOGGER.warn(supplier.get());
            return new Transformation(null, null, new Vector3f(0.0f, 0.0f, 0.0f), null);
        }
        Transformation transformation3 = VANILLA_UV_TRANSFORM_GLOBAL_TO_LOCAL.get(direction).compose(transformation2).compose(VANILLA_UV_TRANSFORM_LOCAL_TO_GLOBAL.get(direction2));
        return BlockMath.blockCenterToCorner(transformation3);
    }
}

