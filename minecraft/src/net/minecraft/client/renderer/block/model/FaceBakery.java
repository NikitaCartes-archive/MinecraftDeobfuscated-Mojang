package net.minecraft.client.renderer.block.model;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class FaceBakery {
	private static final float RESCALE_22_5 = 1.0F / (float)Math.cos((float) (Math.PI / 8)) - 1.0F;
	private static final float RESCALE_45 = 1.0F / (float)Math.cos((float) (Math.PI / 4)) - 1.0F;
	private static final FaceBakery.Rotation[] BY_INDEX = new FaceBakery.Rotation[BlockModelRotation.values().length * Direction.values().length];
	private static final FaceBakery.Rotation ROT_0 = new FaceBakery.Rotation() {
		@Override
		BlockFaceUV apply(float f, float g, float h, float i) {
			return new BlockFaceUV(new float[]{f, g, h, i}, 0);
		}
	};
	private static final FaceBakery.Rotation ROT_90 = new FaceBakery.Rotation() {
		@Override
		BlockFaceUV apply(float f, float g, float h, float i) {
			return new BlockFaceUV(new float[]{i, 16.0F - f, g, 16.0F - h}, 270);
		}
	};
	private static final FaceBakery.Rotation ROT_180 = new FaceBakery.Rotation() {
		@Override
		BlockFaceUV apply(float f, float g, float h, float i) {
			return new BlockFaceUV(new float[]{16.0F - f, 16.0F - g, 16.0F - h, 16.0F - i}, 0);
		}
	};
	private static final FaceBakery.Rotation ROT_270 = new FaceBakery.Rotation() {
		@Override
		BlockFaceUV apply(float f, float g, float h, float i) {
			return new BlockFaceUV(new float[]{16.0F - g, h, 16.0F - i, f}, 90);
		}
	};

	public BakedQuad bakeQuad(
		Vector3f vector3f,
		Vector3f vector3f2,
		BlockElementFace blockElementFace,
		TextureAtlasSprite textureAtlasSprite,
		Direction direction,
		ModelState modelState,
		@Nullable BlockElementRotation blockElementRotation,
		boolean bl
	) {
		BlockFaceUV blockFaceUV = blockElementFace.uv;
		if (modelState.isUvLocked()) {
			blockFaceUV = this.recomputeUVs(blockElementFace.uv, direction, modelState.getRotation());
		}

		float[] fs = new float[blockFaceUV.uvs.length];
		System.arraycopy(blockFaceUV.uvs, 0, fs, 0, fs.length);
		float f = (float)textureAtlasSprite.getWidth() / (textureAtlasSprite.getU1() - textureAtlasSprite.getU0());
		float g = (float)textureAtlasSprite.getHeight() / (textureAtlasSprite.getV1() - textureAtlasSprite.getV0());
		float h = 4.0F / Math.max(g, f);
		float i = (blockFaceUV.uvs[0] + blockFaceUV.uvs[0] + blockFaceUV.uvs[2] + blockFaceUV.uvs[2]) / 4.0F;
		float j = (blockFaceUV.uvs[1] + blockFaceUV.uvs[1] + blockFaceUV.uvs[3] + blockFaceUV.uvs[3]) / 4.0F;
		blockFaceUV.uvs[0] = Mth.lerp(h, blockFaceUV.uvs[0], i);
		blockFaceUV.uvs[2] = Mth.lerp(h, blockFaceUV.uvs[2], i);
		blockFaceUV.uvs[1] = Mth.lerp(h, blockFaceUV.uvs[1], j);
		blockFaceUV.uvs[3] = Mth.lerp(h, blockFaceUV.uvs[3], j);
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

	private BlockFaceUV recomputeUVs(BlockFaceUV blockFaceUV, Direction direction, BlockModelRotation blockModelRotation) {
		return BY_INDEX[getIndex(blockModelRotation, direction)].recompute(blockFaceUV);
	}

	private int[] makeVertices(
		BlockFaceUV blockFaceUV,
		TextureAtlasSprite textureAtlasSprite,
		Direction direction,
		float[] fs,
		BlockModelRotation blockModelRotation,
		@Nullable BlockElementRotation blockElementRotation,
		boolean bl
	) {
		int[] is = new int[28];

		for (int i = 0; i < 4; i++) {
			this.bakeVertex(is, i, direction, blockFaceUV, fs, textureAtlasSprite, blockModelRotation, blockElementRotation, bl);
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
		BlockModelRotation blockModelRotation,
		@Nullable BlockElementRotation blockElementRotation,
		boolean bl
	) {
		Direction direction2 = blockModelRotation.rotate(direction);
		int j = bl ? this.getShadeValue(direction2) : -1;
		FaceInfo.VertexInfo vertexInfo = FaceInfo.fromFacing(direction).getVertexInfo(i);
		Vector3f vector3f = new Vector3f(fs[vertexInfo.xFace], fs[vertexInfo.yFace], fs[vertexInfo.zFace]);
		this.applyElementRotation(vector3f, blockElementRotation);
		int k = this.applyModelRotation(vector3f, direction, i, blockModelRotation);
		this.fillVertex(is, k, i, vector3f, j, textureAtlasSprite, blockFaceUV);
	}

	private void fillVertex(int[] is, int i, int j, Vector3f vector3f, int k, TextureAtlasSprite textureAtlasSprite, BlockFaceUV blockFaceUV) {
		int l = i * 7;
		is[l] = Float.floatToRawIntBits(vector3f.x());
		is[l + 1] = Float.floatToRawIntBits(vector3f.y());
		is[l + 2] = Float.floatToRawIntBits(vector3f.z());
		is[l + 3] = k;
		is[l + 4] = Float.floatToRawIntBits(textureAtlasSprite.getU((double)blockFaceUV.getU(j)));
		is[l + 4 + 1] = Float.floatToRawIntBits(textureAtlasSprite.getV((double)blockFaceUV.getV(j)));
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

			this.rotateVertexBy(vector3f, new Vector3f(blockElementRotation.origin), quaternion, vector3f3);
		}
	}

	public int applyModelRotation(Vector3f vector3f, Direction direction, int i, BlockModelRotation blockModelRotation) {
		if (blockModelRotation == BlockModelRotation.X0_Y0) {
			return i;
		} else {
			this.rotateVertexBy(vector3f, new Vector3f(0.5F, 0.5F, 0.5F), blockModelRotation.getRotationQuaternion(), new Vector3f(1.0F, 1.0F, 1.0F));
			return blockModelRotation.rotateVertexIndex(direction, i);
		}
	}

	private void rotateVertexBy(Vector3f vector3f, Vector3f vector3f2, Quaternion quaternion, Vector3f vector3f3) {
		Vector4f vector4f = new Vector4f(vector3f.x() - vector3f2.x(), vector3f.y() - vector3f2.y(), vector3f.z() - vector3f2.z(), 1.0F);
		vector4f.transform(quaternion);
		vector4f.mul(vector3f3);
		vector3f.set(vector4f.x() + vector3f2.x(), vector4f.y() + vector3f2.y(), vector4f.z() + vector3f2.z());
	}

	public static Direction calculateFacing(int[] is) {
		Vector3f vector3f = new Vector3f(Float.intBitsToFloat(is[0]), Float.intBitsToFloat(is[1]), Float.intBitsToFloat(is[2]));
		Vector3f vector3f2 = new Vector3f(Float.intBitsToFloat(is[7]), Float.intBitsToFloat(is[8]), Float.intBitsToFloat(is[9]));
		Vector3f vector3f3 = new Vector3f(Float.intBitsToFloat(is[14]), Float.intBitsToFloat(is[15]), Float.intBitsToFloat(is[16]));
		Vector3f vector3f4 = new Vector3f(vector3f);
		vector3f4.sub(vector3f2);
		Vector3f vector3f5 = new Vector3f(vector3f3);
		vector3f5.sub(vector3f2);
		Vector3f vector3f6 = new Vector3f(vector3f5);
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
			int j = 7 * i;
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
			int k = 7 * jx;
			FaceInfo.VertexInfo vertexInfo = faceInfo.getVertexInfo(jx);
			float hx = fs[vertexInfo.xFace];
			float l = fs[vertexInfo.yFace];
			float m = fs[vertexInfo.zFace];
			is[k] = Float.floatToRawIntBits(hx);
			is[k + 1] = Float.floatToRawIntBits(l);
			is[k + 2] = Float.floatToRawIntBits(m);

			for (int n = 0; n < 4; n++) {
				int o = 7 * n;
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

	private static void register(BlockModelRotation blockModelRotation, Direction direction, FaceBakery.Rotation rotation) {
		BY_INDEX[getIndex(blockModelRotation, direction)] = rotation;
	}

	private static int getIndex(BlockModelRotation blockModelRotation, Direction direction) {
		return BlockModelRotation.values().length * direction.ordinal() + blockModelRotation.ordinal();
	}

	static {
		register(BlockModelRotation.X0_Y0, Direction.DOWN, ROT_0);
		register(BlockModelRotation.X0_Y0, Direction.EAST, ROT_0);
		register(BlockModelRotation.X0_Y0, Direction.NORTH, ROT_0);
		register(BlockModelRotation.X0_Y0, Direction.SOUTH, ROT_0);
		register(BlockModelRotation.X0_Y0, Direction.UP, ROT_0);
		register(BlockModelRotation.X0_Y0, Direction.WEST, ROT_0);
		register(BlockModelRotation.X0_Y90, Direction.EAST, ROT_0);
		register(BlockModelRotation.X0_Y90, Direction.NORTH, ROT_0);
		register(BlockModelRotation.X0_Y90, Direction.SOUTH, ROT_0);
		register(BlockModelRotation.X0_Y90, Direction.WEST, ROT_0);
		register(BlockModelRotation.X0_Y180, Direction.EAST, ROT_0);
		register(BlockModelRotation.X0_Y180, Direction.NORTH, ROT_0);
		register(BlockModelRotation.X0_Y180, Direction.SOUTH, ROT_0);
		register(BlockModelRotation.X0_Y180, Direction.WEST, ROT_0);
		register(BlockModelRotation.X0_Y270, Direction.EAST, ROT_0);
		register(BlockModelRotation.X0_Y270, Direction.NORTH, ROT_0);
		register(BlockModelRotation.X0_Y270, Direction.SOUTH, ROT_0);
		register(BlockModelRotation.X0_Y270, Direction.WEST, ROT_0);
		register(BlockModelRotation.X90_Y0, Direction.DOWN, ROT_0);
		register(BlockModelRotation.X90_Y0, Direction.SOUTH, ROT_0);
		register(BlockModelRotation.X90_Y90, Direction.DOWN, ROT_0);
		register(BlockModelRotation.X90_Y180, Direction.DOWN, ROT_0);
		register(BlockModelRotation.X90_Y180, Direction.NORTH, ROT_0);
		register(BlockModelRotation.X90_Y270, Direction.DOWN, ROT_0);
		register(BlockModelRotation.X180_Y0, Direction.DOWN, ROT_0);
		register(BlockModelRotation.X180_Y0, Direction.UP, ROT_0);
		register(BlockModelRotation.X270_Y0, Direction.SOUTH, ROT_0);
		register(BlockModelRotation.X270_Y0, Direction.UP, ROT_0);
		register(BlockModelRotation.X270_Y90, Direction.UP, ROT_0);
		register(BlockModelRotation.X270_Y180, Direction.NORTH, ROT_0);
		register(BlockModelRotation.X270_Y180, Direction.UP, ROT_0);
		register(BlockModelRotation.X270_Y270, Direction.UP, ROT_0);
		register(BlockModelRotation.X0_Y270, Direction.UP, ROT_90);
		register(BlockModelRotation.X0_Y90, Direction.DOWN, ROT_90);
		register(BlockModelRotation.X90_Y0, Direction.WEST, ROT_90);
		register(BlockModelRotation.X90_Y90, Direction.WEST, ROT_90);
		register(BlockModelRotation.X90_Y180, Direction.WEST, ROT_90);
		register(BlockModelRotation.X90_Y270, Direction.NORTH, ROT_90);
		register(BlockModelRotation.X90_Y270, Direction.SOUTH, ROT_90);
		register(BlockModelRotation.X90_Y270, Direction.WEST, ROT_90);
		register(BlockModelRotation.X180_Y90, Direction.UP, ROT_90);
		register(BlockModelRotation.X180_Y270, Direction.DOWN, ROT_90);
		register(BlockModelRotation.X270_Y0, Direction.EAST, ROT_90);
		register(BlockModelRotation.X270_Y90, Direction.EAST, ROT_90);
		register(BlockModelRotation.X270_Y90, Direction.NORTH, ROT_90);
		register(BlockModelRotation.X270_Y90, Direction.SOUTH, ROT_90);
		register(BlockModelRotation.X270_Y180, Direction.EAST, ROT_90);
		register(BlockModelRotation.X270_Y270, Direction.EAST, ROT_90);
		register(BlockModelRotation.X0_Y180, Direction.DOWN, ROT_180);
		register(BlockModelRotation.X0_Y180, Direction.UP, ROT_180);
		register(BlockModelRotation.X90_Y0, Direction.NORTH, ROT_180);
		register(BlockModelRotation.X90_Y0, Direction.UP, ROT_180);
		register(BlockModelRotation.X90_Y90, Direction.UP, ROT_180);
		register(BlockModelRotation.X90_Y180, Direction.SOUTH, ROT_180);
		register(BlockModelRotation.X90_Y180, Direction.UP, ROT_180);
		register(BlockModelRotation.X90_Y270, Direction.UP, ROT_180);
		register(BlockModelRotation.X180_Y0, Direction.EAST, ROT_180);
		register(BlockModelRotation.X180_Y0, Direction.NORTH, ROT_180);
		register(BlockModelRotation.X180_Y0, Direction.SOUTH, ROT_180);
		register(BlockModelRotation.X180_Y0, Direction.WEST, ROT_180);
		register(BlockModelRotation.X180_Y90, Direction.EAST, ROT_180);
		register(BlockModelRotation.X180_Y90, Direction.NORTH, ROT_180);
		register(BlockModelRotation.X180_Y90, Direction.SOUTH, ROT_180);
		register(BlockModelRotation.X180_Y90, Direction.WEST, ROT_180);
		register(BlockModelRotation.X180_Y180, Direction.DOWN, ROT_180);
		register(BlockModelRotation.X180_Y180, Direction.EAST, ROT_180);
		register(BlockModelRotation.X180_Y180, Direction.NORTH, ROT_180);
		register(BlockModelRotation.X180_Y180, Direction.SOUTH, ROT_180);
		register(BlockModelRotation.X180_Y180, Direction.UP, ROT_180);
		register(BlockModelRotation.X180_Y180, Direction.WEST, ROT_180);
		register(BlockModelRotation.X180_Y270, Direction.EAST, ROT_180);
		register(BlockModelRotation.X180_Y270, Direction.NORTH, ROT_180);
		register(BlockModelRotation.X180_Y270, Direction.SOUTH, ROT_180);
		register(BlockModelRotation.X180_Y270, Direction.WEST, ROT_180);
		register(BlockModelRotation.X270_Y0, Direction.DOWN, ROT_180);
		register(BlockModelRotation.X270_Y0, Direction.NORTH, ROT_180);
		register(BlockModelRotation.X270_Y90, Direction.DOWN, ROT_180);
		register(BlockModelRotation.X270_Y180, Direction.DOWN, ROT_180);
		register(BlockModelRotation.X270_Y180, Direction.SOUTH, ROT_180);
		register(BlockModelRotation.X270_Y270, Direction.DOWN, ROT_180);
		register(BlockModelRotation.X0_Y90, Direction.UP, ROT_270);
		register(BlockModelRotation.X0_Y270, Direction.DOWN, ROT_270);
		register(BlockModelRotation.X90_Y0, Direction.EAST, ROT_270);
		register(BlockModelRotation.X90_Y90, Direction.EAST, ROT_270);
		register(BlockModelRotation.X90_Y90, Direction.NORTH, ROT_270);
		register(BlockModelRotation.X90_Y90, Direction.SOUTH, ROT_270);
		register(BlockModelRotation.X90_Y180, Direction.EAST, ROT_270);
		register(BlockModelRotation.X90_Y270, Direction.EAST, ROT_270);
		register(BlockModelRotation.X270_Y0, Direction.WEST, ROT_270);
		register(BlockModelRotation.X180_Y90, Direction.DOWN, ROT_270);
		register(BlockModelRotation.X180_Y270, Direction.UP, ROT_270);
		register(BlockModelRotation.X270_Y90, Direction.WEST, ROT_270);
		register(BlockModelRotation.X270_Y180, Direction.WEST, ROT_270);
		register(BlockModelRotation.X270_Y270, Direction.NORTH, ROT_270);
		register(BlockModelRotation.X270_Y270, Direction.SOUTH, ROT_270);
		register(BlockModelRotation.X270_Y270, Direction.WEST, ROT_270);
	}

	@Environment(EnvType.CLIENT)
	abstract static class Rotation {
		private Rotation() {
		}

		public BlockFaceUV recompute(BlockFaceUV blockFaceUV) {
			float f = blockFaceUV.getU(blockFaceUV.getReverseIndex(0));
			float g = blockFaceUV.getV(blockFaceUV.getReverseIndex(0));
			float h = blockFaceUV.getU(blockFaceUV.getReverseIndex(2));
			float i = blockFaceUV.getV(blockFaceUV.getReverseIndex(2));
			return this.apply(f, g, h, i);
		}

		abstract BlockFaceUV apply(float f, float g, float h, float i);
	}
}
