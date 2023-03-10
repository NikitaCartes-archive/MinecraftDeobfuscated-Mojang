/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.block.model;

import com.mojang.math.Transformation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockMath;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

@Environment(value=EnvType.CLIENT)
public class FaceBakery {
    public static final int VERTEX_INT_SIZE = 8;
    private static final float RESCALE_22_5 = 1.0f / (float)Math.cos(0.3926991f) - 1.0f;
    private static final float RESCALE_45 = 1.0f / (float)Math.cos(0.7853981852531433) - 1.0f;
    public static final int VERTEX_COUNT = 4;
    private static final int COLOR_INDEX = 3;
    public static final int UV_INDEX = 4;

    public BakedQuad bakeQuad(Vector3f vector3f, Vector3f vector3f2, BlockElementFace blockElementFace, TextureAtlasSprite textureAtlasSprite, Direction direction, ModelState modelState, @Nullable BlockElementRotation blockElementRotation, boolean bl, ResourceLocation resourceLocation) {
        BlockFaceUV blockFaceUV = blockElementFace.uv;
        if (modelState.isUvLocked()) {
            blockFaceUV = FaceBakery.recomputeUVs(blockElementFace.uv, direction, modelState.getRotation(), resourceLocation);
        }
        float[] fs = new float[blockFaceUV.uvs.length];
        System.arraycopy(blockFaceUV.uvs, 0, fs, 0, fs.length);
        float f = textureAtlasSprite.uvShrinkRatio();
        float g = (blockFaceUV.uvs[0] + blockFaceUV.uvs[0] + blockFaceUV.uvs[2] + blockFaceUV.uvs[2]) / 4.0f;
        float h = (blockFaceUV.uvs[1] + blockFaceUV.uvs[1] + blockFaceUV.uvs[3] + blockFaceUV.uvs[3]) / 4.0f;
        blockFaceUV.uvs[0] = Mth.lerp(f, blockFaceUV.uvs[0], g);
        blockFaceUV.uvs[2] = Mth.lerp(f, blockFaceUV.uvs[2], g);
        blockFaceUV.uvs[1] = Mth.lerp(f, blockFaceUV.uvs[1], h);
        blockFaceUV.uvs[3] = Mth.lerp(f, blockFaceUV.uvs[3], h);
        int[] is = this.makeVertices(blockFaceUV, textureAtlasSprite, direction, this.setupShape(vector3f, vector3f2), modelState.getRotation(), blockElementRotation, bl);
        Direction direction2 = FaceBakery.calculateFacing(is);
        System.arraycopy(fs, 0, blockFaceUV.uvs, 0, fs.length);
        if (blockElementRotation == null) {
            this.recalculateWinding(is, direction2);
        }
        return new BakedQuad(is, blockElementFace.tintIndex, direction2, textureAtlasSprite, bl);
    }

    public static BlockFaceUV recomputeUVs(BlockFaceUV blockFaceUV, Direction direction, Transformation transformation, ResourceLocation resourceLocation) {
        float q;
        float p;
        float o;
        float n;
        Matrix4f matrix4f = BlockMath.getUVLockTransform(transformation, direction, () -> "Unable to resolve UVLock for model: " + resourceLocation).getMatrix();
        float f = blockFaceUV.getU(blockFaceUV.getReverseIndex(0));
        float g = blockFaceUV.getV(blockFaceUV.getReverseIndex(0));
        Vector4f vector4f = matrix4f.transform(new Vector4f(f / 16.0f, g / 16.0f, 0.0f, 1.0f));
        float h = 16.0f * vector4f.x();
        float i = 16.0f * vector4f.y();
        float j = blockFaceUV.getU(blockFaceUV.getReverseIndex(2));
        float k = blockFaceUV.getV(blockFaceUV.getReverseIndex(2));
        Vector4f vector4f2 = matrix4f.transform(new Vector4f(j / 16.0f, k / 16.0f, 0.0f, 1.0f));
        float l = 16.0f * vector4f2.x();
        float m = 16.0f * vector4f2.y();
        if (Math.signum(j - f) == Math.signum(l - h)) {
            n = h;
            o = l;
        } else {
            n = l;
            o = h;
        }
        if (Math.signum(k - g) == Math.signum(m - i)) {
            p = i;
            q = m;
        } else {
            p = m;
            q = i;
        }
        float r = (float)Math.toRadians(blockFaceUV.rotation);
        Matrix3f matrix3f = new Matrix3f(matrix4f);
        Vector3f vector3f = matrix3f.transform(new Vector3f(Mth.cos(r), Mth.sin(r), 0.0f));
        int s = Math.floorMod(-((int)Math.round(Math.toDegrees(Math.atan2(vector3f.y(), vector3f.x())) / 90.0)) * 90, 360);
        return new BlockFaceUV(new float[]{n, p, o, q}, s);
    }

    private int[] makeVertices(BlockFaceUV blockFaceUV, TextureAtlasSprite textureAtlasSprite, Direction direction, float[] fs, Transformation transformation, @Nullable BlockElementRotation blockElementRotation, boolean bl) {
        int[] is = new int[32];
        for (int i = 0; i < 4; ++i) {
            this.bakeVertex(is, i, direction, blockFaceUV, fs, textureAtlasSprite, transformation, blockElementRotation, bl);
        }
        return is;
    }

    private float[] setupShape(Vector3f vector3f, Vector3f vector3f2) {
        float[] fs = new float[Direction.values().length];
        fs[FaceInfo.Constants.MIN_X] = vector3f.x() / 16.0f;
        fs[FaceInfo.Constants.MIN_Y] = vector3f.y() / 16.0f;
        fs[FaceInfo.Constants.MIN_Z] = vector3f.z() / 16.0f;
        fs[FaceInfo.Constants.MAX_X] = vector3f2.x() / 16.0f;
        fs[FaceInfo.Constants.MAX_Y] = vector3f2.y() / 16.0f;
        fs[FaceInfo.Constants.MAX_Z] = vector3f2.z() / 16.0f;
        return fs;
    }

    private void bakeVertex(int[] is, int i, Direction direction, BlockFaceUV blockFaceUV, float[] fs, TextureAtlasSprite textureAtlasSprite, Transformation transformation, @Nullable BlockElementRotation blockElementRotation, boolean bl) {
        FaceInfo.VertexInfo vertexInfo = FaceInfo.fromFacing(direction).getVertexInfo(i);
        Vector3f vector3f = new Vector3f(fs[vertexInfo.xFace], fs[vertexInfo.yFace], fs[vertexInfo.zFace]);
        this.applyElementRotation(vector3f, blockElementRotation);
        this.applyModelRotation(vector3f, transformation);
        this.fillVertex(is, i, vector3f, textureAtlasSprite, blockFaceUV);
    }

    private void fillVertex(int[] is, int i, Vector3f vector3f, TextureAtlasSprite textureAtlasSprite, BlockFaceUV blockFaceUV) {
        int j = i * 8;
        is[j] = Float.floatToRawIntBits(vector3f.x());
        is[j + 1] = Float.floatToRawIntBits(vector3f.y());
        is[j + 2] = Float.floatToRawIntBits(vector3f.z());
        is[j + 3] = -1;
        is[j + 4] = Float.floatToRawIntBits(textureAtlasSprite.getU(blockFaceUV.getU(i)));
        is[j + 4 + 1] = Float.floatToRawIntBits(textureAtlasSprite.getV(blockFaceUV.getV(i)));
    }

    private void applyElementRotation(Vector3f vector3f, @Nullable BlockElementRotation blockElementRotation) {
        Vector3f vector3f2;
        if (blockElementRotation == null) {
            return;
        }
        Vector3f vector3f3 = switch (blockElementRotation.axis()) {
            case Direction.Axis.X -> {
                vector3f2 = new Vector3f(1.0f, 0.0f, 0.0f);
                yield new Vector3f(0.0f, 1.0f, 1.0f);
            }
            case Direction.Axis.Y -> {
                vector3f2 = new Vector3f(0.0f, 1.0f, 0.0f);
                yield new Vector3f(1.0f, 0.0f, 1.0f);
            }
            case Direction.Axis.Z -> {
                vector3f2 = new Vector3f(0.0f, 0.0f, 1.0f);
                yield new Vector3f(1.0f, 1.0f, 0.0f);
            }
            default -> throw new IllegalArgumentException("There are only 3 axes");
        };
        Quaternionf quaternionf = new Quaternionf().rotationAxis(blockElementRotation.angle() * ((float)Math.PI / 180), vector3f2);
        if (blockElementRotation.rescale()) {
            if (Math.abs(blockElementRotation.angle()) == 22.5f) {
                vector3f3.mul(RESCALE_22_5);
            } else {
                vector3f3.mul(RESCALE_45);
            }
            vector3f3.add(1.0f, 1.0f, 1.0f);
        } else {
            vector3f3.set(1.0f, 1.0f, 1.0f);
        }
        this.rotateVertexBy(vector3f, new Vector3f(blockElementRotation.origin()), new Matrix4f().rotation(quaternionf), vector3f3);
    }

    public void applyModelRotation(Vector3f vector3f, Transformation transformation) {
        if (transformation == Transformation.identity()) {
            return;
        }
        this.rotateVertexBy(vector3f, new Vector3f(0.5f, 0.5f, 0.5f), transformation.getMatrix(), new Vector3f(1.0f, 1.0f, 1.0f));
    }

    private void rotateVertexBy(Vector3f vector3f, Vector3f vector3f2, Matrix4f matrix4f, Vector3f vector3f3) {
        Vector4f vector4f = matrix4f.transform(new Vector4f(vector3f.x() - vector3f2.x(), vector3f.y() - vector3f2.y(), vector3f.z() - vector3f2.z(), 1.0f));
        vector4f.mul(new Vector4f(vector3f3, 1.0f));
        vector3f.set(vector4f.x() + vector3f2.x(), vector4f.y() + vector3f2.y(), vector4f.z() + vector3f2.z());
    }

    public static Direction calculateFacing(int[] is) {
        Vector3f vector3f = new Vector3f(Float.intBitsToFloat(is[0]), Float.intBitsToFloat(is[1]), Float.intBitsToFloat(is[2]));
        Vector3f vector3f2 = new Vector3f(Float.intBitsToFloat(is[8]), Float.intBitsToFloat(is[9]), Float.intBitsToFloat(is[10]));
        Vector3f vector3f3 = new Vector3f(Float.intBitsToFloat(is[16]), Float.intBitsToFloat(is[17]), Float.intBitsToFloat(is[18]));
        Vector3f vector3f4 = new Vector3f(vector3f).sub(vector3f2);
        Vector3f vector3f5 = new Vector3f(vector3f3).sub(vector3f2);
        Vector3f vector3f6 = new Vector3f(vector3f5).cross(vector3f4).normalize();
        if (!vector3f6.isFinite()) {
            return Direction.UP;
        }
        Direction direction = null;
        float f = 0.0f;
        for (Direction direction2 : Direction.values()) {
            Vec3i vec3i = direction2.getNormal();
            Vector3f vector3f7 = new Vector3f(vec3i.getX(), vec3i.getY(), vec3i.getZ());
            float g = vector3f6.dot(vector3f7);
            if (!(g >= 0.0f) || !(g > f)) continue;
            f = g;
            direction = direction2;
        }
        if (direction == null) {
            return Direction.UP;
        }
        return direction;
    }

    private void recalculateWinding(int[] is, Direction direction) {
        float h;
        int j;
        int[] js = new int[is.length];
        System.arraycopy(is, 0, js, 0, is.length);
        float[] fs = new float[Direction.values().length];
        fs[FaceInfo.Constants.MIN_X] = 999.0f;
        fs[FaceInfo.Constants.MIN_Y] = 999.0f;
        fs[FaceInfo.Constants.MIN_Z] = 999.0f;
        fs[FaceInfo.Constants.MAX_X] = -999.0f;
        fs[FaceInfo.Constants.MAX_Y] = -999.0f;
        fs[FaceInfo.Constants.MAX_Z] = -999.0f;
        for (int i = 0; i < 4; ++i) {
            j = 8 * i;
            float f = Float.intBitsToFloat(js[j]);
            float g = Float.intBitsToFloat(js[j + 1]);
            h = Float.intBitsToFloat(js[j + 2]);
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
            if (!(h > fs[FaceInfo.Constants.MAX_Z])) continue;
            fs[FaceInfo.Constants.MAX_Z] = h;
        }
        FaceInfo faceInfo = FaceInfo.fromFacing(direction);
        for (j = 0; j < 4; ++j) {
            int k = 8 * j;
            FaceInfo.VertexInfo vertexInfo = faceInfo.getVertexInfo(j);
            h = fs[vertexInfo.xFace];
            float l = fs[vertexInfo.yFace];
            float m = fs[vertexInfo.zFace];
            is[k] = Float.floatToRawIntBits(h);
            is[k + 1] = Float.floatToRawIntBits(l);
            is[k + 2] = Float.floatToRawIntBits(m);
            for (int n = 0; n < 4; ++n) {
                int o = 8 * n;
                float p = Float.intBitsToFloat(js[o]);
                float q = Float.intBitsToFloat(js[o + 1]);
                float r = Float.intBitsToFloat(js[o + 2]);
                if (!Mth.equal(h, p) || !Mth.equal(l, q) || !Mth.equal(m, r)) continue;
                is[k + 4] = js[o + 4];
                is[k + 4 + 1] = js[o + 4 + 1];
            }
        }
    }
}

