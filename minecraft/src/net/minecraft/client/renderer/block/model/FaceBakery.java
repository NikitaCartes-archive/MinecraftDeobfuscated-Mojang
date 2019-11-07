package net.minecraft.client.renderer.block.model;

import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockMath;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class FaceBakery {
	private static final float RESCALE_22_5 = 1.0F / (float)Math.cos((float) (Math.PI / 8)) - 1.0F;
	private static final float RESCALE_45 = 1.0F / (float)Math.cos((float) (Math.PI / 4)) - 1.0F;

	public BakedQuad bakeQuad(
		Vector3f vector3f,
		Vector3f vector3f2,
		BlockElementFace blockElementFace,
		TextureAtlasSprite textureAtlasSprite,
		Direction direction,
		ModelState modelState,
		@Nullable BlockElementRotation blockElementRotation,
		boolean bl,
		ResourceLocation resourceLocation
	) {
		BlockFaceUV blockFaceUV = blockElementFace.uv;
		if (modelState.isUvLocked()) {
			blockFaceUV = recomputeUVs(blockElementFace.uv, direction, modelState.getRotation(), resourceLocation);
		}

		float[] fs = new float[blockFaceUV.uvs.length];
		System.arraycopy(blockFaceUV.uvs, 0, fs, 0, fs.length);
		float f = textureAtlasSprite.uvShrinkRatio();
		float g = (blockFaceUV.uvs[0] + blockFaceUV.uvs[0] + blockFaceUV.uvs[2] + blockFaceUV.uvs[2]) / 4.0F;
		float h = (blockFaceUV.uvs[1] + blockFaceUV.uvs[1] + blockFaceUV.uvs[3] + blockFaceUV.uvs[3]) / 4.0F;
		blockFaceUV.uvs[0] = Mth.lerp(f, blockFaceUV.uvs[0], g);
		blockFaceUV.uvs[2] = Mth.lerp(f, blockFaceUV.uvs[2], g);
		blockFaceUV.uvs[1] = Mth.lerp(f, blockFaceUV.uvs[1], h);
		blockFaceUV.uvs[3] = Mth.lerp(f, blockFaceUV.uvs[3], h);
		int[] is = this.makeVertices(
			blockFaceUV, textureAtlasSprite, direction, this.setupShape(vector3f, vector3f2), modelState.getRotation(), blockElementRotation, bl
		);
		Direction direction2 = calculateFacing(is);
		System.arraycopy(fs, 0, blockFaceUV.uvs, 0, fs.length);
		if (blockElementRotation == null) {
			this.recalculateWinding(is, direction2);
		}

		return new BakedQuad(is, blockElementFace.tintIndex, direction2, textureAtlasSprite);
	}

	public static BlockFaceUV recomputeUVs(BlockFaceUV blockFaceUV, Direction direction, Transformation transformation, ResourceLocation resourceLocation) {
		Matrix4f matrix4f = BlockMath.getUVLockTransform(transformation, direction, () -> "Unable to resolve UVLock for model: " + resourceLocation).getMatrix();
		float f = blockFaceUV.getU(blockFaceUV.getReverseIndex(0));
		float g = blockFaceUV.getV(blockFaceUV.getReverseIndex(0));
		Vector4f vector4f = new Vector4f(f / 16.0F, g / 16.0F, 0.0F, 1.0F);
		vector4f.transform(matrix4f);
		float h = 16.0F * vector4f.x();
		float i = 16.0F * vector4f.y();
		float j = blockFaceUV.getU(blockFaceUV.getReverseIndex(2));
		float k = blockFaceUV.getV(blockFaceUV.getReverseIndex(2));
		Vector4f vector4f2 = new Vector4f(j / 16.0F, k / 16.0F, 0.0F, 1.0F);
		vector4f2.transform(matrix4f);
		float l = 16.0F * vector4f2.x();
		float m = 16.0F * vector4f2.y();
		float n;
		float o;
		if (Math.signum(j - f) == Math.signum(l - h)) {
			n = h;
			o = l;
		} else {
			n = l;
			o = h;
		}

		float p;
		float q;
		if (Math.signum(k - g) == Math.signum(m - i)) {
			p = i;
			q = m;
		} else {
			p = m;
			q = i;
		}

		float r = (float)Math.toRadians((double)blockFaceUV.rotation);
		Vector3f vector3f = new Vector3f(Mth.cos(r), Mth.sin(r), 0.0F);
		Matrix3f matrix3f = new Matrix3f(matrix4f);
		vector3f.transform(matrix3f);
		int s = Math.floorMod(-((int)Math.round(Math.toDegrees(Math.atan2((double)vector3f.y(), (double)vector3f.x())) / 90.0)) * 90, 360);
		return new BlockFaceUV(new float[]{n, p, o, q}, s);
	}

	private int[] makeVertices(
		BlockFaceUV blockFaceUV,
		TextureAtlasSprite textureAtlasSprite,
		Direction direction,
		float[] fs,
		Transformation transformation,
		@Nullable BlockElementRotation blockElementRotation,
		boolean bl
	) {
		int[] is = new int[32];

		for (int i = 0; i < 4; i++) {
			this.bakeVertex(is, i, direction, blockFaceUV, fs, textureAtlasSprite, transformation, blockElementRotation, bl);
		}

		return is;
	}

	private int getShadeValue(Direction direction) {
		float f = this.getShade(direction);
		int i = Mth.clamp((int)(f * 255.0F), 0, 255);
		return 0xFF000000 | i << 16 | i << 8 | i;
	}

	private float getShade(Direction direction) {
		switch (direction) {
			case DOWN:
				return 0.5F;
			case UP:
				return 1.0F;
			case NORTH:
			case SOUTH:
				return 0.8F;
			case WEST:
			case EAST:
				return 0.6F;
			default:
				return 1.0F;
		}
	}

	private float[] setupShape(Vector3f vector3f, Vector3f vector3f2) {
		float[] fs = new float[Direction.values().length];
		fs[FaceInfo.Constants.MIN_X] = vector3f.x() / 16.0F;
		fs[FaceInfo.Constants.MIN_Y] = vector3f.y() / 16.0F;
		fs[FaceInfo.Constants.MIN_Z] = vector3f.z() / 16.0F;
		fs[FaceInfo.Constants.MAX_X] = vector3f2.x() / 16.0F;
		fs[FaceInfo.Constants.MAX_Y] = vector3f2.y() / 16.0F;
		fs[FaceInfo.Constants.MAX_Z] = vector3f2.z() / 16.0F;
		return fs;
	}

	private void bakeVertex(
		int[] is,
		int i,
		Direction direction,
		BlockFaceUV blockFaceUV,
		float[] fs,
		TextureAtlasSprite textureAtlasSprite,
		Transformation transformation,
		@Nullable BlockElementRotation blockElementRotation,
		boolean bl
	) {
		Direction direction2 = Direction.rotate(transformation.getMatrix(), direction);
		int j = bl ? this.getShadeValue(direction2) : -1;
		FaceInfo.VertexInfo vertexInfo = FaceInfo.fromFacing(direction).getVertexInfo(i);
		Vector3f vector3f = new Vector3f(fs[vertexInfo.xFace], fs[vertexInfo.yFace], fs[vertexInfo.zFace]);
		this.applyElementRotation(vector3f, blockElementRotation);
		this.applyModelRotation(vector3f, transformation);
		this.fillVertex(is, i, vector3f, j, textureAtlasSprite, blockFaceUV);
	}

	private void fillVertex(int[] is, int i, Vector3f vector3f, int j, TextureAtlasSprite textureAtlasSprite, BlockFaceUV blockFaceUV) {
		int k = i * 8;
		is[k] = Float.floatToRawIntBits(vector3f.x());
		is[k + 1] = Float.floatToRawIntBits(vector3f.y());
		is[k + 2] = Float.floatToRawIntBits(vector3f.z());
		is[k + 3] = j;
		is[k + 4] = Float.floatToRawIntBits(textureAtlasSprite.getU((double)blockFaceUV.getU(i)));
		is[k + 4 + 1] = Float.floatToRawIntBits(textureAtlasSprite.getV((double)blockFaceUV.getV(i)));
	}

	private void applyElementRotation(Vector3f vector3f, @Nullable BlockElementRotation blockElementRotation) {
		if (blockElementRotation != null) {
			Vector3f vector3f2;
			Vector3f vector3f3;
			switch (blockElementRotation.axis) {
				case X:
					vector3f2 = new Vector3f(1.0F, 0.0F, 0.0F);
					vector3f3 = new Vector3f(0.0F, 1.0F, 1.0F);
					break;
				case Y:
					vector3f2 = new Vector3f(0.0F, 1.0F, 0.0F);
					vector3f3 = new Vector3f(1.0F, 0.0F, 1.0F);
					break;
				case Z:
					vector3f2 = new Vector3f(0.0F, 0.0F, 1.0F);
					vector3f3 = new Vector3f(1.0F, 1.0F, 0.0F);
					break;
				default:
					throw new IllegalArgumentException("There are only 3 axes");
			}

			Quaternion quaternion = new Quaternion(vector3f2, blockElementRotation.angle, true);
			if (blockElementRotation.rescale) {
				if (Math.abs(blockElementRotation.angle) == 22.5F) {
					vector3f3.mul(RESCALE_22_5);
				} else {
					vector3f3.mul(RESCALE_45);
				}

				vector3f3.add(1.0F, 1.0F, 1.0F);
			} else {
				vector3f3.set(1.0F, 1.0F, 1.0F);
			}

			this.rotateVertexBy(vector3f, blockElementRotation.origin.copy(), new Matrix4f(quaternion), vector3f3);
		}
	}

	public void applyModelRotation(Vector3f vector3f, Transformation transformation) {
		if (transformation != Transformation.identity()) {
			this.rotateVertexBy(vector3f, new Vector3f(0.5F, 0.5F, 0.5F), transformation.getMatrix(), new Vector3f(1.0F, 1.0F, 1.0F));
		}
	}

	private void rotateVertexBy(Vector3f vector3f, Vector3f vector3f2, Matrix4f matrix4f, Vector3f vector3f3) {
		Vector4f vector4f = new Vector4f(vector3f.x() - vector3f2.x(), vector3f.y() - vector3f2.y(), vector3f.z() - vector3f2.z(), 1.0F);
		vector4f.transform(matrix4f);
		vector4f.mul(vector3f3);
		vector3f.set(vector4f.x() + vector3f2.x(), vector4f.y() + vector3f2.y(), vector4f.z() + vector3f2.z());
	}

	public static Direction calculateFacing(int[] is) {
		Vector3f vector3f = new Vector3f(Float.intBitsToFloat(is[0]), Float.intBitsToFloat(is[1]), Float.intBitsToFloat(is[2]));
		Vector3f vector3f2 = new Vector3f(Float.intBitsToFloat(is[8]), Float.intBitsToFloat(is[9]), Float.intBitsToFloat(is[10]));
		Vector3f vector3f3 = new Vector3f(Float.intBitsToFloat(is[16]), Float.intBitsToFloat(is[17]), Float.intBitsToFloat(is[18]));
		Vector3f vector3f4 = vector3f.copy();
		vector3f4.sub(vector3f2);
		Vector3f vector3f5 = vector3f3.copy();
		vector3f5.sub(vector3f2);
		Vector3f vector3f6 = vector3f5.copy();
		vector3f6.cross(vector3f4);
		vector3f6.normalize();
		Direction direction = null;
		float f = 0.0F;

		for (Direction direction2 : Direction.values()) {
			Vec3i vec3i = direction2.getNormal();
			Vector3f vector3f7 = new Vector3f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
			float g = vector3f6.dot(vector3f7);
			if (g >= 0.0F && g > f) {
				f = g;
				direction = direction2;
			}
		}

		return direction == null ? Direction.UP : direction;
	}

	private void recalculateWinding(int[] is, Direction direction) {
		int[] js = new int[is.length];
		System.arraycopy(is, 0, js, 0, is.length);
		float[] fs = new float[Direction.values().length];
		fs[FaceInfo.Constants.MIN_X] = 999.0F;
		fs[FaceInfo.Constants.MIN_Y] = 999.0F;
		fs[FaceInfo.Constants.MIN_Z] = 999.0F;
		fs[FaceInfo.Constants.MAX_X] = -999.0F;
		fs[FaceInfo.Constants.MAX_Y] = -999.0F;
		fs[FaceInfo.Constants.MAX_Z] = -999.0F;

		for (int i = 0; i < 4; i++) {
			int j = 8 * i;
			float f = Float.intBitsToFloat(js[j]);
			float g = Float.intBitsToFloat(js[j + 1]);
			float h = Float.intBitsToFloat(js[j + 2]);
			if (f < fs[FaceInfo.Constants.MIN_X]) {
				fs[FaceInfo.Constants.MIN_X] = f;
			}

			if (g < fs[FaceInfo.Constants.MIN_Y]) {
				fs[FaceInfo.Constants.MIN_Y] = g;
			}

			if (h < fs[FaceInfo.Constants.MIN_Z]) {
				fs[FaceInfo.Constants.MIN_Z] = h;
			}

			if (f > fs[FaceInfo.Constants.MAX_X]) {
				fs[FaceInfo.Constants.MAX_X] = f;
			}

			if (g > fs[FaceInfo.Constants.MAX_Y]) {
				fs[FaceInfo.Constants.MAX_Y] = g;
			}

			if (h > fs[FaceInfo.Constants.MAX_Z]) {
				fs[FaceInfo.Constants.MAX_Z] = h;
			}
		}

		FaceInfo faceInfo = FaceInfo.fromFacing(direction);

		for (int jx = 0; jx < 4; jx++) {
			int k = 8 * jx;
			FaceInfo.VertexInfo vertexInfo = faceInfo.getVertexInfo(jx);
			float hx = fs[vertexInfo.xFace];
			float l = fs[vertexInfo.yFace];
			float m = fs[vertexInfo.zFace];
			is[k] = Float.floatToRawIntBits(hx);
			is[k + 1] = Float.floatToRawIntBits(l);
			is[k + 2] = Float.floatToRawIntBits(m);

			for (int n = 0; n < 4; n++) {
				int o = 8 * n;
				float p = Float.intBitsToFloat(js[o]);
				float q = Float.intBitsToFloat(js[o + 1]);
				float r = Float.intBitsToFloat(js[o + 2]);
				if (Mth.equal(hx, p) && Mth.equal(l, q) && Mth.equal(m, r)) {
					is[k + 4] = js[o + 4];
					is[k + 4 + 1] = js[o + 4 + 1];
				}
			}
		}
	}
}
