package net.minecraft.core;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.math.Transformation;
import java.util.Map;
import net.minecraft.Util;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.slf4j.Logger;

public class BlockMath {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final Map<Direction, Transformation> VANILLA_UV_TRANSFORM_LOCAL_TO_GLOBAL = Util.make(Maps.newEnumMap(Direction.class), enumMap -> {
		enumMap.put(Direction.SOUTH, Transformation.identity());
		enumMap.put(Direction.EAST, new Transformation(null, new Quaternionf().rotateY((float) (Math.PI / 2)), null, null));
		enumMap.put(Direction.WEST, new Transformation(null, new Quaternionf().rotateY((float) (-Math.PI / 2)), null, null));
		enumMap.put(Direction.NORTH, new Transformation(null, new Quaternionf().rotateY((float) Math.PI), null, null));
		enumMap.put(Direction.UP, new Transformation(null, new Quaternionf().rotateX((float) (-Math.PI / 2)), null, null));
		enumMap.put(Direction.DOWN, new Transformation(null, new Quaternionf().rotateX((float) (Math.PI / 2)), null, null));
	});
	public static final Map<Direction, Transformation> VANILLA_UV_TRANSFORM_GLOBAL_TO_LOCAL = Util.make(Maps.newEnumMap(Direction.class), enumMap -> {
		for (Direction direction : Direction.values()) {
			enumMap.put(direction, ((Transformation)VANILLA_UV_TRANSFORM_LOCAL_TO_GLOBAL.get(direction)).inverse());
		}
	});

	public static Transformation blockCenterToCorner(Transformation transformation) {
		Matrix4f matrix4f = new Matrix4f().translation(0.5F, 0.5F, 0.5F);
		matrix4f.mul(transformation.getMatrix());
		matrix4f.translate(-0.5F, -0.5F, -0.5F);
		return new Transformation(matrix4f);
	}

	public static Transformation blockCornerToCenter(Transformation transformation) {
		Matrix4f matrix4f = new Matrix4f().translation(-0.5F, -0.5F, -0.5F);
		matrix4f.mul(transformation.getMatrix());
		matrix4f.translate(0.5F, 0.5F, 0.5F);
		return new Transformation(matrix4f);
	}

	public static Transformation getUVLockTransform(Transformation transformation, Direction direction) {
		Direction direction2 = Direction.rotate(transformation.getMatrix(), direction);
		Transformation transformation2 = transformation.inverse();
		if (transformation2 == null) {
			LOGGER.debug("Failed to invert transformation {}", transformation);
			return Transformation.identity();
		} else {
			Transformation transformation3 = ((Transformation)VANILLA_UV_TRANSFORM_GLOBAL_TO_LOCAL.get(direction))
				.compose(transformation2)
				.compose((Transformation)VANILLA_UV_TRANSFORM_LOCAL_TO_GLOBAL.get(direction2));
			return blockCenterToCorner(transformation3);
		}
	}
}
