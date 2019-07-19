/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
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
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockAndBiomeGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ModelBlockRenderer {
    private final BlockColors blockColors;
    private static final ThreadLocal<Cache> CACHE = ThreadLocal.withInitial(() -> new Cache());

    public ModelBlockRenderer(BlockColors blockColors) {
        this.blockColors = blockColors;
    }

    public boolean tesselateBlock(BlockAndBiomeGetter blockAndBiomeGetter, BakedModel bakedModel, BlockState blockState, BlockPos blockPos, BufferBuilder bufferBuilder, boolean bl, Random random, long l) {
        boolean bl2 = Minecraft.useAmbientOcclusion() && blockState.getLightEmission() == 0 && bakedModel.useAmbientOcclusion();
        try {
            if (bl2) {
                return this.tesselateWithAO(blockAndBiomeGetter, bakedModel, blockState, blockPos, bufferBuilder, bl, random, l);
            }
            return this.tesselateWithoutAO(blockAndBiomeGetter, bakedModel, blockState, blockPos, bufferBuilder, bl, random, l);
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Tesselating block model");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Block model being tesselated");
            CrashReportCategory.populateBlockDetails(crashReportCategory, blockPos, blockState);
            crashReportCategory.setDetail("Using AO", bl2);
            throw new ReportedException(crashReport);
        }
    }

    public boolean tesselateWithAO(BlockAndBiomeGetter blockAndBiomeGetter, BakedModel bakedModel, BlockState blockState, BlockPos blockPos, BufferBuilder bufferBuilder, boolean bl, Random random, long l) {
        boolean bl2 = false;
        float[] fs = new float[Direction.values().length * 2];
        BitSet bitSet = new BitSet(3);
        AmbientOcclusionFace ambientOcclusionFace = new AmbientOcclusionFace();
        for (Direction direction : Direction.values()) {
            random.setSeed(l);
            List<BakedQuad> list = bakedModel.getQuads(blockState, direction, random);
            if (list.isEmpty() || bl && !Block.shouldRenderFace(blockState, blockAndBiomeGetter, blockPos, direction)) continue;
            this.renderModelFaceAO(blockAndBiomeGetter, blockState, blockPos, bufferBuilder, list, fs, bitSet, ambientOcclusionFace);
            bl2 = true;
        }
        random.setSeed(l);
        List<BakedQuad> list2 = bakedModel.getQuads(blockState, null, random);
        if (!list2.isEmpty()) {
            this.renderModelFaceAO(blockAndBiomeGetter, blockState, blockPos, bufferBuilder, list2, fs, bitSet, ambientOcclusionFace);
            bl2 = true;
        }
        return bl2;
    }

    public boolean tesselateWithoutAO(BlockAndBiomeGetter blockAndBiomeGetter, BakedModel bakedModel, BlockState blockState, BlockPos blockPos, BufferBuilder bufferBuilder, boolean bl, Random random, long l) {
        boolean bl2 = false;
        BitSet bitSet = new BitSet(3);
        for (Direction direction : Direction.values()) {
            random.setSeed(l);
            List<BakedQuad> list = bakedModel.getQuads(blockState, direction, random);
            if (list.isEmpty() || bl && !Block.shouldRenderFace(blockState, blockAndBiomeGetter, blockPos, direction)) continue;
            int i = blockState.getLightColor(blockAndBiomeGetter, blockPos.relative(direction));
            this.renderModelFaceFlat(blockAndBiomeGetter, blockState, blockPos, i, false, bufferBuilder, list, bitSet);
            bl2 = true;
        }
        random.setSeed(l);
        List<BakedQuad> list2 = bakedModel.getQuads(blockState, null, random);
        if (!list2.isEmpty()) {
            this.renderModelFaceFlat(blockAndBiomeGetter, blockState, blockPos, -1, true, bufferBuilder, list2, bitSet);
            bl2 = true;
        }
        return bl2;
    }

    private void renderModelFaceAO(BlockAndBiomeGetter blockAndBiomeGetter, BlockState blockState, BlockPos blockPos, BufferBuilder bufferBuilder, List<BakedQuad> list, float[] fs, BitSet bitSet, AmbientOcclusionFace ambientOcclusionFace) {
        Vec3 vec3 = blockState.getOffset(blockAndBiomeGetter, blockPos);
        double d = (double)blockPos.getX() + vec3.x;
        double e = (double)blockPos.getY() + vec3.y;
        double f = (double)blockPos.getZ() + vec3.z;
        int j = list.size();
        for (int i = 0; i < j; ++i) {
            BakedQuad bakedQuad = list.get(i);
            this.calculateShape(blockAndBiomeGetter, blockState, blockPos, bakedQuad.getVertices(), bakedQuad.getDirection(), fs, bitSet);
            ambientOcclusionFace.calculate(blockAndBiomeGetter, blockState, blockPos, bakedQuad.getDirection(), fs, bitSet);
            bufferBuilder.putBulkData(bakedQuad.getVertices());
            bufferBuilder.faceTex2(ambientOcclusionFace.lightmap[0], ambientOcclusionFace.lightmap[1], ambientOcclusionFace.lightmap[2], ambientOcclusionFace.lightmap[3]);
            if (bakedQuad.isTinted()) {
                int k = this.blockColors.getColor(blockState, blockAndBiomeGetter, blockPos, bakedQuad.getTintIndex());
                float g = (float)(k >> 16 & 0xFF) / 255.0f;
                float h = (float)(k >> 8 & 0xFF) / 255.0f;
                float l = (float)(k & 0xFF) / 255.0f;
                bufferBuilder.faceTint(ambientOcclusionFace.brightness[0] * g, ambientOcclusionFace.brightness[0] * h, ambientOcclusionFace.brightness[0] * l, 4);
                bufferBuilder.faceTint(ambientOcclusionFace.brightness[1] * g, ambientOcclusionFace.brightness[1] * h, ambientOcclusionFace.brightness[1] * l, 3);
                bufferBuilder.faceTint(ambientOcclusionFace.brightness[2] * g, ambientOcclusionFace.brightness[2] * h, ambientOcclusionFace.brightness[2] * l, 2);
                bufferBuilder.faceTint(ambientOcclusionFace.brightness[3] * g, ambientOcclusionFace.brightness[3] * h, ambientOcclusionFace.brightness[3] * l, 1);
            } else {
                bufferBuilder.faceTint(ambientOcclusionFace.brightness[0], ambientOcclusionFace.brightness[0], ambientOcclusionFace.brightness[0], 4);
                bufferBuilder.faceTint(ambientOcclusionFace.brightness[1], ambientOcclusionFace.brightness[1], ambientOcclusionFace.brightness[1], 3);
                bufferBuilder.faceTint(ambientOcclusionFace.brightness[2], ambientOcclusionFace.brightness[2], ambientOcclusionFace.brightness[2], 2);
                bufferBuilder.faceTint(ambientOcclusionFace.brightness[3], ambientOcclusionFace.brightness[3], ambientOcclusionFace.brightness[3], 1);
            }
            bufferBuilder.postProcessFacePosition(d, e, f);
        }
    }

    private void calculateShape(BlockAndBiomeGetter blockAndBiomeGetter, BlockState blockState, BlockPos blockPos, int[] is, Direction direction, @Nullable float[] fs, BitSet bitSet) {
        float m;
        int l;
        float f = 32.0f;
        float g = 32.0f;
        float h = 32.0f;
        float i = -32.0f;
        float j = -32.0f;
        float k = -32.0f;
        for (l = 0; l < 4; ++l) {
            m = Float.intBitsToFloat(is[l * 7]);
            float n = Float.intBitsToFloat(is[l * 7 + 1]);
            float o = Float.intBitsToFloat(is[l * 7 + 2]);
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
            l = Direction.values().length;
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
                bitSet.set(0, g == j && (g < 1.0E-4f || blockState.isCollisionShapeFullBlock(blockAndBiomeGetter, blockPos)));
                break;
            }
            case UP: {
                bitSet.set(1, f >= 1.0E-4f || h >= 1.0E-4f || i <= 0.9999f || k <= 0.9999f);
                bitSet.set(0, g == j && (j > 0.9999f || blockState.isCollisionShapeFullBlock(blockAndBiomeGetter, blockPos)));
                break;
            }
            case NORTH: {
                bitSet.set(1, f >= 1.0E-4f || g >= 1.0E-4f || i <= 0.9999f || j <= 0.9999f);
                bitSet.set(0, h == k && (h < 1.0E-4f || blockState.isCollisionShapeFullBlock(blockAndBiomeGetter, blockPos)));
                break;
            }
            case SOUTH: {
                bitSet.set(1, f >= 1.0E-4f || g >= 1.0E-4f || i <= 0.9999f || j <= 0.9999f);
                bitSet.set(0, h == k && (k > 0.9999f || blockState.isCollisionShapeFullBlock(blockAndBiomeGetter, blockPos)));
                break;
            }
            case WEST: {
                bitSet.set(1, g >= 1.0E-4f || h >= 1.0E-4f || j <= 0.9999f || k <= 0.9999f);
                bitSet.set(0, f == i && (f < 1.0E-4f || blockState.isCollisionShapeFullBlock(blockAndBiomeGetter, blockPos)));
                break;
            }
            case EAST: {
                bitSet.set(1, g >= 1.0E-4f || h >= 1.0E-4f || j <= 0.9999f || k <= 0.9999f);
                bitSet.set(0, f == i && (i > 0.9999f || blockState.isCollisionShapeFullBlock(blockAndBiomeGetter, blockPos)));
            }
        }
    }

    private void renderModelFaceFlat(BlockAndBiomeGetter blockAndBiomeGetter, BlockState blockState, BlockPos blockPos, int i, boolean bl, BufferBuilder bufferBuilder, List<BakedQuad> list, BitSet bitSet) {
        Vec3 vec3 = blockState.getOffset(blockAndBiomeGetter, blockPos);
        double d = (double)blockPos.getX() + vec3.x;
        double e = (double)blockPos.getY() + vec3.y;
        double f = (double)blockPos.getZ() + vec3.z;
        int k = list.size();
        for (int j = 0; j < k; ++j) {
            BakedQuad bakedQuad = list.get(j);
            if (bl) {
                this.calculateShape(blockAndBiomeGetter, blockState, blockPos, bakedQuad.getVertices(), bakedQuad.getDirection(), null, bitSet);
                BlockPos blockPos2 = bitSet.get(0) ? blockPos.relative(bakedQuad.getDirection()) : blockPos;
                i = blockState.getLightColor(blockAndBiomeGetter, blockPos2);
            }
            bufferBuilder.putBulkData(bakedQuad.getVertices());
            bufferBuilder.faceTex2(i, i, i, i);
            if (bakedQuad.isTinted()) {
                int l = this.blockColors.getColor(blockState, blockAndBiomeGetter, blockPos, bakedQuad.getTintIndex());
                float g = (float)(l >> 16 & 0xFF) / 255.0f;
                float h = (float)(l >> 8 & 0xFF) / 255.0f;
                float m = (float)(l & 0xFF) / 255.0f;
                bufferBuilder.faceTint(g, h, m, 4);
                bufferBuilder.faceTint(g, h, m, 3);
                bufferBuilder.faceTint(g, h, m, 2);
                bufferBuilder.faceTint(g, h, m, 1);
            }
            bufferBuilder.postProcessFacePosition(d, e, f);
        }
    }

    public void renderModel(BakedModel bakedModel, float f, float g, float h, float i) {
        this.renderModel(null, bakedModel, f, g, h, i);
    }

    public void renderModel(@Nullable BlockState blockState, BakedModel bakedModel, float f, float g, float h, float i) {
        Random random = new Random();
        long l = 42L;
        for (Direction direction : Direction.values()) {
            random.setSeed(42L);
            this.renderQuadList(f, g, h, i, bakedModel.getQuads(blockState, direction, random));
        }
        random.setSeed(42L);
        this.renderQuadList(f, g, h, i, bakedModel.getQuads(blockState, null, random));
    }

    public void renderSingleBlock(BakedModel bakedModel, BlockState blockState, float f, boolean bl) {
        GlStateManager.rotatef(90.0f, 0.0f, 1.0f, 0.0f);
        int i = this.blockColors.getColor(blockState, null, null, 0);
        float g = (float)(i >> 16 & 0xFF) / 255.0f;
        float h = (float)(i >> 8 & 0xFF) / 255.0f;
        float j = (float)(i & 0xFF) / 255.0f;
        if (!bl) {
            GlStateManager.color4f(f, f, f, 1.0f);
        }
        this.renderModel(blockState, bakedModel, f, g, h, j);
    }

    private void renderQuadList(float f, float g, float h, float i, List<BakedQuad> list) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        int k = list.size();
        for (int j = 0; j < k; ++j) {
            BakedQuad bakedQuad = list.get(j);
            bufferBuilder.begin(7, DefaultVertexFormat.BLOCK_NORMALS);
            bufferBuilder.putBulkData(bakedQuad.getVertices());
            if (bakedQuad.isTinted()) {
                bufferBuilder.fixupQuadColor(g * f, h * f, i * f);
            } else {
                bufferBuilder.fixupQuadColor(f, f, f);
            }
            Vec3i vec3i = bakedQuad.getDirection().getNormal();
            bufferBuilder.postNormal(vec3i.getX(), vec3i.getY(), vec3i.getZ());
            tesselator.end();
        }
    }

    public static void enableCaching() {
        CACHE.get().enable();
    }

    public static void clearCache() {
        CACHE.get().disable();
    }

    @Environment(value=EnvType.CLIENT)
    public static enum AdjacencyInfo {
        DOWN(new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH}, 0.5f, true, new SizeInfo[]{SizeInfo.FLIP_WEST, SizeInfo.SOUTH, SizeInfo.FLIP_WEST, SizeInfo.FLIP_SOUTH, SizeInfo.WEST, SizeInfo.FLIP_SOUTH, SizeInfo.WEST, SizeInfo.SOUTH}, new SizeInfo[]{SizeInfo.FLIP_WEST, SizeInfo.NORTH, SizeInfo.FLIP_WEST, SizeInfo.FLIP_NORTH, SizeInfo.WEST, SizeInfo.FLIP_NORTH, SizeInfo.WEST, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.FLIP_EAST, SizeInfo.NORTH, SizeInfo.FLIP_EAST, SizeInfo.FLIP_NORTH, SizeInfo.EAST, SizeInfo.FLIP_NORTH, SizeInfo.EAST, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.FLIP_EAST, SizeInfo.SOUTH, SizeInfo.FLIP_EAST, SizeInfo.FLIP_SOUTH, SizeInfo.EAST, SizeInfo.FLIP_SOUTH, SizeInfo.EAST, SizeInfo.SOUTH}),
        UP(new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH}, 1.0f, true, new SizeInfo[]{SizeInfo.EAST, SizeInfo.SOUTH, SizeInfo.EAST, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_EAST, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_EAST, SizeInfo.SOUTH}, new SizeInfo[]{SizeInfo.EAST, SizeInfo.NORTH, SizeInfo.EAST, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_EAST, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_EAST, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.WEST, SizeInfo.NORTH, SizeInfo.WEST, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_WEST, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_WEST, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.WEST, SizeInfo.SOUTH, SizeInfo.WEST, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_WEST, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_WEST, SizeInfo.SOUTH}),
        NORTH(new Direction[]{Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST}, 0.8f, true, new SizeInfo[]{SizeInfo.UP, SizeInfo.FLIP_WEST, SizeInfo.UP, SizeInfo.WEST, SizeInfo.FLIP_UP, SizeInfo.WEST, SizeInfo.FLIP_UP, SizeInfo.FLIP_WEST}, new SizeInfo[]{SizeInfo.UP, SizeInfo.FLIP_EAST, SizeInfo.UP, SizeInfo.EAST, SizeInfo.FLIP_UP, SizeInfo.EAST, SizeInfo.FLIP_UP, SizeInfo.FLIP_EAST}, new SizeInfo[]{SizeInfo.DOWN, SizeInfo.FLIP_EAST, SizeInfo.DOWN, SizeInfo.EAST, SizeInfo.FLIP_DOWN, SizeInfo.EAST, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_EAST}, new SizeInfo[]{SizeInfo.DOWN, SizeInfo.FLIP_WEST, SizeInfo.DOWN, SizeInfo.WEST, SizeInfo.FLIP_DOWN, SizeInfo.WEST, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_WEST}),
        SOUTH(new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP}, 0.8f, true, new SizeInfo[]{SizeInfo.UP, SizeInfo.FLIP_WEST, SizeInfo.FLIP_UP, SizeInfo.FLIP_WEST, SizeInfo.FLIP_UP, SizeInfo.WEST, SizeInfo.UP, SizeInfo.WEST}, new SizeInfo[]{SizeInfo.DOWN, SizeInfo.FLIP_WEST, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_WEST, SizeInfo.FLIP_DOWN, SizeInfo.WEST, SizeInfo.DOWN, SizeInfo.WEST}, new SizeInfo[]{SizeInfo.DOWN, SizeInfo.FLIP_EAST, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_EAST, SizeInfo.FLIP_DOWN, SizeInfo.EAST, SizeInfo.DOWN, SizeInfo.EAST}, new SizeInfo[]{SizeInfo.UP, SizeInfo.FLIP_EAST, SizeInfo.FLIP_UP, SizeInfo.FLIP_EAST, SizeInfo.FLIP_UP, SizeInfo.EAST, SizeInfo.UP, SizeInfo.EAST}),
        WEST(new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH}, 0.6f, true, new SizeInfo[]{SizeInfo.UP, SizeInfo.SOUTH, SizeInfo.UP, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_UP, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_UP, SizeInfo.SOUTH}, new SizeInfo[]{SizeInfo.UP, SizeInfo.NORTH, SizeInfo.UP, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_UP, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_UP, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.DOWN, SizeInfo.NORTH, SizeInfo.DOWN, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_DOWN, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.DOWN, SizeInfo.SOUTH, SizeInfo.DOWN, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_DOWN, SizeInfo.SOUTH}),
        EAST(new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH}, 0.6f, true, new SizeInfo[]{SizeInfo.FLIP_DOWN, SizeInfo.SOUTH, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_SOUTH, SizeInfo.DOWN, SizeInfo.FLIP_SOUTH, SizeInfo.DOWN, SizeInfo.SOUTH}, new SizeInfo[]{SizeInfo.FLIP_DOWN, SizeInfo.NORTH, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_NORTH, SizeInfo.DOWN, SizeInfo.FLIP_NORTH, SizeInfo.DOWN, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.FLIP_UP, SizeInfo.NORTH, SizeInfo.FLIP_UP, SizeInfo.FLIP_NORTH, SizeInfo.UP, SizeInfo.FLIP_NORTH, SizeInfo.UP, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.FLIP_UP, SizeInfo.SOUTH, SizeInfo.FLIP_UP, SizeInfo.FLIP_SOUTH, SizeInfo.UP, SizeInfo.FLIP_SOUTH, SizeInfo.UP, SizeInfo.SOUTH});

        private final Direction[] corners;
        private final boolean doNonCubicWeight;
        private final SizeInfo[] vert0Weights;
        private final SizeInfo[] vert1Weights;
        private final SizeInfo[] vert2Weights;
        private final SizeInfo[] vert3Weights;
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
    public static enum SizeInfo {
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

        private final int shape;

        private SizeInfo(Direction direction, boolean bl) {
            this.shape = direction.get3DDataValue() + (bl ? Direction.values().length : 0);
        }
    }

    @Environment(value=EnvType.CLIENT)
    class AmbientOcclusionFace {
        private final float[] brightness = new float[4];
        private final int[] lightmap = new int[4];

        public void calculate(BlockAndBiomeGetter blockAndBiomeGetter, BlockState blockState, BlockPos blockPos, Direction direction, float[] fs, BitSet bitSet) {
            int u;
            float t;
            int s;
            float r;
            int q;
            float p;
            int o;
            float n;
            BlockState blockState6;
            boolean bl4;
            BlockPos blockPos2 = bitSet.get(0) ? blockPos.relative(direction) : blockPos;
            AdjacencyInfo adjacencyInfo = AdjacencyInfo.fromFacing(direction);
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            Cache cache = (Cache)CACHE.get();
            mutableBlockPos.set(blockPos2).move(adjacencyInfo.corners[0]);
            BlockState blockState2 = blockAndBiomeGetter.getBlockState(mutableBlockPos);
            int i = cache.getLightColor(blockState2, blockAndBiomeGetter, mutableBlockPos);
            float f = cache.getShadeBrightness(blockState2, blockAndBiomeGetter, mutableBlockPos);
            mutableBlockPos.set(blockPos2).move(adjacencyInfo.corners[1]);
            BlockState blockState3 = blockAndBiomeGetter.getBlockState(mutableBlockPos);
            int j = cache.getLightColor(blockState3, blockAndBiomeGetter, mutableBlockPos);
            float g = cache.getShadeBrightness(blockState3, blockAndBiomeGetter, mutableBlockPos);
            mutableBlockPos.set(blockPos2).move(adjacencyInfo.corners[2]);
            BlockState blockState4 = blockAndBiomeGetter.getBlockState(mutableBlockPos);
            int k = cache.getLightColor(blockState4, blockAndBiomeGetter, mutableBlockPos);
            float h = cache.getShadeBrightness(blockState4, blockAndBiomeGetter, mutableBlockPos);
            mutableBlockPos.set(blockPos2).move(adjacencyInfo.corners[3]);
            BlockState blockState5 = blockAndBiomeGetter.getBlockState(mutableBlockPos);
            int l = cache.getLightColor(blockState5, blockAndBiomeGetter, mutableBlockPos);
            float m = cache.getShadeBrightness(blockState5, blockAndBiomeGetter, mutableBlockPos);
            mutableBlockPos.set(blockPos2).move(adjacencyInfo.corners[0]).move(direction);
            boolean bl = blockAndBiomeGetter.getBlockState(mutableBlockPos).getLightBlock(blockAndBiomeGetter, mutableBlockPos) == 0;
            mutableBlockPos.set(blockPos2).move(adjacencyInfo.corners[1]).move(direction);
            boolean bl2 = blockAndBiomeGetter.getBlockState(mutableBlockPos).getLightBlock(blockAndBiomeGetter, mutableBlockPos) == 0;
            mutableBlockPos.set(blockPos2).move(adjacencyInfo.corners[2]).move(direction);
            boolean bl3 = blockAndBiomeGetter.getBlockState(mutableBlockPos).getLightBlock(blockAndBiomeGetter, mutableBlockPos) == 0;
            mutableBlockPos.set(blockPos2).move(adjacencyInfo.corners[3]).move(direction);
            boolean bl5 = bl4 = blockAndBiomeGetter.getBlockState(mutableBlockPos).getLightBlock(blockAndBiomeGetter, mutableBlockPos) == 0;
            if (bl3 || bl) {
                mutableBlockPos.set(blockPos2).move(adjacencyInfo.corners[0]).move(adjacencyInfo.corners[2]);
                blockState6 = blockAndBiomeGetter.getBlockState(mutableBlockPos);
                n = cache.getShadeBrightness(blockState6, blockAndBiomeGetter, mutableBlockPos);
                o = cache.getLightColor(blockState6, blockAndBiomeGetter, mutableBlockPos);
            } else {
                n = f;
                o = i;
            }
            if (bl4 || bl) {
                mutableBlockPos.set(blockPos2).move(adjacencyInfo.corners[0]).move(adjacencyInfo.corners[3]);
                blockState6 = blockAndBiomeGetter.getBlockState(mutableBlockPos);
                p = cache.getShadeBrightness(blockState6, blockAndBiomeGetter, mutableBlockPos);
                q = cache.getLightColor(blockState6, blockAndBiomeGetter, mutableBlockPos);
            } else {
                p = f;
                q = i;
            }
            if (bl3 || bl2) {
                mutableBlockPos.set(blockPos2).move(adjacencyInfo.corners[1]).move(adjacencyInfo.corners[2]);
                blockState6 = blockAndBiomeGetter.getBlockState(mutableBlockPos);
                r = cache.getShadeBrightness(blockState6, blockAndBiomeGetter, mutableBlockPos);
                s = cache.getLightColor(blockState6, blockAndBiomeGetter, mutableBlockPos);
            } else {
                r = f;
                s = i;
            }
            if (bl4 || bl2) {
                mutableBlockPos.set(blockPos2).move(adjacencyInfo.corners[1]).move(adjacencyInfo.corners[3]);
                blockState6 = blockAndBiomeGetter.getBlockState(mutableBlockPos);
                t = cache.getShadeBrightness(blockState6, blockAndBiomeGetter, mutableBlockPos);
                u = cache.getLightColor(blockState6, blockAndBiomeGetter, mutableBlockPos);
            } else {
                t = f;
                u = i;
            }
            int v = cache.getLightColor(blockState, blockAndBiomeGetter, blockPos);
            mutableBlockPos.set(blockPos).move(direction);
            BlockState blockState7 = blockAndBiomeGetter.getBlockState(mutableBlockPos);
            if (bitSet.get(0) || !blockState7.isSolidRender(blockAndBiomeGetter, mutableBlockPos)) {
                v = cache.getLightColor(blockState7, blockAndBiomeGetter, mutableBlockPos);
            }
            float w = bitSet.get(0) ? cache.getShadeBrightness(blockAndBiomeGetter.getBlockState(blockPos2), blockAndBiomeGetter, blockPos2) : cache.getShadeBrightness(blockAndBiomeGetter.getBlockState(blockPos), blockAndBiomeGetter, blockPos);
            AmbientVertexRemap ambientVertexRemap = AmbientVertexRemap.fromFacing(direction);
            if (!bitSet.get(1) || !adjacencyInfo.doNonCubicWeight) {
                float x = (m + f + p + w) * 0.25f;
                float y = (h + f + n + w) * 0.25f;
                float z = (h + g + r + w) * 0.25f;
                float aa = (m + g + t + w) * 0.25f;
                this.lightmap[((AmbientVertexRemap)ambientVertexRemap).vert0] = this.blend(l, i, q, v);
                this.lightmap[((AmbientVertexRemap)ambientVertexRemap).vert1] = this.blend(k, i, o, v);
                this.lightmap[((AmbientVertexRemap)ambientVertexRemap).vert2] = this.blend(k, j, s, v);
                this.lightmap[((AmbientVertexRemap)ambientVertexRemap).vert3] = this.blend(l, j, u, v);
                this.brightness[((AmbientVertexRemap)ambientVertexRemap).vert0] = x;
                this.brightness[((AmbientVertexRemap)ambientVertexRemap).vert1] = y;
                this.brightness[((AmbientVertexRemap)ambientVertexRemap).vert2] = z;
                this.brightness[((AmbientVertexRemap)ambientVertexRemap).vert3] = aa;
            } else {
                float x = (m + f + p + w) * 0.25f;
                float y = (h + f + n + w) * 0.25f;
                float z = (h + g + r + w) * 0.25f;
                float aa = (m + g + t + w) * 0.25f;
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
                this.brightness[((AmbientVertexRemap)ambientVertexRemap).vert0] = x * ab + y * ac + z * ad + aa * ae;
                this.brightness[((AmbientVertexRemap)ambientVertexRemap).vert1] = x * af + y * ag + z * ah + aa * ai;
                this.brightness[((AmbientVertexRemap)ambientVertexRemap).vert2] = x * aj + y * ak + z * al + aa * am;
                this.brightness[((AmbientVertexRemap)ambientVertexRemap).vert3] = x * an + y * ao + z * ap + aa * aq;
                int ar = this.blend(l, i, q, v);
                int as = this.blend(k, i, o, v);
                int at = this.blend(k, j, s, v);
                int au = this.blend(l, j, u, v);
                this.lightmap[((AmbientVertexRemap)ambientVertexRemap).vert0] = this.blend(ar, as, at, au, ab, ac, ad, ae);
                this.lightmap[((AmbientVertexRemap)ambientVertexRemap).vert1] = this.blend(ar, as, at, au, af, ag, ah, ai);
                this.lightmap[((AmbientVertexRemap)ambientVertexRemap).vert2] = this.blend(ar, as, at, au, aj, ak, al, am);
                this.lightmap[((AmbientVertexRemap)ambientVertexRemap).vert3] = this.blend(ar, as, at, au, an, ao, ap, aq);
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

        public int getLightColor(BlockState blockState, BlockAndBiomeGetter blockAndBiomeGetter, BlockPos blockPos) {
            int i;
            long l = blockPos.asLong();
            if (this.enabled && (i = this.colorCache.get(l)) != Integer.MAX_VALUE) {
                return i;
            }
            i = blockState.getLightColor(blockAndBiomeGetter, blockPos);
            if (this.enabled) {
                if (this.colorCache.size() == 100) {
                    this.colorCache.removeFirstInt();
                }
                this.colorCache.put(l, i);
            }
            return i;
        }

        public float getShadeBrightness(BlockState blockState, BlockAndBiomeGetter blockAndBiomeGetter, BlockPos blockPos) {
            float f;
            long l = blockPos.asLong();
            if (this.enabled && !Float.isNaN(f = this.brightnessCache.get(l))) {
                return f;
            }
            f = blockState.getShadeBrightness(blockAndBiomeGetter, blockPos);
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
    static enum AmbientVertexRemap {
        DOWN(0, 1, 2, 3),
        UP(2, 3, 0, 1),
        NORTH(3, 0, 1, 2),
        SOUTH(0, 1, 2, 3),
        WEST(3, 0, 1, 2),
        EAST(1, 2, 3, 0);

        private final int vert0;
        private final int vert1;
        private final int vert2;
        private final int vert3;
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

