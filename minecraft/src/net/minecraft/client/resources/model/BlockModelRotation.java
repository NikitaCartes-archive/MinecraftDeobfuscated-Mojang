package net.minecraft.client.resources.model;

import com.mojang.math.OctahedralGroup;
import com.mojang.math.Quaternion;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

	private static final int DEGREES = 360;
	private static final Map<Integer, BlockModelRotation> BY_INDEX = (Map<Integer, BlockModelRotation>)Arrays.stream(values())
		.collect(Collectors.toMap(blockModelRotation -> blockModelRotation.index, blockModelRotation -> blockModelRotation));
	private final Transformation transformation;
	private final OctahedralGroup actualRotation;
	private final int index;

	private static int getIndex(int i, int j) {
		return i * 360 + j;
	}

	private BlockModelRotation(int j, int k) {
		this.index = getIndex(j, k);
		Quaternion quaternion = Vector3f.YP.rotationDegrees((float)(-k));
		quaternion.mul(Vector3f.XP.rotationDegrees((float)(-j)));
		OctahedralGroup octahedralGroup = OctahedralGroup.IDENTITY;

		for (int l = 0; l < k; l += 90) {
			octahedralGroup = octahedralGroup.compose(OctahedralGroup.ROT_90_Y_NEG);
		}

		for (int l = 0; l < j; l += 90) {
			octahedralGroup = octahedralGroup.compose(OctahedralGroup.ROT_90_X_NEG);
		}

		this.transformation = new Transformation(null, quaternion, null, null);
		this.actualRotation = octahedralGroup;
	}

	@Override
	public Transformation getRotation() {
		return this.transformation;
	}

	public static BlockModelRotation by(int i, int j) {
		return (BlockModelRotation)BY_INDEX.get(getIndex(Mth.positiveModulo(i, 360), Mth.positiveModulo(j, 360)));
	}

	public OctahedralGroup actualRotation() {
		return this.actualRotation;
	}
}
