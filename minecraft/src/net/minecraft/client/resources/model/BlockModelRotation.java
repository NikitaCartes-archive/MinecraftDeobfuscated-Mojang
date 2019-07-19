package net.minecraft.client.resources.model;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public enum BlockModelRotation implements ModelState {
	X0_Y0(0, 0),
	X0_Y90(0, 90),
	X0_Y180(0, 180),
	X0_Y270(0, 270),
	X90_Y0(90, 0),
	X90_Y90(90, 90),
	X90_Y180(90, 180),
	X90_Y270(90, 270),
	X180_Y0(180, 0),
	X180_Y90(180, 90),
	X180_Y180(180, 180),
	X180_Y270(180, 270),
	X270_Y0(270, 0),
	X270_Y90(270, 90),
	X270_Y180(270, 180),
	X270_Y270(270, 270);

	private static final Map<Integer, BlockModelRotation> BY_INDEX = (Map<Integer, BlockModelRotation>)Arrays.stream(values())
		.sorted(Comparator.comparingInt(blockModelRotation -> blockModelRotation.index))
		.collect(Collectors.toMap(blockModelRotation -> blockModelRotation.index, blockModelRotation -> blockModelRotation));
	private final int index;
	private final Quaternion rotation;
	private final int xSteps;
	private final int ySteps;

	private static int getIndex(int i, int j) {
		return i * 360 + j;
	}

	private BlockModelRotation(int j, int k) {
		this.index = getIndex(j, k);
		Quaternion quaternion = new Quaternion(new Vector3f(0.0F, 1.0F, 0.0F), (float)(-k), true);
		quaternion.mul(new Quaternion(new Vector3f(1.0F, 0.0F, 0.0F), (float)(-j), true));
		this.rotation = quaternion;
		this.xSteps = Mth.abs(j / 90);
		this.ySteps = Mth.abs(k / 90);
	}

	@Override
	public BlockModelRotation getRotation() {
		return this;
	}

	public Quaternion getRotationQuaternion() {
		return this.rotation;
	}

	public Direction rotate(Direction direction) {
		Direction direction2 = direction;

		for (int i = 0; i < this.xSteps; i++) {
			direction2 = direction2.getClockWise(Direction.Axis.X);
		}

		if (direction2.getAxis() != Direction.Axis.Y) {
			for (int i = 0; i < this.ySteps; i++) {
				direction2 = direction2.getClockWise(Direction.Axis.Y);
			}
		}

		return direction2;
	}

	public int rotateVertexIndex(Direction direction, int i) {
		int j = i;
		if (direction.getAxis() == Direction.Axis.X) {
			j = (i + this.xSteps) % 4;
		}

		Direction direction2 = direction;

		for (int k = 0; k < this.xSteps; k++) {
			direction2 = direction2.getClockWise(Direction.Axis.X);
		}

		if (direction2.getAxis() == Direction.Axis.Y) {
			j = (j + this.ySteps) % 4;
		}

		return j;
	}

	public static BlockModelRotation by(int i, int j) {
		return (BlockModelRotation)BY_INDEX.get(getIndex(Mth.positiveModulo(i, 360), Mth.positiveModulo(j, 360)));
	}
}
