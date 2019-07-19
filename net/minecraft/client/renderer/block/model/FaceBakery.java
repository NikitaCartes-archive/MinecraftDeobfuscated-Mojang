/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.block.model;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class FaceBakery {
    private static final float RESCALE_22_5 = 1.0f / (float)Math.cos(0.3926991f) - 1.0f;
    private static final float RESCALE_45 = 1.0f / (float)Math.cos(0.7853981852531433) - 1.0f;
    private static final Rotation[] BY_INDEX = new Rotation[BlockModelRotation.values().length * Direction.values().length];
    private static final Rotation ROT_0 = new Rotation(){

        @Override
        BlockFaceUV apply(float f, float g, float h, float i) {
            return new BlockFaceUV(new float[]{f, g, h, i}, 0);
        }
    };
    private static final Rotation ROT_90 = new Rotation(){

        @Override
        BlockFaceUV apply(float f, float g, float h, float i) {
            return new BlockFaceUV(new float[]{i, 16.0f - f, g, 16.0f - h}, 270);
        }
    };
    private static final Rotation ROT_180 = new Rotation(){

        @Override
        BlockFaceUV apply(float f, float g, float h, float i) {
            return new BlockFaceUV(new float[]{16.0f - f, 16.0f - g, 16.0f - h, 16.0f - i}, 0);
        }
    };
    private static final Rotation ROT_270 = new Rotation(){

        @Override
        BlockFaceUV apply(float f, float g, float h, float i) {
            return new BlockFaceUV(new float[]{16.0f - g, h, 16.0f - i, f}, 90);
        }
    };

    public BakedQuad bakeQuad(Vector3f vector3f, Vector3f vector3f2, BlockElementFace blockElementFace, TextureAtlasSprite textureAtlasSprite, Direction direction, ModelState modelState, @Nullable BlockElementRotation blockElementRotation, boolean bl) {
        BlockFaceUV blockFaceUV = blockElementFace.uv;
        if (modelState.isUvLocked()) {
            blockFaceUV = this.recomputeUVs(blockElementFace.uv, direction, modelState.getRotation());
        }
        float[] fs = new float[blockFaceUV.uvs.length];
        System.arraycopy(blockFaceUV.uvs, 0, fs, 0, fs.length);
        float f = (float)textureAtlasSprite.getWidth() / (textureAtlasSprite.getU1() - textureAtlasSprite.getU0());
        float g = (float)textureAtlasSprite.getHeight() / (textureAtlasSprite.getV1() - textureAtlasSprite.getV0());
        float h = 4.0f / Math.max(g, f);
        float i = (blockFaceUV.uvs[0] + blockFaceUV.uvs[0] + blockFaceUV.uvs[2] + blockFaceUV.uvs[2]) / 4.0f;
        float j = (blockFaceUV.uvs[1] + blockFaceUV.uvs[1] + blockFaceUV.uvs[3] + blockFaceUV.uvs[3]) / 4.0f;
        blockFaceUV.uvs[0] = Mth.lerp(h, blockFaceUV.uvs[0], i);
        blockFaceUV.uvs[2] = Mth.lerp(h, blockFaceUV.uvs[2], i);
        blockFaceUV.uvs[1] = Mth.lerp(h, blockFaceUV.uvs[1], j);
        blockFaceUV.uvs[3] = Mth.lerp(h, blockFaceUV.uvs[3], j);
        int[] is = this.makeVertices(blockFaceUV, textureAtlasSprite, direction, this.setupShape(vector3f, vector3f2), modelState.getRotation(), blockElementRotation, bl);
        Direction direction2 = FaceBakery.calculateFacing(is);
        System.arraycopy(fs, 0, blockFaceUV.uvs, 0, fs.length);
        if (blockElementRotation == null) {
            this.recalculateWinding(is, direction2);
        }
        return new BakedQuad(is, blockElementFace.tintIndex, direction2, textureAtlasSprite);
    }

    private BlockFaceUV recomputeUVs(BlockFaceUV blockFaceUV, Direction direction, BlockModelRotation blockModelRotation) {
        return BY_INDEX[FaceBakery.getIndex(blockModelRotation, direction)].recompute(blockFaceUV);
    }

    private int[] makeVertices(BlockFaceUV blockFaceUV, TextureAtlasSprite textureAtlasSprite, Direction direction, float[] fs, BlockModelRotation blockModelRotation, @Nullable BlockElementRotation blockElementRotation, boolean bl) {
        int[] is = new int[28];
        for (int i = 0; i < 4; ++i) {
            this.bakeVertex(is, i, direction, blockFaceUV, fs, textureAtlasSprite, blockModelRotation, blockElementRotation, bl);
        }
        return is;
    }

    private int getShadeValue(Direction direction) {
        float f = this.getShade(direction);
        int i = Mth.clamp((int)(f * 255.0f), 0, 255);
        return 0xFF000000 | i << 16 | i << 8 | i;
    }

    private float getShade(Direction direction) {
        switch (direction) {
            case DOWN: {
                return 0.5f;
            }
            case UP: {
                return 1.0f;
            }
            case NORTH: 
            case SOUTH: {
                return 0.8f;
            }
            case WEST: 
            case EAST: {
                return 0.6f;
            }
        }
        return 1.0f;
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

    private void bakeVertex(int[] is, int i, Direction direction, BlockFaceUV blockFaceUV, float[] fs, TextureAtlasSprite textureAtlasSprite, BlockModelRotation blockModelRotation, @Nullable BlockElementRotation blockElementRotation, boolean bl) {
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
        is[l + 4] = Float.floatToRawIntBits(textureAtlasSprite.getU(blockFaceUV.getU(j)));
        is[l + 4 + 1] = Float.floatToRawIntBits(textureAtlasSprite.getV(blockFaceUV.getV(j)));
    }

    private void applyElementRotation(Vector3f vector3f, @Nullable BlockElementRotation blockElementRotation) {
        Vector3f vector3f3;
        Vector3f vector3f2;
        if (blockElementRotation == null) {
            return;
        }
        switch (blockElementRotation.axis) {
            case X: {
                vector3f2 = new Vector3f(1.0f, 0.0f, 0.0f);
                vector3f3 = new Vector3f(0.0f, 1.0f, 1.0f);
                break;
            }
            case Y: {
                vector3f2 = new Vector3f(0.0f, 1.0f, 0.0f);
                vector3f3 = new Vector3f(1.0f, 0.0f, 1.0f);
                break;
            }
            case Z: {
                vector3f2 = new Vector3f(0.0f, 0.0f, 1.0f);
                vector3f3 = new Vector3f(1.0f, 1.0f, 0.0f);
                break;
            }
            default: {
                throw new IllegalArgumentException("There are only 3 axes");
            }
        }
        Quaternion quaternion = new Quaternion(vector3f2, blockElementRotation.angle, true);
        if (blockElementRotation.rescale) {
            if (Math.abs(blockElementRotation.angle) == 22.5f) {
                vector3f3.mul(RESCALE_22_5);
            } else {
                vector3f3.mul(RESCALE_45);
            }
            vector3f3.add(1.0f, 1.0f, 1.0f);
        } else {
            vector3f3.set(1.0f, 1.0f, 1.0f);
        }
        this.rotateVertexBy(vector3f, new Vector3f(blockElementRotation.origin), quaternion, vector3f3);
    }

    public int applyModelRotation(Vector3f vector3f, Direction direction, int i, BlockModelRotation blockModelRotation) {
        if (blockModelRotation == BlockModelRotation.X0_Y0) {
            return i;
        }
        this.rotateVertexBy(vector3f, new Vector3f(0.5f, 0.5f, 0.5f), blockModelRotation.getRotationQuaternion(), new Vector3f(1.0f, 1.0f, 1.0f));
        return blockModelRotation.rotateVertexIndex(direction, i);
    }

    private void rotateVertexBy(Vector3f vector3f, Vector3f vector3f2, Quaternion quaternion, Vector3f vector3f3) {
        Vector4f vector4f = new Vector4f(vector3f.x() - vector3f2.x(), vector3f.y() - vector3f2.y(), vector3f.z() - vector3f2.z(), 1.0f);
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
            j = 7 * i;
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
            int k = 7 * j;
            FaceInfo.VertexInfo vertexInfo = faceInfo.getVertexInfo(j);
            h = fs[vertexInfo.xFace];
            float l = fs[vertexInfo.yFace];
            float m = fs[vertexInfo.zFace];
            is[k] = Float.floatToRawIntBits(h);
            is[k + 1] = Float.floatToRawIntBits(l);
            is[k + 2] = Float.floatToRawIntBits(m);
            for (int n = 0; n < 4; ++n) {
                int o = 7 * n;
                float p = Float.intBitsToFloat(js[o]);
                float q = Float.intBitsToFloat(js[o + 1]);
                float r = Float.intBitsToFloat(js[o + 2]);
                if (!Mth.equal(h, p) || !Mth.equal(l, q) || !Mth.equal(m, r)) continue;
                is[k + 4] = js[o + 4];
                is[k + 4 + 1] = js[o + 4 + 1];
            }
        }
    }

    private static void register(BlockModelRotation blockModelRotation, Direction direction, Rotation rotation) {
        FaceBakery.BY_INDEX[FaceBakery.getIndex((BlockModelRotation)blockModelRotation, (Direction)direction)] = rotation;
    }

    private static int getIndex(BlockModelRotation blockModelRotation, Direction direction) {
        return BlockModelRotation.values().length * direction.ordinal() + blockModelRotation.ordinal();
    }

    static {
        FaceBakery.register(BlockModelRotation.X0_Y0, Direction.DOWN, ROT_0);
        FaceBakery.register(BlockModelRotation.X0_Y0, Direction.EAST, ROT_0);
        FaceBakery.register(BlockModelRotation.X0_Y0, Direction.NORTH, ROT_0);
        FaceBakery.register(BlockModelRotation.X0_Y0, Direction.SOUTH, ROT_0);
        FaceBakery.register(BlockModelRotation.X0_Y0, Direction.UP, ROT_0);
        FaceBakery.register(BlockModelRotation.X0_Y0, Direction.WEST, ROT_0);
        FaceBakery.register(BlockModelRotation.X0_Y90, Direction.EAST, ROT_0);
        FaceBakery.register(BlockModelRotation.X0_Y90, Direction.NORTH, ROT_0);
        FaceBakery.register(BlockModelRotation.X0_Y90, Direction.SOUTH, ROT_0);
        FaceBakery.register(BlockModelRotation.X0_Y90, Direction.WEST, ROT_0);
        FaceBakery.register(BlockModelRotation.X0_Y180, Direction.EAST, ROT_0);
        FaceBakery.register(BlockModelRotation.X0_Y180, Direction.NORTH, ROT_0);
        FaceBakery.register(BlockModelRotation.X0_Y180, Direction.SOUTH, ROT_0);
        FaceBakery.register(BlockModelRotation.X0_Y180, Direction.WEST, ROT_0);
        FaceBakery.register(BlockModelRotation.X0_Y270, Direction.EAST, ROT_0);
        FaceBakery.register(BlockModelRotation.X0_Y270, Direction.NORTH, ROT_0);
        FaceBakery.register(BlockModelRotation.X0_Y270, Direction.SOUTH, ROT_0);
        FaceBakery.register(BlockModelRotation.X0_Y270, Direction.WEST, ROT_0);
        FaceBakery.register(BlockModelRotation.X90_Y0, Direction.DOWN, ROT_0);
        FaceBakery.register(BlockModelRotation.X90_Y0, Direction.SOUTH, ROT_0);
        FaceBakery.register(BlockModelRotation.X90_Y90, Direction.DOWN, ROT_0);
        FaceBakery.register(BlockModelRotation.X90_Y180, Direction.DOWN, ROT_0);
        FaceBakery.register(BlockModelRotation.X90_Y180, Direction.NORTH, ROT_0);
        FaceBakery.register(BlockModelRotation.X90_Y270, Direction.DOWN, ROT_0);
        FaceBakery.register(BlockModelRotation.X180_Y0, Direction.DOWN, ROT_0);
        FaceBakery.register(BlockModelRotation.X180_Y0, Direction.UP, ROT_0);
        FaceBakery.register(BlockModelRotation.X270_Y0, Direction.SOUTH, ROT_0);
        FaceBakery.register(BlockModelRotation.X270_Y0, Direction.UP, ROT_0);
        FaceBakery.register(BlockModelRotation.X270_Y90, Direction.UP, ROT_0);
        FaceBakery.register(BlockModelRotation.X270_Y180, Direction.NORTH, ROT_0);
        FaceBakery.register(BlockModelRotation.X270_Y180, Direction.UP, ROT_0);
        FaceBakery.register(BlockModelRotation.X270_Y270, Direction.UP, ROT_0);
        FaceBakery.register(BlockModelRotation.X0_Y270, Direction.UP, ROT_90);
        FaceBakery.register(BlockModelRotation.X0_Y90, Direction.DOWN, ROT_90);
        FaceBakery.register(BlockModelRotation.X90_Y0, Direction.WEST, ROT_90);
        FaceBakery.register(BlockModelRotation.X90_Y90, Direction.WEST, ROT_90);
        FaceBakery.register(BlockModelRotation.X90_Y180, Direction.WEST, ROT_90);
        FaceBakery.register(BlockModelRotation.X90_Y270, Direction.NORTH, ROT_90);
        FaceBakery.register(BlockModelRotation.X90_Y270, Direction.SOUTH, ROT_90);
        FaceBakery.register(BlockModelRotation.X90_Y270, Direction.WEST, ROT_90);
        FaceBakery.register(BlockModelRotation.X180_Y90, Direction.UP, ROT_90);
        FaceBakery.register(BlockModelRotation.X180_Y270, Direction.DOWN, ROT_90);
        FaceBakery.register(BlockModelRotation.X270_Y0, Direction.EAST, ROT_90);
        FaceBakery.register(BlockModelRotation.X270_Y90, Direction.EAST, ROT_90);
        FaceBakery.register(BlockModelRotation.X270_Y90, Direction.NORTH, ROT_90);
        FaceBakery.register(BlockModelRotation.X270_Y90, Direction.SOUTH, ROT_90);
        FaceBakery.register(BlockModelRotation.X270_Y180, Direction.EAST, ROT_90);
        FaceBakery.register(BlockModelRotation.X270_Y270, Direction.EAST, ROT_90);
        FaceBakery.register(BlockModelRotation.X0_Y180, Direction.DOWN, ROT_180);
        FaceBakery.register(BlockModelRotation.X0_Y180, Direction.UP, ROT_180);
        FaceBakery.register(BlockModelRotation.X90_Y0, Direction.NORTH, ROT_180);
        FaceBakery.register(BlockModelRotation.X90_Y0, Direction.UP, ROT_180);
        FaceBakery.register(BlockModelRotation.X90_Y90, Direction.UP, ROT_180);
        FaceBakery.register(BlockModelRotation.X90_Y180, Direction.SOUTH, ROT_180);
        FaceBakery.register(BlockModelRotation.X90_Y180, Direction.UP, ROT_180);
        FaceBakery.register(BlockModelRotation.X90_Y270, Direction.UP, ROT_180);
        FaceBakery.register(BlockModelRotation.X180_Y0, Direction.EAST, ROT_180);
        FaceBakery.register(BlockModelRotation.X180_Y0, Direction.NORTH, ROT_180);
        FaceBakery.register(BlockModelRotation.X180_Y0, Direction.SOUTH, ROT_180);
        FaceBakery.register(BlockModelRotation.X180_Y0, Direction.WEST, ROT_180);
        FaceBakery.register(BlockModelRotation.X180_Y90, Direction.EAST, ROT_180);
        FaceBakery.register(BlockModelRotation.X180_Y90, Direction.NORTH, ROT_180);
        FaceBakery.register(BlockModelRotation.X180_Y90, Direction.SOUTH, ROT_180);
        FaceBakery.register(BlockModelRotation.X180_Y90, Direction.WEST, ROT_180);
        FaceBakery.register(BlockModelRotation.X180_Y180, Direction.DOWN, ROT_180);
        FaceBakery.register(BlockModelRotation.X180_Y180, Direction.EAST, ROT_180);
        FaceBakery.register(BlockModelRotation.X180_Y180, Direction.NORTH, ROT_180);
        FaceBakery.register(BlockModelRotation.X180_Y180, Direction.SOUTH, ROT_180);
        FaceBakery.register(BlockModelRotation.X180_Y180, Direction.UP, ROT_180);
        FaceBakery.register(BlockModelRotation.X180_Y180, Direction.WEST, ROT_180);
        FaceBakery.register(BlockModelRotation.X180_Y270, Direction.EAST, ROT_180);
        FaceBakery.register(BlockModelRotation.X180_Y270, Direction.NORTH, ROT_180);
        FaceBakery.register(BlockModelRotation.X180_Y270, Direction.SOUTH, ROT_180);
        FaceBakery.register(BlockModelRotation.X180_Y270, Direction.WEST, ROT_180);
        FaceBakery.register(BlockModelRotation.X270_Y0, Direction.DOWN, ROT_180);
        FaceBakery.register(BlockModelRotation.X270_Y0, Direction.NORTH, ROT_180);
        FaceBakery.register(BlockModelRotation.X270_Y90, Direction.DOWN, ROT_180);
        FaceBakery.register(BlockModelRotation.X270_Y180, Direction.DOWN, ROT_180);
        FaceBakery.register(BlockModelRotation.X270_Y180, Direction.SOUTH, ROT_180);
        FaceBakery.register(BlockModelRotation.X270_Y270, Direction.DOWN, ROT_180);
        FaceBakery.register(BlockModelRotation.X0_Y90, Direction.UP, ROT_270);
        FaceBakery.register(BlockModelRotation.X0_Y270, Direction.DOWN, ROT_270);
        FaceBakery.register(BlockModelRotation.X90_Y0, Direction.EAST, ROT_270);
        FaceBakery.register(BlockModelRotation.X90_Y90, Direction.EAST, ROT_270);
        FaceBakery.register(BlockModelRotation.X90_Y90, Direction.NORTH, ROT_270);
        FaceBakery.register(BlockModelRotation.X90_Y90, Direction.SOUTH, ROT_270);
        FaceBakery.register(BlockModelRotation.X90_Y180, Direction.EAST, ROT_270);
        FaceBakery.register(BlockModelRotation.X90_Y270, Direction.EAST, ROT_270);
        FaceBakery.register(BlockModelRotation.X270_Y0, Direction.WEST, ROT_270);
        FaceBakery.register(BlockModelRotation.X180_Y90, Direction.DOWN, ROT_270);
        FaceBakery.register(BlockModelRotation.X180_Y270, Direction.UP, ROT_270);
        FaceBakery.register(BlockModelRotation.X270_Y90, Direction.WEST, ROT_270);
        FaceBakery.register(BlockModelRotation.X270_Y180, Direction.WEST, ROT_270);
        FaceBakery.register(BlockModelRotation.X270_Y270, Direction.NORTH, ROT_270);
        FaceBakery.register(BlockModelRotation.X270_Y270, Direction.SOUTH, ROT_270);
        FaceBakery.register(BlockModelRotation.X270_Y270, Direction.WEST, ROT_270);
    }

    @Environment(value=EnvType.CLIENT)
    static abstract class Rotation {
        private Rotation() {
        }

        public BlockFaceUV recompute(BlockFaceUV blockFaceUV) {
            float f = blockFaceUV.getU(blockFaceUV.getReverseIndex(0));
            float g = blockFaceUV.getV(blockFaceUV.getReverseIndex(0));
            float h = blockFaceUV.getU(blockFaceUV.getReverseIndex(2));
            float i = blockFaceUV.getV(blockFaceUV.getReverseIndex(2));
            return this.apply(f, g, h, i);
        }

        abstract BlockFaceUV apply(float var1, float var2, float var3, float var4);
    }
}

