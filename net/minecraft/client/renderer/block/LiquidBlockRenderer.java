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

    private static boolean isNeighborSameFluid(FluidState fluidState, FluidState fluidState2) {
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

    private static boolean isFaceOccludedByNeighbor(BlockGetter blockGetter, BlockPos blockPos, Direction direction, float f, BlockState blockState) {
        return LiquidBlockRenderer.isFaceOccludedByState(blockGetter, direction, f, blockPos.relative(direction), blockState);
    }

    private static boolean isFaceOccludedBySelf(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Direction direction) {
        return LiquidBlockRenderer.isFaceOccludedByState(blockGetter, direction.getOpposite(), 1.0f, blockPos, blockState);
    }

    public static boolean shouldRenderFace(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos, FluidState fluidState, BlockState blockState, Direction direction, FluidState fluidState2) {
        return !LiquidBlockRenderer.isFaceOccludedBySelf(blockAndTintGetter, blockPos, blockState, direction) && !LiquidBlockRenderer.isNeighborSameFluid(fluidState, fluidState2);
    }

    public void tesselate(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
        float ag;
        float af;
        float ae;
        float ad;
        float ac;
        float ab;
        float z;
        float y;
        float r;
        float q;
        float p;
        float o;
        boolean bl = fluidState.is(FluidTags.LAVA);
        TextureAtlasSprite[] textureAtlasSprites = bl ? this.lavaIcons : this.waterIcons;
        int i = bl ? 0xFFFFFF : BiomeColors.getAverageWaterColor(blockAndTintGetter, blockPos);
        float f = (float)(i >> 16 & 0xFF) / 255.0f;
        float g = (float)(i >> 8 & 0xFF) / 255.0f;
        float h = (float)(i & 0xFF) / 255.0f;
        BlockState blockState2 = blockAndTintGetter.getBlockState(blockPos.relative(Direction.DOWN));
        FluidState fluidState2 = blockState2.getFluidState();
        BlockState blockState3 = blockAndTintGetter.getBlockState(blockPos.relative(Direction.UP));
        FluidState fluidState3 = blockState3.getFluidState();
        BlockState blockState4 = blockAndTintGetter.getBlockState(blockPos.relative(Direction.NORTH));
        FluidState fluidState4 = blockState4.getFluidState();
        BlockState blockState5 = blockAndTintGetter.getBlockState(blockPos.relative(Direction.SOUTH));
        FluidState fluidState5 = blockState5.getFluidState();
        BlockState blockState6 = blockAndTintGetter.getBlockState(blockPos.relative(Direction.WEST));
        FluidState fluidState6 = blockState6.getFluidState();
        BlockState blockState7 = blockAndTintGetter.getBlockState(blockPos.relative(Direction.EAST));
        FluidState fluidState7 = blockState7.getFluidState();
        boolean bl2 = !LiquidBlockRenderer.isNeighborSameFluid(fluidState, fluidState3);
        boolean bl3 = LiquidBlockRenderer.shouldRenderFace(blockAndTintGetter, blockPos, fluidState, blockState, Direction.DOWN, fluidState2) && !LiquidBlockRenderer.isFaceOccludedByNeighbor(blockAndTintGetter, blockPos, Direction.DOWN, 0.8888889f, blockState2);
        boolean bl4 = LiquidBlockRenderer.shouldRenderFace(blockAndTintGetter, blockPos, fluidState, blockState, Direction.NORTH, fluidState4);
        boolean bl5 = LiquidBlockRenderer.shouldRenderFace(blockAndTintGetter, blockPos, fluidState, blockState, Direction.SOUTH, fluidState5);
        boolean bl6 = LiquidBlockRenderer.shouldRenderFace(blockAndTintGetter, blockPos, fluidState, blockState, Direction.WEST, fluidState6);
        boolean bl7 = LiquidBlockRenderer.shouldRenderFace(blockAndTintGetter, blockPos, fluidState, blockState, Direction.EAST, fluidState7);
        if (!(bl2 || bl3 || bl7 || bl6 || bl4 || bl5)) {
            return;
        }
        float j = blockAndTintGetter.getShade(Direction.DOWN, true);
        float k = blockAndTintGetter.getShade(Direction.UP, true);
        float l = blockAndTintGetter.getShade(Direction.NORTH, true);
        float m = blockAndTintGetter.getShade(Direction.WEST, true);
        Fluid fluid = fluidState.getType();
        float n = this.getHeight(blockAndTintGetter, fluid, blockPos, blockState, fluidState);
        if (n >= 1.0f) {
            o = 1.0f;
            p = 1.0f;
            q = 1.0f;
            r = 1.0f;
        } else {
            float s = this.getHeight(blockAndTintGetter, fluid, blockPos.north(), blockState4, fluidState4);
            float t = this.getHeight(blockAndTintGetter, fluid, blockPos.south(), blockState5, fluidState5);
            float u = this.getHeight(blockAndTintGetter, fluid, blockPos.east(), blockState7, fluidState7);
            float v = this.getHeight(blockAndTintGetter, fluid, blockPos.west(), blockState6, fluidState6);
            o = this.calculateAverageHeight(blockAndTintGetter, fluid, n, s, u, blockPos.relative(Direction.NORTH).relative(Direction.EAST));
            p = this.calculateAverageHeight(blockAndTintGetter, fluid, n, s, v, blockPos.relative(Direction.NORTH).relative(Direction.WEST));
            q = this.calculateAverageHeight(blockAndTintGetter, fluid, n, t, u, blockPos.relative(Direction.SOUTH).relative(Direction.EAST));
            r = this.calculateAverageHeight(blockAndTintGetter, fluid, n, t, v, blockPos.relative(Direction.SOUTH).relative(Direction.WEST));
        }
        double d = blockPos.getX() & 0xF;
        double e = blockPos.getY() & 0xF;
        double w = blockPos.getZ() & 0xF;
        float x = 0.001f;
        float f2 = y = bl3 ? 0.001f : 0.0f;
        if (bl2 && !LiquidBlockRenderer.isFaceOccludedByNeighbor(blockAndTintGetter, blockPos, Direction.UP, Math.min(Math.min(p, r), Math.min(q, o)), blockState3)) {
            float ak;
            float ai;
            float ah;
            float aa;
            p -= 0.001f;
            r -= 0.001f;
            q -= 0.001f;
            o -= 0.001f;
            Vec3 vec3 = fluidState.getFlow(blockAndTintGetter, blockPos);
            if (vec3.x == 0.0 && vec3.z == 0.0) {
                textureAtlasSprite = textureAtlasSprites[0];
                z = textureAtlasSprite.getU(0.0);
                aa = textureAtlasSprite.getV(0.0);
                ab = z;
                ac = textureAtlasSprite.getV(16.0);
                ad = textureAtlasSprite.getU(16.0);
                ae = ac;
                af = ad;
                ag = aa;
            } else {
                textureAtlasSprite = textureAtlasSprites[1];
                ah = (float)Mth.atan2(vec3.z, vec3.x) - 1.5707964f;
                ai = Mth.sin(ah) * 0.25f;
                float aj = Mth.cos(ah) * 0.25f;
                ak = 8.0f;
                z = textureAtlasSprite.getU(8.0f + (-aj - ai) * 16.0f);
                aa = textureAtlasSprite.getV(8.0f + (-aj + ai) * 16.0f);
                ab = textureAtlasSprite.getU(8.0f + (-aj + ai) * 16.0f);
                ac = textureAtlasSprite.getV(8.0f + (aj + ai) * 16.0f);
                ad = textureAtlasSprite.getU(8.0f + (aj + ai) * 16.0f);
                ae = textureAtlasSprite.getV(8.0f + (aj - ai) * 16.0f);
                af = textureAtlasSprite.getU(8.0f + (aj - ai) * 16.0f);
                ag = textureAtlasSprite.getV(8.0f + (-aj - ai) * 16.0f);
            }
            float al = (z + ab + ad + af) / 4.0f;
            ah = (aa + ac + ae + ag) / 4.0f;
            ai = textureAtlasSprites[0].uvShrinkRatio();
            z = Mth.lerp(ai, z, al);
            ab = Mth.lerp(ai, ab, al);
            ad = Mth.lerp(ai, ad, al);
            af = Mth.lerp(ai, af, al);
            aa = Mth.lerp(ai, aa, ah);
            ac = Mth.lerp(ai, ac, ah);
            ae = Mth.lerp(ai, ae, ah);
            ag = Mth.lerp(ai, ag, ah);
            int am = this.getLightColor(blockAndTintGetter, blockPos);
            ak = k * f;
            float an = k * g;
            float ao = k * h;
            this.vertex(vertexConsumer, d + 0.0, e + (double)p, w + 0.0, ak, an, ao, z, aa, am);
            this.vertex(vertexConsumer, d + 0.0, e + (double)r, w + 1.0, ak, an, ao, ab, ac, am);
            this.vertex(vertexConsumer, d + 1.0, e + (double)q, w + 1.0, ak, an, ao, ad, ae, am);
            this.vertex(vertexConsumer, d + 1.0, e + (double)o, w + 0.0, ak, an, ao, af, ag, am);
            if (fluidState.shouldRenderBackwardUpFace(blockAndTintGetter, blockPos.above())) {
                this.vertex(vertexConsumer, d + 0.0, e + (double)p, w + 0.0, ak, an, ao, z, aa, am);
                this.vertex(vertexConsumer, d + 1.0, e + (double)o, w + 0.0, ak, an, ao, af, ag, am);
                this.vertex(vertexConsumer, d + 1.0, e + (double)q, w + 1.0, ak, an, ao, ad, ae, am);
                this.vertex(vertexConsumer, d + 0.0, e + (double)r, w + 1.0, ak, an, ao, ab, ac, am);
            }
        }
        if (bl3) {
            z = textureAtlasSprites[0].getU0();
            ab = textureAtlasSprites[0].getU1();
            ad = textureAtlasSprites[0].getV0();
            af = textureAtlasSprites[0].getV1();
            int ap = this.getLightColor(blockAndTintGetter, blockPos.below());
            ac = j * f;
            ae = j * g;
            ag = j * h;
            this.vertex(vertexConsumer, d, e + (double)y, w + 1.0, ac, ae, ag, z, af, ap);
            this.vertex(vertexConsumer, d, e + (double)y, w, ac, ae, ag, z, ad, ap);
            this.vertex(vertexConsumer, d + 1.0, e + (double)y, w, ac, ae, ag, ab, ad, ap);
            this.vertex(vertexConsumer, d + 1.0, e + (double)y, w + 1.0, ac, ae, ag, ab, af, ap);
        }
        int aq = this.getLightColor(blockAndTintGetter, blockPos);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            Block block;
            double au;
            double at;
            double as;
            double ar;
            float aa;
            if (!(switch (direction) {
                case Direction.NORTH -> {
                    af = p;
                    aa = o;
                    ar = d;
                    as = d + 1.0;
                    at = w + (double)0.001f;
                    au = w + (double)0.001f;
                    yield bl4;
                }
                case Direction.SOUTH -> {
                    af = q;
                    aa = r;
                    ar = d + 1.0;
                    as = d;
                    at = w + 1.0 - (double)0.001f;
                    au = w + 1.0 - (double)0.001f;
                    yield bl5;
                }
                case Direction.WEST -> {
                    af = r;
                    aa = p;
                    ar = d + (double)0.001f;
                    as = d + (double)0.001f;
                    at = w + 1.0;
                    au = w;
                    yield bl6;
                }
                default -> {
                    af = o;
                    aa = q;
                    ar = d + 1.0 - (double)0.001f;
                    as = d + 1.0 - (double)0.001f;
                    at = w;
                    au = w + 1.0;
                    yield bl7;
                }
            }) || LiquidBlockRenderer.isFaceOccludedByNeighbor(blockAndTintGetter, blockPos, direction, Math.max(af, aa), blockAndTintGetter.getBlockState(blockPos.relative(direction)))) continue;
            BlockPos blockPos2 = blockPos.relative(direction);
            TextureAtlasSprite textureAtlasSprite2 = textureAtlasSprites[1];
            if (!bl && ((block = blockAndTintGetter.getBlockState(blockPos2).getBlock()) instanceof HalfTransparentBlock || block instanceof LeavesBlock)) {
                textureAtlasSprite2 = this.waterOverlay;
            }
            float av = textureAtlasSprite2.getU(0.0);
            float aw = textureAtlasSprite2.getU(8.0);
            float ax = textureAtlasSprite2.getV((1.0f - af) * 16.0f * 0.5f);
            float ay = textureAtlasSprite2.getV((1.0f - aa) * 16.0f * 0.5f);
            float az = textureAtlasSprite2.getV(8.0);
            float ba = direction.getAxis() == Direction.Axis.Z ? l : m;
            float bb = k * ba * f;
            float bc = k * ba * g;
            float bd = k * ba * h;
            this.vertex(vertexConsumer, ar, e + (double)af, at, bb, bc, bd, av, ax, aq);
            this.vertex(vertexConsumer, as, e + (double)aa, au, bb, bc, bd, aw, ay, aq);
            this.vertex(vertexConsumer, as, e + (double)y, au, bb, bc, bd, aw, az, aq);
            this.vertex(vertexConsumer, ar, e + (double)y, at, bb, bc, bd, av, az, aq);
            if (textureAtlasSprite2 == this.waterOverlay) continue;
            this.vertex(vertexConsumer, ar, e + (double)y, at, bb, bc, bd, av, az, aq);
            this.vertex(vertexConsumer, as, e + (double)y, au, bb, bc, bd, aw, az, aq);
            this.vertex(vertexConsumer, as, e + (double)aa, au, bb, bc, bd, aw, ay, aq);
            this.vertex(vertexConsumer, ar, e + (double)af, at, bb, bc, bd, av, ax, aq);
        }
    }

    private float calculateAverageHeight(BlockAndTintGetter blockAndTintGetter, Fluid fluid, float f, float g, float h, BlockPos blockPos) {
        if (h >= 1.0f || g >= 1.0f) {
            return 1.0f;
        }
        float[] fs = new float[2];
        if (h > 0.0f || g > 0.0f) {
            float i = this.getHeight(blockAndTintGetter, fluid, blockPos);
            if (i >= 1.0f) {
                return 1.0f;
            }
            this.addWeightedHeight(fs, i);
        }
        this.addWeightedHeight(fs, f);
        this.addWeightedHeight(fs, h);
        this.addWeightedHeight(fs, g);
        return fs[0] / fs[1];
    }

    private void addWeightedHeight(float[] fs, float f) {
        if (f >= 0.8f) {
            fs[0] = fs[0] + f * 10.0f;
            fs[1] = fs[1] + 10.0f;
        } else if (f >= 0.0f) {
            fs[0] = fs[0] + f;
            fs[1] = fs[1] + 1.0f;
        }
    }

    private float getHeight(BlockAndTintGetter blockAndTintGetter, Fluid fluid, BlockPos blockPos) {
        BlockState blockState = blockAndTintGetter.getBlockState(blockPos);
        return this.getHeight(blockAndTintGetter, fluid, blockPos, blockState, blockState.getFluidState());
    }

    private float getHeight(BlockAndTintGetter blockAndTintGetter, Fluid fluid, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        if (fluid.isSame(fluidState.getType())) {
            BlockState blockState2 = blockAndTintGetter.getBlockState(blockPos.above());
            if (fluid.isSame(blockState2.getFluidState().getType())) {
                return 1.0f;
            }
            return fluidState.getOwnHeight();
        }
        if (!blockState.getMaterial().isSolid()) {
            return 0.0f;
        }
        return -1.0f;
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
}

