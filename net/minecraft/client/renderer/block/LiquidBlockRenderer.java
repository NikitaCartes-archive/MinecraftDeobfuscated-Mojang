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
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@Environment(value=EnvType.CLIENT)
public class LiquidBlockRenderer {
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

    private static boolean isFaceOccluded(BlockGetter blockGetter, BlockPos blockPos, Direction direction, float f) {
        BlockPos blockPos2 = blockPos.relative(direction);
        BlockState blockState = blockGetter.getBlockState(blockPos2);
        if (blockState.canOcclude()) {
            VoxelShape voxelShape = Shapes.box(0.0, 0.0, 0.0, 1.0, f, 1.0);
            VoxelShape voxelShape2 = blockState.getOcclusionShape(blockGetter, blockPos2);
            return Shapes.blockOccudes(voxelShape, voxelShape2, direction);
        }
        return false;
    }

    public boolean tesselate(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos, VertexConsumer vertexConsumer, FluidState fluidState) {
        float ak;
        float aj;
        float ai;
        float ab;
        float aa;
        float z;
        float y;
        float x;
        float w;
        float u;
        float t;
        boolean bl7;
        boolean bl = fluidState.is(FluidTags.LAVA);
        TextureAtlasSprite[] textureAtlasSprites = bl ? this.lavaIcons : this.waterIcons;
        int i = bl ? 0xFFFFFF : BiomeColors.getAverageWaterColor(blockAndTintGetter, blockPos);
        float f = (float)(i >> 16 & 0xFF) / 255.0f;
        float g = (float)(i >> 8 & 0xFF) / 255.0f;
        float h = (float)(i & 0xFF) / 255.0f;
        boolean bl2 = !LiquidBlockRenderer.isNeighborSameFluid(blockAndTintGetter, blockPos, Direction.UP, fluidState);
        boolean bl3 = !LiquidBlockRenderer.isNeighborSameFluid(blockAndTintGetter, blockPos, Direction.DOWN, fluidState) && !LiquidBlockRenderer.isFaceOccluded(blockAndTintGetter, blockPos, Direction.DOWN, 0.8888889f);
        boolean bl4 = !LiquidBlockRenderer.isNeighborSameFluid(blockAndTintGetter, blockPos, Direction.NORTH, fluidState);
        boolean bl5 = !LiquidBlockRenderer.isNeighborSameFluid(blockAndTintGetter, blockPos, Direction.SOUTH, fluidState);
        boolean bl6 = !LiquidBlockRenderer.isNeighborSameFluid(blockAndTintGetter, blockPos, Direction.WEST, fluidState);
        boolean bl8 = bl7 = !LiquidBlockRenderer.isNeighborSameFluid(blockAndTintGetter, blockPos, Direction.EAST, fluidState);
        if (!(bl2 || bl3 || bl7 || bl6 || bl4 || bl5)) {
            return false;
        }
        boolean bl82 = false;
        float j = 0.5f;
        float k = 1.0f;
        float l = 0.8f;
        float m = 0.6f;
        float n = this.getWaterHeight(blockAndTintGetter, blockPos, fluidState.getType());
        float o = this.getWaterHeight(blockAndTintGetter, blockPos.south(), fluidState.getType());
        float p = this.getWaterHeight(blockAndTintGetter, blockPos.east().south(), fluidState.getType());
        float q = this.getWaterHeight(blockAndTintGetter, blockPos.east(), fluidState.getType());
        double d = blockPos.getX() & 0xF;
        double e = blockPos.getY() & 0xF;
        double r = blockPos.getZ() & 0xF;
        float s = 0.001f;
        float f2 = t = bl3 ? 0.001f : 0.0f;
        if (bl2 && !LiquidBlockRenderer.isFaceOccluded(blockAndTintGetter, blockPos, Direction.UP, Math.min(Math.min(n, o), Math.min(p, q)))) {
            float af;
            float ae;
            float ad;
            float ac;
            float v;
            TextureAtlasSprite textureAtlasSprite;
            bl82 = true;
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
            ai = 1.0f * f;
            aj = 1.0f * g;
            ak = 1.0f * h;
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
            x = 0.5f * f;
            z = 0.5f * g;
            ab = 0.5f * h;
            this.vertex(vertexConsumer, d, e + (double)t, r + 1.0, x, z, ab, u, aa, al);
            this.vertex(vertexConsumer, d, e + (double)t, r, x, z, ab, u, y, al);
            this.vertex(vertexConsumer, d + 1.0, e + (double)t, r, x, z, ab, w, y, al);
            this.vertex(vertexConsumer, d + 1.0, e + (double)t, r + 1.0, x, z, ab, w, aa, al);
            bl82 = true;
        }
        for (int am = 0; am < 4; ++am) {
            Block block;
            boolean bl9;
            Direction direction;
            double aq;
            double ap;
            double ao;
            double an;
            if (am == 0) {
                w = n;
                y = q;
                an = d;
                ao = d + 1.0;
                ap = r + (double)0.001f;
                aq = r + (double)0.001f;
                direction = Direction.NORTH;
                bl9 = bl4;
            } else if (am == 1) {
                w = p;
                y = o;
                an = d + 1.0;
                ao = d;
                ap = r + 1.0 - (double)0.001f;
                aq = r + 1.0 - (double)0.001f;
                direction = Direction.SOUTH;
                bl9 = bl5;
            } else if (am == 2) {
                w = o;
                y = n;
                an = d + (double)0.001f;
                ao = d + (double)0.001f;
                ap = r + 1.0;
                aq = r;
                direction = Direction.WEST;
                bl9 = bl6;
            } else {
                w = q;
                y = p;
                an = d + 1.0 - (double)0.001f;
                ao = d + 1.0 - (double)0.001f;
                ap = r;
                aq = r + 1.0;
                direction = Direction.EAST;
                bl9 = bl7;
            }
            if (!bl9 || LiquidBlockRenderer.isFaceOccluded(blockAndTintGetter, blockPos, direction, Math.max(w, y))) continue;
            bl82 = true;
            BlockPos blockPos2 = blockPos.relative(direction);
            TextureAtlasSprite textureAtlasSprite2 = textureAtlasSprites[1];
            if (!bl && ((block = blockAndTintGetter.getBlockState(blockPos2).getBlock()) == Blocks.GLASS || block instanceof StainedGlassBlock)) {
                textureAtlasSprite2 = this.waterOverlay;
            }
            ai = textureAtlasSprite2.getU(0.0);
            aj = textureAtlasSprite2.getU(8.0);
            ak = textureAtlasSprite2.getV((1.0f - w) * 16.0f * 0.5f);
            float ar = textureAtlasSprite2.getV((1.0f - y) * 16.0f * 0.5f);
            float as = textureAtlasSprite2.getV(8.0);
            int at = this.getLightColor(blockAndTintGetter, blockPos2);
            float au = am < 2 ? 0.8f : 0.6f;
            float av = 1.0f * au * f;
            float aw = 1.0f * au * g;
            float ax = 1.0f * au * h;
            this.vertex(vertexConsumer, an, e + (double)w, ap, av, aw, ax, ai, ak, at);
            this.vertex(vertexConsumer, ao, e + (double)y, aq, av, aw, ax, aj, ar, at);
            this.vertex(vertexConsumer, ao, e + (double)t, aq, av, aw, ax, aj, as, at);
            this.vertex(vertexConsumer, an, e + (double)t, ap, av, aw, ax, ai, as, at);
            if (textureAtlasSprite2 == this.waterOverlay) continue;
            this.vertex(vertexConsumer, an, e + (double)t, ap, av, aw, ax, ai, as, at);
            this.vertex(vertexConsumer, ao, e + (double)t, aq, av, aw, ax, aj, as, at);
            this.vertex(vertexConsumer, ao, e + (double)y, aq, av, aw, ax, aj, ar, at);
            this.vertex(vertexConsumer, an, e + (double)w, ap, av, aw, ax, ai, ak, at);
        }
        return bl82;
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

