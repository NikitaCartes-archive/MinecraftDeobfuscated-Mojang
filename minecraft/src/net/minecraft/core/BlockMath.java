package net.minecraft.core;

import com.google.common.collect.Maps;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import java.util.EnumMap;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class BlockMath {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final EnumMap<Direction, Transformation> vanillaUvTransformLocalToGlobal = Util.make(Maps.newEnumMap(Direction.class), enumMap -> {
		enumMap.put(Direction.SOUTH, Transformation.identity());
		enumMap.put(Direction.EAST, new Transformation(null, new Quaternion(new Vector3f(0.0F, 1.0F, 0.0F), 90.0F, true), null, null));
		enumMap.put(Direction.WEST, new Transformation(null, new Quaternion(new Vector3f(0.0F, 1.0F, 0.0F), -90.0F, true), null, null));
		enumMap.put(Direction.NORTH, new Transformation(null, new Quaternion(new Vector3f(0.0F, 1.0F, 0.0F), 180.0F, true), null, null));
		enumMap.put(Direction.UP, new Transformation(null, new Quaternion(new Vector3f(1.0F, 0.0F, 0.0F), -90.0F, true), null, null));
		enumMap.put(Direction.DOWN, new Transformation(null, new Quaternion(new Vector3f(1.0F, 0.0F, 0.0F), 90.0F, true), null, null));
	});
	public static final EnumMap<Direction, Transformation> vanillaUvTransformGlobalToLocal = Util.make(Maps.newEnumMap(Direction.class), enumMap -> {
		for (Direction direction : Direction.values()) {
			enumMap.put(direction, ((Transformation)vanillaUvTransformLocalToGlobal.get(direction)).inverse());
		}
	});

	public static Transformation blockCenterToCorner(Transformation transformation) {
		Matrix4f matrix4f = Matrix4f.createTranslateMatrix(0.5F, 0.5F, 0.5F);
		matrix4f.multiply(transformation.getMatrix());
		matrix4f.multiply(Matrix4f.createTranslateMatrix(-0.5F, -0.5F, -0.5F));
		return new Transformation(matrix4f);
	}

	public static Transformation getUVLockTransform(Transformation transformation, Direction direction, Supplier<String> supplier) {
		Direction direction2 = Direction.rotate(transformation.getMatrix(), direction);
		Transformation transformation2 = transformation.inverse();
		if (transformation2 == null) {
			LOGGER.warn((String)supplier.get());
			return new Transformation(null, null, new Vector3f(0.0F, 0.0F, 0.0F), null);
		} else {
			Transformation transformation3 = ((Transformation)vanillaUvTransformGlobalToLocal.get(direction))
				.compose(transformation2)
				.compose((Transformation)vanillaUvTransformLocalToGlobal.get(direction2));
			return blockCenterToCorner(transformation3);
		}
	}
}
