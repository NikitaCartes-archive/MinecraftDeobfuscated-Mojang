/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@Environment(value=EnvType.CLIENT)
public class LiquidBlockRenderer {
    private static final float MAX_FLUID_HEIGHT = 0.8888889f;
    private final TextureAtlasSprite[] lavaIcons = new TextureAtlasSprite[2];
    private final TextureAtlasSprite[] waterIcons = new TextureAtlasSprite[2];
    private TextureAtlasSprite waterOverlay;

    protected void setupSprites() {
        this.lavaIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.LAVA.defaultBlockState()).getParticleIcon();
        this.lavaIcons[1] = ModelBakery.LAVA_FLOW.sprite();
        this.waterIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.WATER.defaultBlockState()).getParticleIcon();
        this.waterIcons[1] = ModelBakery.WATER_FLOW.sprite();
        this.waterOverlay = ModelBakery.WATER_OVERLAY.sprite();
    }

    private static boolean isNeighborSameFluid(BlockGetter blockGetter, BlockPos blockPos, Direction direction, FluidState fluidState) {
        BlockPos blockPos2 = blockPos.relative(direction);
        FluidState fluidState2 = blockGetter.getFluidState(blockPos2);
        return fluidState2.getType().isSame(fluidState.getType());
    }

    private static boolean isFaceOccludedByState(BlockGetter blockGetter, Direction direction, float f, BlockPos blockPos, BlockState blockState) {
        if (blockState.canOcclude()) {
            VoxelShape voxelShape = Shapes.box(0.0, 0.0, 0.0, 1.0, f, 1.0);
            VoxelShape voxelShape2 = blockState.getOcclusionShape(blockGetter, blockPos);
            return Shapes.blockOccudes(voxelShape, voxelShape2, direction);
        }
        return false;
    }

    private static boolean isFaceOccludedByNeighbor(BlockGetter blockGetter, BlockPos blockPos, Direction direction, float f) {
        BlockPos blockPos2 = blockPos.relative(direction);
        BlockState blockState = blockGetter.getBlockState(blockPos2);
        return LiquidBlockRenderer.isFaceOccludedByState(blockGetter, direction, f, blockPos2, blockState);
    }

    private static boolean isFaceOccludedBySelf(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Direction direction) {
        return LiquidBlockRenderer.isFaceOccludedByState(blockGetter, direction.getOpposite(), 1.0f, blockPos, blockState);
    }

    public static boolean shouldRenderFace(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos, FluidState fluidState, BlockState blockState, Direction direction) {
        return !LiquidBlockRenderer.isFaceOccludedBySelf(blockAndTintGetter, blockPos, blockState, direction) && !LiquidBlockRenderer.isNeighborSameFluid(blockAndTintGetter, blockPos, direction, fluidState);
    }

    public boolean tesselate(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos, VertexConsumer vertexConsumer, FluidState fluidState) {
        float ak;
        float aj;
        float ab;
        float aa;
        float z;
        float y;
        float x;
        float w;
        float u;
        float t;
        boolean bl = fluidState.is(FluidTags.LAVA);
        TextureAtlasSprite[] textureAtlasSprites = bl ? this.lavaIcons : this.waterIcons;
        BlockState blockState = blockAndTintGetter.getBlockState(blockPos);
        int i = bl ? 0xFFFFFF : BiomeColors.getAverageWaterColor(blockAndTintGetter, blockPos);
        float f = (float)(i >> 16 & 0xFF) / 255.0f;
        float g = (float)(i >> 8 & 0xFF) / 255.0f;
        float h = (float)(i & 0xFF) / 255.0f;
        boolean bl2 = !LiquidBlockRenderer.isNeighborSameFluid(blockAndTintGetter, blockPos, Direction.UP, fluidState);
        boolean bl3 = LiquidBlockRenderer.shouldRenderFace(blockAndTintGetter, blockPos, fluidState, blockState, Direction.DOWN) && !LiquidBlockRenderer.isFaceOccludedByNeighbor(blockAndTintGetter, blockPos, Direction.DOWN, 0.8888889f);
        boolean bl4 = LiquidBlockRenderer.shouldRenderFace(blockAndTintGetter, blockPos, fluidState, blockState, Direction.NORTH);
        boolean bl5 = LiquidBlockRenderer.shouldRenderFace(blockAndTintGetter, blockPos, fluidState, blockState, Direction.SOUTH);
        boolean bl6 = LiquidBlockRenderer.shouldRenderFace(blockAndTintGetter, blockPos, fluidState, blockState, Direction.WEST);
        boolean bl7 = LiquidBlockRenderer.shouldRenderFace(blockAndTintGetter, blockPos, fluidState, blockState, Direction.EAST);
        if (!(bl2 || bl3 || bl7 || bl6 || bl4 || bl5)) {
            return false;
        }
        boolean bl8 = false;
        float j = blockAndTintGetter.getShade(Direction.DOWN, true);
        float k = blockAndTintGetter.getShade(Direction.UP, true);
        float l = blockAndTintGetter.getShade(Direction.NORTH, true);
        float m = blockAndTintGetter.getShade(Direction.WEST, true);
        float n = this.getWaterHeight(blockAndTintGetter, blockPos, fluidState.getType());
        float o = this.getWaterHeight(blockAndTintGetter, blockPos.south(), fluidState.getType());
        float p = this.getWaterHeight(blockAndTintGetter, blockPos.east().south(), fluidState.getType());
        float q = this.getWaterHeight(blockAndTintGetter, blockPos.east(), fluidState.getType());
        double d = blockPos.getX() & 0xF;
        double e = blockPos.getY() & 0xF;
        double r = blockPos.getZ() & 0xF;
        float s = 0.001f;
        float f2 = t = bl3 ? 0.001f : 0.0f;
        if (bl2 && !LiquidBlockRenderer.isFaceOccludedByNeighbor(blockAndTintGetter, blockPos, Direction.UP, Math.min(Math.min(n, o), Math.min(p, q)))) {
            float af;
            float ae;
            float ad;
            float ac;
            float v;
            bl8 = true;
            n -= 0.001f;
            o -= 0.001f;
            p -= 0.001f;
            q -= 0.001f;
            Vec3 vec3 = fluidState.getFlow(blockAndTintGetter, blockPos);
            if (vec3.x == 0.0 && vec3.z == 0.0) {
                textureAtlasSprite = textureAtlasSprites[0];
                u = textureAtlasSprite.getU(0.0);
                v = textureAtlasSprite.getV(0.0);
                w = u;
                x = textureAtlasSprite.getV(16.0);
                y = textureAtlasSprite.getU(16.0);
                z = x;
                aa = y;
                ab = v;
            } else {
                textureAtlasSprite = textureAtlasSprites[1];
                ac = (float)Mth.atan2(vec3.z, vec3.x) - 1.5707964f;
                ad = Mth.sin(ac) * 0.25f;
                ae = Mth.cos(ac) * 0.25f;
                af = 8.0f;
                u = textureAtlasSprite.getU(8.0f + (-ae - ad) * 16.0f);
                v = textureAtlasSprite.getV(8.0f + (-ae + ad) * 16.0f);
                w = textureAtlasSprite.getU(8.0f + (-ae + ad) * 16.0f);
                x = textureAtlasSprite.getV(8.0f + (ae + ad) * 16.0f);
                y = textureAtlasSprite.getU(8.0f + (ae + ad) * 16.0f);
                z = textureAtlasSprite.getV(8.0f + (ae - ad) * 16.0f);
                aa = textureAtlasSprite.getU(8.0f + (ae - ad) * 16.0f);
                ab = textureAtlasSprite.getV(8.0f + (-ae - ad) * 16.0f);
            }
            float ag = (u + w + y + aa) / 4.0f;
            ac = (v + x + z + ab) / 4.0f;
            ad = (float)textureAtlasSprites[0].getWidth() / (textureAtlasSprites[0].getU1() - textureAtlasSprites[0].getU0());
            ae = (float)textureAtlasSprites[0].getHeight() / (textureAtlasSprites[0].getV1() - textureAtlasSprites[0].getV0());
            af = 4.0f / Math.max(ae, ad);
            u = Mth.lerp(af, u, ag);
            w = Mth.lerp(af, w, ag);
            y = Mth.lerp(af, y, ag);
            aa = Mth.lerp(af, aa, ag);
            v = Mth.lerp(af, v, ac);
            x = Mth.lerp(af, x, ac);
            z = Mth.lerp(af, z, ac);
            ab = Mth.lerp(af, ab, ac);
            int ah = this.getLightColor(blockAndTintGetter, blockPos);
            float ai = k * f;
            aj = k * g;
            ak = k * h;
            this.vertex(vertexConsumer, d + 0.0, e + (double)n, r + 0.0, ai, aj, ak, u, v, ah);
            this.vertex(vertexConsumer, d + 0.0, e + (double)o, r + 1.0, ai, aj, ak, w, x, ah);
            this.vertex(vertexConsumer, d + 1.0, e + (double)p, r + 1.0, ai, aj, ak, y, z, ah);
            this.vertex(vertexConsumer, d + 1.0, e + (double)q, r + 0.0, ai, aj, ak, aa, ab, ah);
            if (fluidState.shouldRenderBackwardUpFace(blockAndTintGetter, blockPos.above())) {
                this.vertex(vertexConsumer, d + 0.0, e + (double)n, r + 0.0, ai, aj, ak, u, v, ah);
                this.vertex(vertexConsumer, d + 1.0, e + (double)q, r + 0.0, ai, aj, ak, aa, ab, ah);
                this.vertex(vertexConsumer, d + 1.0, e + (double)p, r + 1.0, ai, aj, ak, y, z, ah);
                this.vertex(vertexConsumer, d + 0.0, e + (double)o, r + 1.0, ai, aj, ak, w, x, ah);
            }
        }
        if (bl3) {
            u = textureAtlasSprites[0].getU0();
            w = textureAtlasSprites[0].getU1();
            y = textureAtlasSprites[0].getV0();
            aa = textureAtlasSprites[0].getV1();
            int al = this.getLightColor(blockAndTintGetter, blockPos.below());
            x = j * f;
            z = j * g;
            ab = j * h;
            this.vertex(vertexConsumer, d, e + (double)t, r + 1.0, x, z, ab, u, aa, al);
            this.vertex(vertexConsumer, d, e + (double)t, r, x, z, ab, u, y, al);
            this.vertex(vertexConsumer, d + 1.0, e + (double)t, r, x, z, ab, w, y, al);
            this.vertex(vertexConsumer, d + 1.0, e + (double)t, r + 1.0, x, z, ab, w, aa, al);
            bl8 = true;
        }
        int am = this.getLightColor(blockAndTintGetter, blockPos);
        for (int an = 0; an < 4; ++an) {
            Block block;
            boolean bl9;
            Direction direction;
            double ar;
            double aq;
            double ap;
            double ao;
            if (an == 0) {
                y = n;
                aa = q;
                ao = d;
                ap = d + 1.0;
                aq = r + (double)0.001f;
                ar = r + (double)0.001f;
                direction = Direction.NORTH;
                bl9 = bl4;
            } else if (an == 1) {
                y = p;
                aa = o;
                ao = d + 1.0;
                ap = d;
                aq = r + 1.0 - (double)0.001f;
                ar = r + 1.0 - (double)0.001f;
                direction = Direction.SOUTH;
                bl9 = bl5;
            } else if (an == 2) {
                y = o;
                aa = n;
                ao = d + (double)0.001f;
                ap = d + (double)0.001f;
                aq = r + 1.0;
                ar = r;
                direction = Direction.WEST;
                bl9 = bl6;
            } else {
                y = q;
                aa = p;
                ao = d + 1.0 - (double)0.001f;
                ap = d + 1.0 - (double)0.001f;
                aq = r;
                ar = r + 1.0;
                direction = Direction.EAST;
                bl9 = bl7;
            }
            if (!bl9 || LiquidBlockRenderer.isFaceOccludedByNeighbor(blockAndTintGetter, blockPos, direction, Math.max(y, aa))) continue;
            bl8 = true;
            BlockPos blockPos2 = blockPos.relative(direction);
            TextureAtlasSprite textureAtlasSprite2 = textureAtlasSprites[1];
            if (!bl && ((block = blockAndTintGetter.getBlockState(blockPos2).getBlock()) instanceof HalfTransparentBlock || block instanceof LeavesBlock)) {
                textureAtlasSprite2 = this.waterOverlay;
            }
            aj = textureAtlasSprite2.getU(0.0);
            ak = textureAtlasSprite2.getU(8.0);
            float as = textureAtlasSprite2.getV((1.0f - y) * 16.0f * 0.5f);
            float at = textureAtlasSprite2.getV((1.0f - aa) * 16.0f * 0.5f);
            float au = textureAtlasSprite2.getV(8.0);
            float av = an < 2 ? l : m;
            float aw = k * av * f;
            float ax = k * av * g;
            float ay = k * av * h;
            this.vertex(vertexConsumer, ao, e + (double)y, aq, aw, ax, ay, aj, as, am);
            this.vertex(vertexConsumer, ap, e + (double)aa, ar, aw, ax, ay, ak, at, am);
            this.vertex(vertexConsumer, ap, e + (double)t, ar, aw, ax, ay, ak, au, am);
            this.vertex(vertexConsumer, ao, e + (double)t, aq, aw, ax, ay, aj, au, am);
            if (textureAtlasSprite2 == this.waterOverlay) continue;
            this.vertex(vertexConsumer, ao, e + (double)t, aq, aw, ax, ay, aj, au, am);
            this.vertex(vertexConsumer, ap, e + (double)t, ar, aw, ax, ay, ak, au, am);
            this.vertex(vertexConsumer, ap, e + (double)aa, ar, aw, ax, ay, ak, at, am);
            this.vertex(vertexConsumer, ao, e + (double)y, aq, aw, ax, ay, aj, as, am);
        }
        return bl8;
    }

    private void vertex(VertexConsumer vertexConsumer, double d, double e, double f, float g, float h, float i, float j, float k, int l) {
        vertexConsumer.vertex(d, e, f).color(g, h, i, 1.0f).uv(j, k).uv2(l).normal(0.0f, 1.0f, 0.0f).endVertex();
    }

    private int getLightColor(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos) {
        int i = LevelRenderer.getLightColor(blockAndTintGetter, blockPos);
        int j = LevelRenderer.getLightColor(blockAndTintGetter, blockPos.above());
        int k = i & 0xFF;
        int l = j & 0xFF;
        int m = i >> 16 & 0xFF;
        int n = j >> 16 & 0xFF;
        return (k > l ? k : l) | (m > n ? m : n) << 16;
    }

    private float getWaterHeight(BlockGetter blockGetter, BlockPos blockPos, Fluid fluid) {
        int i = 0;
        float f = 0.0f;
        for (int j = 0; j < 4; ++j) {
            BlockPos blockPos2 = blockPos.offset(-(j & 1), 0, -(j >> 1 & 1));
            if (blockGetter.getFluidState(blockPos2.above()).getType().isSame(fluid)) {
                return 1.0f;
            }
            FluidState fluidState = blockGetter.getFluidState(blockPos2);
            if (fluidState.getType().isSame(fluid)) {
                float g = fluidState.getHeight(blockGetter, blockPos2);
                if (g >= 0.8f) {
                    f += g * 10.0f;
                    i += 10;
                    continue;
                }
                f += g;
                ++i;
                continue;
            }
            if (blockGetter.getBlockState(blockPos2).getMaterial().isSolid()) continue;
            ++i;
        }
        return f / (float)i;
    }
}

