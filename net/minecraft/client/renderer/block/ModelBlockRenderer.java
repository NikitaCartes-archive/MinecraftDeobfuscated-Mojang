/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ModelBlockRenderer {
    private static final int FACE_CUBIC = 0;
    private static final int FACE_PARTIAL = 1;
    static final Direction[] DIRECTIONS = Direction.values();
    private final BlockColors blockColors;
    private static final int CACHE_SIZE = 100;
    static final ThreadLocal<Cache> CACHE = ThreadLocal.withInitial(Cache::new);

    public ModelBlockRenderer(BlockColors blockColors) {
        this.blockColors = blockColors;
    }

    public boolean tesselateBlock(BlockAndTintGetter blockAndTintGetter, BakedModel bakedModel, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, Random random, long l, int i) {
        boolean bl2 = Minecraft.useAmbientOcclusion() && blockState.getLightEmission() == 0 && bakedModel.useAmbientOcclusion();
        Vec3 vec3 = blockState.getOffset(blockAndTintGetter, blockPos);
        poseStack.translate(vec3.x, vec3.y, vec3.z);
        try {
            if (bl2) {
                return this.tesselateWithAO(blockAndTintGetter, bakedModel, blockState, blockPos, poseStack, vertexConsumer, bl, random, l, i);
            }
            return this.tesselateWithoutAO(blockAndTintGetter, bakedModel, blockState, blockPos, poseStack, vertexConsumer, bl, random, l, i);
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Tesselating block model");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Block model being tesselated");
            CrashReportCategory.populateBlockDetails(crashReportCategory, blockAndTintGetter, blockPos, blockState);
            crashReportCategory.setDetail("Using AO", bl2);
            throw new ReportedException(crashReport);
        }
    }

    public boolean tesselateWithAO(BlockAndTintGetter blockAndTintGetter, BakedModel bakedModel, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, Random random, long l, int i) {
        boolean bl2 = false;
        float[] fs = new float[DIRECTIONS.length * 2];
        BitSet bitSet = new BitSet(3);
        AmbientOcclusionFace ambientOcclusionFace = new AmbientOcclusionFace();
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (Direction direction : DIRECTIONS) {
            random.setSeed(l);
            List<BakedQuad> list = bakedModel.getQuads(blockState, direction, random);
            if (list.isEmpty()) continue;
            mutableBlockPos.setWithOffset((Vec3i)blockPos, direction);
            if (bl && !Block.shouldRenderFace(blockState, blockAndTintGetter, blockPos, direction, mutableBlockPos)) continue;
            this.renderModelFaceAO(blockAndTintGetter, blockState, blockPos, poseStack, vertexConsumer, list, fs, bitSet, ambientOcclusionFace, i);
            bl2 = true;
        }
        random.setSeed(l);
        List<BakedQuad> list2 = bakedModel.getQuads(blockState, null, random);
        if (!list2.isEmpty()) {
            this.renderModelFaceAO(blockAndTintGetter, blockState, blockPos, poseStack, vertexConsumer, list2, fs, bitSet, ambientOcclusionFace, i);
            bl2 = true;
        }
        return bl2;
    }

    public boolean tesselateWithoutAO(BlockAndTintGetter blockAndTintGetter, BakedModel bakedModel, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, Random random, long l, int i) {
        boolean bl2 = false;
        BitSet bitSet = new BitSet(3);
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        for (Direction direction : DIRECTIONS) {
            random.setSeed(l);
            List<BakedQuad> list = bakedModel.getQuads(blockState, direction, random);
            if (list.isEmpty()) continue;
            mutableBlockPos.setWithOffset((Vec3i)blockPos, direction);
            if (bl && !Block.shouldRenderFace(blockState, blockAndTintGetter, blockPos, direction, mutableBlockPos)) continue;
            int j = LevelRenderer.getLightColor(blockAndTintGetter, blockState, mutableBlockPos);
            this.renderModelFaceFlat(blockAndTintGetter, blockState, blockPos, j, i, false, poseStack, vertexConsumer, list, bitSet);
            bl2 = true;
        }
        random.setSeed(l);
        List<BakedQuad> list2 = bakedModel.getQuads(blockState, null, random);
        if (!list2.isEmpty()) {
            this.renderModelFaceFlat(blockAndTintGetter, blockState, blockPos, -1, i, true, poseStack, vertexConsumer, list2, bitSet);
            bl2 = true;
        }
        return bl2;
    }

    private void renderModelFaceAO(BlockAndTintGetter blockAndTintGetter, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, List<BakedQuad> list, float[] fs, BitSet bitSet, AmbientOcclusionFace ambientOcclusionFace, int i) {
        for (BakedQuad bakedQuad : list) {
            this.calculateShape(blockAndTintGetter, blockState, blockPos, bakedQuad.getVertices(), bakedQuad.getDirection(), fs, bitSet);
            ambientOcclusionFace.calculate(blockAndTintGetter, blockState, blockPos, bakedQuad.getDirection(), fs, bitSet, bakedQuad.isShade());
            this.putQuadData(blockAndTintGetter, blockState, blockPos, vertexConsumer, poseStack.last(), bakedQuad, ambientOcclusionFace.brightness[0], ambientOcclusionFace.brightness[1], ambientOcclusionFace.brightness[2], ambientOcclusionFace.brightness[3], ambientOcclusionFace.lightmap[0], ambientOcclusionFace.lightmap[1], ambientOcclusionFace.lightmap[2], ambientOcclusionFace.lightmap[3], i);
        }
    }

    private void putQuadData(BlockAndTintGetter blockAndTintGetter, BlockState blockState, BlockPos blockPos, VertexConsumer vertexConsumer, PoseStack.Pose pose, BakedQuad bakedQuad, float f, float g, float h, float i, int j, int k, int l, int m, int n) {
        float r;
        float q;
        float p;
        if (bakedQuad.isTinted()) {
            int o = this.blockColors.getColor(blockState, blockAndTintGetter, blockPos, bakedQuad.getTintIndex());
            p = (float)(o >> 16 & 0xFF) / 255.0f;
            q = (float)(o >> 8 & 0xFF) / 255.0f;
            r = (float)(o & 0xFF) / 255.0f;
        } else {
            p = 1.0f;
            q = 1.0f;
            r = 1.0f;
        }
        vertexConsumer.putBulkData(pose, bakedQuad, new float[]{f, g, h, i}, p, q, r, new int[]{j, k, l, m}, n, true);
    }

    private void calculateShape(BlockAndTintGetter blockAndTintGetter, BlockState blockState, BlockPos blockPos, int[] is, Direction direction, @Nullable float[] fs, BitSet bitSet) {
        float m;
        int l;
        float f = 32.0f;
        float g = 32.0f;
        float h = 32.0f;
        float i = -32.0f;
        float j = -32.0f;
        float k = -32.0f;
        for (l = 0; l < 4; ++l) {
            m = Float.intBitsToFloat(is[l * 8]);
            float n = Float.intBitsToFloat(is[l * 8 + 1]);
            float o = Float.intBitsToFloat(is[l * 8 + 2]);
            f = Math.min(f, m);
            g = Math.min(g, n);
            h = Math.min(h, o);
            i = Math.max(i, m);
            j = Math.max(j, n);
            k = Math.max(k, o);
        }
        if (fs != null) {
            fs[Direction.WEST.get3DDataValue()] = f;
            fs[Direction.EAST.get3DDataValue()] = i;
            fs[Direction.DOWN.get3DDataValue()] = g;
            fs[Direction.UP.get3DDataValue()] = j;
            fs[Direction.NORTH.get3DDataValue()] = h;
            fs[Direction.SOUTH.get3DDataValue()] = k;
            l = DIRECTIONS.length;
            fs[Direction.WEST.get3DDataValue() + l] = 1.0f - f;
            fs[Direction.EAST.get3DDataValue() + l] = 1.0f - i;
            fs[Direction.DOWN.get3DDataValue() + l] = 1.0f - g;
            fs[Direction.UP.get3DDataValue() + l] = 1.0f - j;
            fs[Direction.NORTH.get3DDataValue() + l] = 1.0f - h;
            fs[Direction.SOUTH.get3DDataValue() + l] = 1.0f - k;
        }
        float p = 1.0E-4f;
        m = 0.9999f;
        switch (direction) {
            case DOWN: {
                bitSet.set(1, f >= 1.0E-4f || h >= 1.0E-4f || i <= 0.9999f || k <= 0.9999f);
                bitSet.set(0, g == j && (g < 1.0E-4f || blockState.isCollisionShapeFullBlock(blockAndTintGetter, blockPos)));
                break;
            }
            case UP: {
                bitSet.set(1, f >= 1.0E-4f || h >= 1.0E-4f || i <= 0.9999f || k <= 0.9999f);
                bitSet.set(0, g == j && (j > 0.9999f || blockState.isCollisionShapeFullBlock(blockAndTintGetter, blockPos)));
                break;
            }
            case NORTH: {
                bitSet.set(1, f >= 1.0E-4f || g >= 1.0E-4f || i <= 0.9999f || j <= 0.9999f);
                bitSet.set(0, h == k && (h < 1.0E-4f || blockState.isCollisionShapeFullBlock(blockAndTintGetter, blockPos)));
                break;
            }
            case SOUTH: {
                bitSet.set(1, f >= 1.0E-4f || g >= 1.0E-4f || i <= 0.9999f || j <= 0.9999f);
                bitSet.set(0, h == k && (k > 0.9999f || blockState.isCollisionShapeFullBlock(blockAndTintGetter, blockPos)));
                break;
            }
            case WEST: {
                bitSet.set(1, g >= 1.0E-4f || h >= 1.0E-4f || j <= 0.9999f || k <= 0.9999f);
                bitSet.set(0, f == i && (f < 1.0E-4f || blockState.isCollisionShapeFullBlock(blockAndTintGetter, blockPos)));
                break;
            }
            case EAST: {
                bitSet.set(1, g >= 1.0E-4f || h >= 1.0E-4f || j <= 0.9999f || k <= 0.9999f);
                bitSet.set(0, f == i && (i > 0.9999f || blockState.isCollisionShapeFullBlock(blockAndTintGetter, blockPos)));
            }
        }
    }

    private void renderModelFaceFlat(BlockAndTintGetter blockAndTintGetter, BlockState blockState, BlockPos blockPos, int i, int j, boolean bl, PoseStack poseStack, VertexConsumer vertexConsumer, List<BakedQuad> list, BitSet bitSet) {
        for (BakedQuad bakedQuad : list) {
            if (bl) {
                this.calculateShape(blockAndTintGetter, blockState, blockPos, bakedQuad.getVertices(), bakedQuad.getDirection(), null, bitSet);
                BlockPos blockPos2 = bitSet.get(0) ? blockPos.relative(bakedQuad.getDirection()) : blockPos;
                i = LevelRenderer.getLightColor(blockAndTintGetter, blockState, blockPos2);
            }
            float f = blockAndTintGetter.getShade(bakedQuad.getDirection(), bakedQuad.isShade());
            this.putQuadData(blockAndTintGetter, blockState, blockPos, vertexConsumer, poseStack.last(), bakedQuad, f, f, f, f, i, i, i, i, j);
        }
    }

    public void renderModel(PoseStack.Pose pose, VertexConsumer vertexConsumer, @Nullable BlockState blockState, BakedModel bakedModel, float f, float g, float h, int i, int j) {
        Random random = new Random();
        long l = 42L;
        for (Direction direction : DIRECTIONS) {
            random.setSeed(42L);
            ModelBlockRenderer.renderQuadList(pose, vertexConsumer, f, g, h, bakedModel.getQuads(blockState, direction, random), i, j);
        }
        random.setSeed(42L);
        ModelBlockRenderer.renderQuadList(pose, vertexConsumer, f, g, h, bakedModel.getQuads(blockState, null, random), i, j);
    }

    private static void renderQuadList(PoseStack.Pose pose, VertexConsumer vertexConsumer, float f, float g, float h, List<BakedQuad> list, int i, int j) {
        for (BakedQuad bakedQuad : list) {
            float m;
            float l;
            float k;
            if (bakedQuad.isTinted()) {
                k = Mth.clamp(f, 0.0f, 1.0f);
                l = Mth.clamp(g, 0.0f, 1.0f);
                m = Mth.clamp(h, 0.0f, 1.0f);
            } else {
                k = 1.0f;
                l = 1.0f;
                m = 1.0f;
            }
            vertexConsumer.putBulkData(pose, bakedQuad, k, l, m, i, j);
        }
    }

    public static void enableCaching() {
        CACHE.get().enable();
    }

    public static void clearCache() {
        CACHE.get().disable();
    }

    @Environment(value=EnvType.CLIENT)
    class AmbientOcclusionFace {
        final float[] brightness = new float[4];
        final int[] lightmap = new int[4];

        public void calculate(BlockAndTintGetter blockAndTintGetter, BlockState blockState, BlockPos blockPos, Direction direction, float[] fs, BitSet bitSet, boolean bl) {
            float x;
            int u;
            float t;
            int s;
            float r;
            int q;
            float p;
            int o;
            float n;
            BlockState blockState6;
            boolean bl5;
            BlockPos blockPos2 = bitSet.get(0) ? blockPos.relative(direction) : blockPos;
            AdjacencyInfo adjacencyInfo = AdjacencyInfo.fromFacing(direction);
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            Cache cache = CACHE.get();
            mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[0]);
            BlockState blockState2 = blockAndTintGetter.getBlockState(mutableBlockPos);
            int i = cache.getLightColor(blockState2, blockAndTintGetter, mutableBlockPos);
            float f = cache.getShadeBrightness(blockState2, blockAndTintGetter, mutableBlockPos);
            mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[1]);
            BlockState blockState3 = blockAndTintGetter.getBlockState(mutableBlockPos);
            int j = cache.getLightColor(blockState3, blockAndTintGetter, mutableBlockPos);
            float g = cache.getShadeBrightness(blockState3, blockAndTintGetter, mutableBlockPos);
            mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[2]);
            BlockState blockState4 = blockAndTintGetter.getBlockState(mutableBlockPos);
            int k = cache.getLightColor(blockState4, blockAndTintGetter, mutableBlockPos);
            float h = cache.getShadeBrightness(blockState4, blockAndTintGetter, mutableBlockPos);
            mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[3]);
            BlockState blockState5 = blockAndTintGetter.getBlockState(mutableBlockPos);
            int l = cache.getLightColor(blockState5, blockAndTintGetter, mutableBlockPos);
            float m = cache.getShadeBrightness(blockState5, blockAndTintGetter, mutableBlockPos);
            mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[0]).move(direction);
            boolean bl2 = blockAndTintGetter.getBlockState(mutableBlockPos).getLightBlock(blockAndTintGetter, mutableBlockPos) == 0;
            mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[1]).move(direction);
            boolean bl3 = blockAndTintGetter.getBlockState(mutableBlockPos).getLightBlock(blockAndTintGetter, mutableBlockPos) == 0;
            mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[2]).move(direction);
            boolean bl4 = blockAndTintGetter.getBlockState(mutableBlockPos).getLightBlock(blockAndTintGetter, mutableBlockPos) == 0;
            mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[3]).move(direction);
            boolean bl6 = bl5 = blockAndTintGetter.getBlockState(mutableBlockPos).getLightBlock(blockAndTintGetter, mutableBlockPos) == 0;
            if (bl4 || bl2) {
                mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[0]).move(adjacencyInfo.corners[2]);
                blockState6 = blockAndTintGetter.getBlockState(mutableBlockPos);
                n = cache.getShadeBrightness(blockState6, blockAndTintGetter, mutableBlockPos);
                o = cache.getLightColor(blockState6, blockAndTintGetter, mutableBlockPos);
            } else {
                n = f;
                o = i;
            }
            if (bl5 || bl2) {
                mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[0]).move(adjacencyInfo.corners[3]);
                blockState6 = blockAndTintGetter.getBlockState(mutableBlockPos);
                p = cache.getShadeBrightness(blockState6, blockAndTintGetter, mutableBlockPos);
                q = cache.getLightColor(blockState6, blockAndTintGetter, mutableBlockPos);
            } else {
                p = f;
                q = i;
            }
            if (bl4 || bl3) {
                mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[1]).move(adjacencyInfo.corners[2]);
                blockState6 = blockAndTintGetter.getBlockState(mutableBlockPos);
                r = cache.getShadeBrightness(blockState6, blockAndTintGetter, mutableBlockPos);
                s = cache.getLightColor(blockState6, blockAndTintGetter, mutableBlockPos);
            } else {
                r = f;
                s = i;
            }
            if (bl5 || bl3) {
                mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[1]).move(adjacencyInfo.corners[3]);
                blockState6 = blockAndTintGetter.getBlockState(mutableBlockPos);
                t = cache.getShadeBrightness(blockState6, blockAndTintGetter, mutableBlockPos);
                u = cache.getLightColor(blockState6, blockAndTintGetter, mutableBlockPos);
            } else {
                t = f;
                u = i;
            }
            int v = cache.getLightColor(blockState, blockAndTintGetter, blockPos);
            mutableBlockPos.setWithOffset((Vec3i)blockPos, direction);
            BlockState blockState7 = blockAndTintGetter.getBlockState(mutableBlockPos);
            if (bitSet.get(0) || !blockState7.isSolidRender(blockAndTintGetter, mutableBlockPos)) {
                v = cache.getLightColor(blockState7, blockAndTintGetter, mutableBlockPos);
            }
            float w = bitSet.get(0) ? cache.getShadeBrightness(blockAndTintGetter.getBlockState(blockPos2), blockAndTintGetter, blockPos2) : cache.getShadeBrightness(blockAndTintGetter.getBlockState(blockPos), blockAndTintGetter, blockPos);
            AmbientVertexRemap ambientVertexRemap = AmbientVertexRemap.fromFacing(direction);
            if (!bitSet.get(1) || !adjacencyInfo.doNonCubicWeight) {
                x = (m + f + p + w) * 0.25f;
                y = (h + f + n + w) * 0.25f;
                z = (h + g + r + w) * 0.25f;
                aa = (m + g + t + w) * 0.25f;
                this.lightmap[ambientVertexRemap.vert0] = this.blend(l, i, q, v);
                this.lightmap[ambientVertexRemap.vert1] = this.blend(k, i, o, v);
                this.lightmap[ambientVertexRemap.vert2] = this.blend(k, j, s, v);
                this.lightmap[ambientVertexRemap.vert3] = this.blend(l, j, u, v);
                this.brightness[ambientVertexRemap.vert0] = x;
                this.brightness[ambientVertexRemap.vert1] = y;
                this.brightness[ambientVertexRemap.vert2] = z;
                this.brightness[ambientVertexRemap.vert3] = aa;
            } else {
                x = (m + f + p + w) * 0.25f;
                y = (h + f + n + w) * 0.25f;
                z = (h + g + r + w) * 0.25f;
                aa = (m + g + t + w) * 0.25f;
                float ab = fs[adjacencyInfo.vert0Weights[0].shape] * fs[adjacencyInfo.vert0Weights[1].shape];
                float ac = fs[adjacencyInfo.vert0Weights[2].shape] * fs[adjacencyInfo.vert0Weights[3].shape];
                float ad = fs[adjacencyInfo.vert0Weights[4].shape] * fs[adjacencyInfo.vert0Weights[5].shape];
                float ae = fs[adjacencyInfo.vert0Weights[6].shape] * fs[adjacencyInfo.vert0Weights[7].shape];
                float af = fs[adjacencyInfo.vert1Weights[0].shape] * fs[adjacencyInfo.vert1Weights[1].shape];
                float ag = fs[adjacencyInfo.vert1Weights[2].shape] * fs[adjacencyInfo.vert1Weights[3].shape];
                float ah = fs[adjacencyInfo.vert1Weights[4].shape] * fs[adjacencyInfo.vert1Weights[5].shape];
                float ai = fs[adjacencyInfo.vert1Weights[6].shape] * fs[adjacencyInfo.vert1Weights[7].shape];
                float aj = fs[adjacencyInfo.vert2Weights[0].shape] * fs[adjacencyInfo.vert2Weights[1].shape];
                float ak = fs[adjacencyInfo.vert2Weights[2].shape] * fs[adjacencyInfo.vert2Weights[3].shape];
                float al = fs[adjacencyInfo.vert2Weights[4].shape] * fs[adjacencyInfo.vert2Weights[5].shape];
                float am = fs[adjacencyInfo.vert2Weights[6].shape] * fs[adjacencyInfo.vert2Weights[7].shape];
                float an = fs[adjacencyInfo.vert3Weights[0].shape] * fs[adjacencyInfo.vert3Weights[1].shape];
                float ao = fs[adjacencyInfo.vert3Weights[2].shape] * fs[adjacencyInfo.vert3Weights[3].shape];
                float ap = fs[adjacencyInfo.vert3Weights[4].shape] * fs[adjacencyInfo.vert3Weights[5].shape];
                float aq = fs[adjacencyInfo.vert3Weights[6].shape] * fs[adjacencyInfo.vert3Weights[7].shape];
                this.brightness[ambientVertexRemap.vert0] = x * ab + y * ac + z * ad + aa * ae;
                this.brightness[ambientVertexRemap.vert1] = x * af + y * ag + z * ah + aa * ai;
                this.brightness[ambientVertexRemap.vert2] = x * aj + y * ak + z * al + aa * am;
                this.brightness[ambientVertexRemap.vert3] = x * an + y * ao + z * ap + aa * aq;
                int ar = this.blend(l, i, q, v);
                int as = this.blend(k, i, o, v);
                int at = this.blend(k, j, s, v);
                int au = this.blend(l, j, u, v);
                this.lightmap[ambientVertexRemap.vert0] = this.blend(ar, as, at, au, ab, ac, ad, ae);
                this.lightmap[ambientVertexRemap.vert1] = this.blend(ar, as, at, au, af, ag, ah, ai);
                this.lightmap[ambientVertexRemap.vert2] = this.blend(ar, as, at, au, aj, ak, al, am);
                this.lightmap[ambientVertexRemap.vert3] = this.blend(ar, as, at, au, an, ao, ap, aq);
            }
            x = blockAndTintGetter.getShade(direction, bl);
            int av = 0;
            while (av < this.brightness.length) {
                int n2 = av++;
                this.brightness[n2] = this.brightness[n2] * x;
            }
        }

        private int blend(int i, int j, int k, int l) {
            if (i == 0) {
                i = l;
            }
            if (j == 0) {
                j = l;
            }
            if (k == 0) {
                k = l;
            }
            return i + j + k + l >> 2 & 0xFF00FF;
        }

        private int blend(int i, int j, int k, int l, float f, float g, float h, float m) {
            int n = (int)((float)(i >> 16 & 0xFF) * f + (float)(j >> 16 & 0xFF) * g + (float)(k >> 16 & 0xFF) * h + (float)(l >> 16 & 0xFF) * m) & 0xFF;
            int o = (int)((float)(i & 0xFF) * f + (float)(j & 0xFF) * g + (float)(k & 0xFF) * h + (float)(l & 0xFF) * m) & 0xFF;
            return n << 16 | o;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Cache {
        private boolean enabled;
        private final Long2IntLinkedOpenHashMap colorCache = Util.make(() -> {
            Long2IntLinkedOpenHashMap long2IntLinkedOpenHashMap = new Long2IntLinkedOpenHashMap(100, 0.25f){

                @Override
                protected void rehash(int i) {
                }
            };
            long2IntLinkedOpenHashMap.defaultReturnValue(Integer.MAX_VALUE);
            return long2IntLinkedOpenHashMap;
        });
        private final Long2FloatLinkedOpenHashMap brightnessCache = Util.make(() -> {
            Long2FloatLinkedOpenHashMap long2FloatLinkedOpenHashMap = new Long2FloatLinkedOpenHashMap(100, 0.25f){

                @Override
                protected void rehash(int i) {
                }
            };
            long2FloatLinkedOpenHashMap.defaultReturnValue(Float.NaN);
            return long2FloatLinkedOpenHashMap;
        });

        private Cache() {
        }

        public void enable() {
            this.enabled = true;
        }

        public void disable() {
            this.enabled = false;
            this.colorCache.clear();
            this.brightnessCache.clear();
        }

        public int getLightColor(BlockState blockState, BlockAndTintGetter blockAndTintGetter, BlockPos blockPos) {
            int i;
            long l = blockPos.asLong();
            if (this.enabled && (i = this.colorCache.get(l)) != Integer.MAX_VALUE) {
                return i;
            }
            i = LevelRenderer.getLightColor(blockAndTintGetter, blockState, blockPos);
            if (this.enabled) {
                if (this.colorCache.size() == 100) {
                    this.colorCache.removeFirstInt();
                }
                this.colorCache.put(l, i);
            }
            return i;
        }

        public float getShadeBrightness(BlockState blockState, BlockAndTintGetter blockAndTintGetter, BlockPos blockPos) {
            float f;
            long l = blockPos.asLong();
            if (this.enabled && !Float.isNaN(f = this.brightnessCache.get(l))) {
                return f;
            }
            f = blockState.getShadeBrightness(blockAndTintGetter, blockPos);
            if (this.enabled) {
                if (this.brightnessCache.size() == 100) {
                    this.brightnessCache.removeFirstFloat();
                }
                this.brightnessCache.put(l, f);
            }
            return f;
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static enum AdjacencyInfo {
        DOWN(new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH}, 0.5f, true, new SizeInfo[]{SizeInfo.FLIP_WEST, SizeInfo.SOUTH, SizeInfo.FLIP_WEST, SizeInfo.FLIP_SOUTH, SizeInfo.WEST, SizeInfo.FLIP_SOUTH, SizeInfo.WEST, SizeInfo.SOUTH}, new SizeInfo[]{SizeInfo.FLIP_WEST, SizeInfo.NORTH, SizeInfo.FLIP_WEST, SizeInfo.FLIP_NORTH, SizeInfo.WEST, SizeInfo.FLIP_NORTH, SizeInfo.WEST, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.FLIP_EAST, SizeInfo.NORTH, SizeInfo.FLIP_EAST, SizeInfo.FLIP_NORTH, SizeInfo.EAST, SizeInfo.FLIP_NORTH, SizeInfo.EAST, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.FLIP_EAST, SizeInfo.SOUTH, SizeInfo.FLIP_EAST, SizeInfo.FLIP_SOUTH, SizeInfo.EAST, SizeInfo.FLIP_SOUTH, SizeInfo.EAST, SizeInfo.SOUTH}),
        UP(new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH}, 1.0f, true, new SizeInfo[]{SizeInfo.EAST, SizeInfo.SOUTH, SizeInfo.EAST, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_EAST, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_EAST, SizeInfo.SOUTH}, new SizeInfo[]{SizeInfo.EAST, SizeInfo.NORTH, SizeInfo.EAST, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_EAST, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_EAST, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.WEST, SizeInfo.NORTH, SizeInfo.WEST, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_WEST, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_WEST, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.WEST, SizeInfo.SOUTH, SizeInfo.WEST, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_WEST, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_WEST, SizeInfo.SOUTH}),
        NORTH(new Direction[]{Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST}, 0.8f, true, new SizeInfo[]{SizeInfo.UP, SizeInfo.FLIP_WEST, SizeInfo.UP, SizeInfo.WEST, SizeInfo.FLIP_UP, SizeInfo.WEST, SizeInfo.FLIP_UP, SizeInfo.FLIP_WEST}, new SizeInfo[]{SizeInfo.UP, SizeInfo.FLIP_EAST, SizeInfo.UP, SizeInfo.EAST, SizeInfo.FLIP_UP, SizeInfo.EAST, SizeInfo.FLIP_UP, SizeInfo.FLIP_EAST}, new SizeInfo[]{SizeInfo.DOWN, SizeInfo.FLIP_EAST, SizeInfo.DOWN, SizeInfo.EAST, SizeInfo.FLIP_DOWN, SizeInfo.EAST, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_EAST}, new SizeInfo[]{SizeInfo.DOWN, SizeInfo.FLIP_WEST, SizeInfo.DOWN, SizeInfo.WEST, SizeInfo.FLIP_DOWN, SizeInfo.WEST, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_WEST}),
        SOUTH(new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP}, 0.8f, true, new SizeInfo[]{SizeInfo.UP, SizeInfo.FLIP_WEST, SizeInfo.FLIP_UP, SizeInfo.FLIP_WEST, SizeInfo.FLIP_UP, SizeInfo.WEST, SizeInfo.UP, SizeInfo.WEST}, new SizeInfo[]{SizeInfo.DOWN, SizeInfo.FLIP_WEST, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_WEST, SizeInfo.FLIP_DOWN, SizeInfo.WEST, SizeInfo.DOWN, SizeInfo.WEST}, new SizeInfo[]{SizeInfo.DOWN, SizeInfo.FLIP_EAST, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_EAST, SizeInfo.FLIP_DOWN, SizeInfo.EAST, SizeInfo.DOWN, SizeInfo.EAST}, new SizeInfo[]{SizeInfo.UP, SizeInfo.FLIP_EAST, SizeInfo.FLIP_UP, SizeInfo.FLIP_EAST, SizeInfo.FLIP_UP, SizeInfo.EAST, SizeInfo.UP, SizeInfo.EAST}),
        WEST(new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH}, 0.6f, true, new SizeInfo[]{SizeInfo.UP, SizeInfo.SOUTH, SizeInfo.UP, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_UP, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_UP, SizeInfo.SOUTH}, new SizeInfo[]{SizeInfo.UP, SizeInfo.NORTH, SizeInfo.UP, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_UP, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_UP, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.DOWN, SizeInfo.NORTH, SizeInfo.DOWN, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_DOWN, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.DOWN, SizeInfo.SOUTH, SizeInfo.DOWN, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_DOWN, SizeInfo.SOUTH}),
        EAST(new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH}, 0.6f, true, new SizeInfo[]{SizeInfo.FLIP_DOWN, SizeInfo.SOUTH, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_SOUTH, SizeInfo.DOWN, SizeInfo.FLIP_SOUTH, SizeInfo.DOWN, SizeInfo.SOUTH}, new SizeInfo[]{SizeInfo.FLIP_DOWN, SizeInfo.NORTH, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_NORTH, SizeInfo.DOWN, SizeInfo.FLIP_NORTH, SizeInfo.DOWN, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.FLIP_UP, SizeInfo.NORTH, SizeInfo.FLIP_UP, SizeInfo.FLIP_NORTH, SizeInfo.UP, SizeInfo.FLIP_NORTH, SizeInfo.UP, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.FLIP_UP, SizeInfo.SOUTH, SizeInfo.FLIP_UP, SizeInfo.FLIP_SOUTH, SizeInfo.UP, SizeInfo.FLIP_SOUTH, SizeInfo.UP, SizeInfo.SOUTH});

        final Direction[] corners;
        final boolean doNonCubicWeight;
        final SizeInfo[] vert0Weights;
        final SizeInfo[] vert1Weights;
        final SizeInfo[] vert2Weights;
        final SizeInfo[] vert3Weights;
        private static final AdjacencyInfo[] BY_FACING;

        private AdjacencyInfo(Direction[] directions, float f, boolean bl, SizeInfo[] sizeInfos, SizeInfo[] sizeInfos2, SizeInfo[] sizeInfos3, SizeInfo[] sizeInfos4) {
            this.corners = directions;
            this.doNonCubicWeight = bl;
            this.vert0Weights = sizeInfos;
            this.vert1Weights = sizeInfos2;
            this.vert2Weights = sizeInfos3;
            this.vert3Weights = sizeInfos4;
        }

        public static AdjacencyInfo fromFacing(Direction direction) {
            return BY_FACING[direction.get3DDataValue()];
        }

        static {
            BY_FACING = Util.make(new AdjacencyInfo[6], adjacencyInfos -> {
                adjacencyInfos[Direction.DOWN.get3DDataValue()] = DOWN;
                adjacencyInfos[Direction.UP.get3DDataValue()] = UP;
                adjacencyInfos[Direction.NORTH.get3DDataValue()] = NORTH;
                adjacencyInfos[Direction.SOUTH.get3DDataValue()] = SOUTH;
                adjacencyInfos[Direction.WEST.get3DDataValue()] = WEST;
                adjacencyInfos[Direction.EAST.get3DDataValue()] = EAST;
            });
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static enum SizeInfo {
        DOWN(Direction.DOWN, false),
        UP(Direction.UP, false),
        NORTH(Direction.NORTH, false),
        SOUTH(Direction.SOUTH, false),
        WEST(Direction.WEST, false),
        EAST(Direction.EAST, false),
        FLIP_DOWN(Direction.DOWN, true),
        FLIP_UP(Direction.UP, true),
        FLIP_NORTH(Direction.NORTH, true),
        FLIP_SOUTH(Direction.SOUTH, true),
        FLIP_WEST(Direction.WEST, true),
        FLIP_EAST(Direction.EAST, true);

        final int shape;

        private SizeInfo(Direction direction, boolean bl) {
            this.shape = direction.get3DDataValue() + (bl ? DIRECTIONS.length : 0);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static enum AmbientVertexRemap {
        DOWN(0, 1, 2, 3),
        UP(2, 3, 0, 1),
        NORTH(3, 0, 1, 2),
        SOUTH(0, 1, 2, 3),
        WEST(3, 0, 1, 2),
        EAST(1, 2, 3, 0);

        final int vert0;
        final int vert1;
        final int vert2;
        final int vert3;
        private static final AmbientVertexRemap[] BY_FACING;

        private AmbientVertexRemap(int j, int k, int l, int m) {
            this.vert0 = j;
            this.vert1 = k;
            this.vert2 = l;
            this.vert3 = m;
        }

        public static AmbientVertexRemap fromFacing(Direction direction) {
            return BY_FACING[direction.get3DDataValue()];
        }

        static {
            BY_FACING = Util.make(new AmbientVertexRemap[6], ambientVertexRemaps -> {
                ambientVertexRemaps[Direction.DOWN.get3DDataValue()] = DOWN;
                ambientVertexRemaps[Direction.UP.get3DDataValue()] = UP;
                ambientVertexRemaps[Direction.NORTH.get3DDataValue()] = NORTH;
                ambientVertexRemaps[Direction.SOUTH.get3DDataValue()] = SOUTH;
                ambientVertexRemaps[Direction.WEST.get3DDataValue()] = WEST;
                ambientVertexRemaps[Direction.EAST.get3DDataValue()] = EAST;
            });
        }
    }
}

