/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndBiomeGetter;
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
        TextureAtlas textureAtlas = Minecraft.getInstance().getTextureAtlas();
        this.lavaIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.LAVA.defaultBlockState()).getParticleIcon();
        this.lavaIcons[1] = textureAtlas.getSprite(ModelBakery.LAVA_FLOW);
        this.waterIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.WATER.defaultBlockState()).getParticleIcon();
        this.waterIcons[1] = textureAtlas.getSprite(ModelBakery.WATER_FLOW);
        this.waterOverlay = textureAtlas.getSprite(ModelBakery.WATER_OVERLAY);
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

    public boolean tesselate(BlockAndBiomeGetter blockAndBiomeGetter, BlockPos blockPos, BufferBuilder bufferBuilder, FluidState fluidState) {
        float al;
        float ak;
        float aj;
        float af;
        float aa;
        float z;
        float x;
        float v;
        float t;
        boolean bl7;
        boolean bl = fluidState.is(FluidTags.LAVA);
        TextureAtlasSprite[] textureAtlasSprites = bl ? this.lavaIcons : this.waterIcons;
        int i = bl ? 0xFFFFFF : BiomeColors.getAverageWaterColor(blockAndBiomeGetter, blockPos);
        float f = (float)(i >> 16 & 0xFF) / 255.0f;
        float g = (float)(i >> 8 & 0xFF) / 255.0f;
        float h = (float)(i & 0xFF) / 255.0f;
        boolean bl2 = !LiquidBlockRenderer.isNeighborSameFluid(blockAndBiomeGetter, blockPos, Direction.UP, fluidState);
        boolean bl3 = !LiquidBlockRenderer.isNeighborSameFluid(blockAndBiomeGetter, blockPos, Direction.DOWN, fluidState) && !LiquidBlockRenderer.isFaceOccluded(blockAndBiomeGetter, blockPos, Direction.DOWN, 0.8888889f);
        boolean bl4 = !LiquidBlockRenderer.isNeighborSameFluid(blockAndBiomeGetter, blockPos, Direction.NORTH, fluidState);
        boolean bl5 = !LiquidBlockRenderer.isNeighborSameFluid(blockAndBiomeGetter, blockPos, Direction.SOUTH, fluidState);
        boolean bl6 = !LiquidBlockRenderer.isNeighborSameFluid(blockAndBiomeGetter, blockPos, Direction.WEST, fluidState);
        boolean bl8 = bl7 = !LiquidBlockRenderer.isNeighborSameFluid(blockAndBiomeGetter, blockPos, Direction.EAST, fluidState);
        if (!(bl2 || bl3 || bl7 || bl6 || bl4 || bl5)) {
            return false;
        }
        boolean bl82 = false;
        float j = 0.5f;
        float k = 1.0f;
        float l = 0.8f;
        float m = 0.6f;
        float n = this.getWaterHeight(blockAndBiomeGetter, blockPos, fluidState.getType());
        float o = this.getWaterHeight(blockAndBiomeGetter, blockPos.south(), fluidState.getType());
        float p = this.getWaterHeight(blockAndBiomeGetter, blockPos.east().south(), fluidState.getType());
        float q = this.getWaterHeight(blockAndBiomeGetter, blockPos.east(), fluidState.getType());
        double d = blockPos.getX();
        double e = blockPos.getY();
        double r = blockPos.getZ();
        float s = 0.001f;
        if (bl2 && !LiquidBlockRenderer.isFaceOccluded(blockAndBiomeGetter, blockPos, Direction.UP, Math.min(Math.min(n, o), Math.min(p, q)))) {
            float ae;
            float ad;
            float ac;
            float ab;
            float y;
            float w;
            float u;
            TextureAtlasSprite textureAtlasSprite;
            bl82 = true;
            n -= 0.001f;
            o -= 0.001f;
            p -= 0.001f;
            q -= 0.001f;
            Vec3 vec3 = fluidState.getFlow(blockAndBiomeGetter, blockPos);
            if (vec3.x == 0.0 && vec3.z == 0.0) {
                textureAtlasSprite = textureAtlasSprites[0];
                t = textureAtlasSprite.getU(0.0);
                u = textureAtlasSprite.getV(0.0);
                v = t;
                w = textureAtlasSprite.getV(16.0);
                x = textureAtlasSprite.getU(16.0);
                y = w;
                z = x;
                aa = u;
            } else {
                textureAtlasSprite = textureAtlasSprites[1];
                ab = (float)Mth.atan2(vec3.z, vec3.x) - 1.5707964f;
                ac = Mth.sin(ab) * 0.25f;
                ad = Mth.cos(ab) * 0.25f;
                ae = 8.0f;
                t = textureAtlasSprite.getU(8.0f + (-ad - ac) * 16.0f);
                u = textureAtlasSprite.getV(8.0f + (-ad + ac) * 16.0f);
                v = textureAtlasSprite.getU(8.0f + (-ad + ac) * 16.0f);
                w = textureAtlasSprite.getV(8.0f + (ad + ac) * 16.0f);
                x = textureAtlasSprite.getU(8.0f + (ad + ac) * 16.0f);
                y = textureAtlasSprite.getV(8.0f + (ad - ac) * 16.0f);
                z = textureAtlasSprite.getU(8.0f + (ad - ac) * 16.0f);
                aa = textureAtlasSprite.getV(8.0f + (-ad - ac) * 16.0f);
            }
            af = (t + v + x + z) / 4.0f;
            ab = (u + w + y + aa) / 4.0f;
            ac = (float)textureAtlasSprites[0].getWidth() / (textureAtlasSprites[0].getU1() - textureAtlasSprites[0].getU0());
            ad = (float)textureAtlasSprites[0].getHeight() / (textureAtlasSprites[0].getV1() - textureAtlasSprites[0].getV0());
            ae = 4.0f / Math.max(ad, ac);
            t = Mth.lerp(ae, t, af);
            v = Mth.lerp(ae, v, af);
            x = Mth.lerp(ae, x, af);
            z = Mth.lerp(ae, z, af);
            u = Mth.lerp(ae, u, ab);
            w = Mth.lerp(ae, w, ab);
            y = Mth.lerp(ae, y, ab);
            aa = Mth.lerp(ae, aa, ab);
            int ag = this.getLightColor(blockAndBiomeGetter, blockPos);
            int ah = ag >> 16 & 0xFFFF;
            int ai = ag & 0xFFFF;
            aj = 1.0f * f;
            ak = 1.0f * g;
            al = 1.0f * h;
            bufferBuilder.vertex(d + 0.0, e + (double)n, r + 0.0).color(aj, ak, al, 1.0f).uv(t, u).uv2(ah, ai).endVertex();
            bufferBuilder.vertex(d + 0.0, e + (double)o, r + 1.0).color(aj, ak, al, 1.0f).uv(v, w).uv2(ah, ai).endVertex();
            bufferBuilder.vertex(d + 1.0, e + (double)p, r + 1.0).color(aj, ak, al, 1.0f).uv(x, y).uv2(ah, ai).endVertex();
            bufferBuilder.vertex(d + 1.0, e + (double)q, r + 0.0).color(aj, ak, al, 1.0f).uv(z, aa).uv2(ah, ai).endVertex();
            if (fluidState.shouldRenderBackwardUpFace(blockAndBiomeGetter, blockPos.above())) {
                bufferBuilder.vertex(d + 0.0, e + (double)n, r + 0.0).color(aj, ak, al, 1.0f).uv(t, u).uv2(ah, ai).endVertex();
                bufferBuilder.vertex(d + 1.0, e + (double)q, r + 0.0).color(aj, ak, al, 1.0f).uv(z, aa).uv2(ah, ai).endVertex();
                bufferBuilder.vertex(d + 1.0, e + (double)p, r + 1.0).color(aj, ak, al, 1.0f).uv(x, y).uv2(ah, ai).endVertex();
                bufferBuilder.vertex(d + 0.0, e + (double)o, r + 1.0).color(aj, ak, al, 1.0f).uv(v, w).uv2(ah, ai).endVertex();
            }
        }
        if (bl3) {
            t = textureAtlasSprites[0].getU0();
            v = textureAtlasSprites[0].getU1();
            x = textureAtlasSprites[0].getV0();
            z = textureAtlasSprites[0].getV1();
            int am = this.getLightColor(blockAndBiomeGetter, blockPos.below());
            int an = am >> 16 & 0xFFFF;
            int ao = am & 0xFFFF;
            aa = 0.5f * f;
            float ap = 0.5f * g;
            af = 0.5f * h;
            bufferBuilder.vertex(d, e, r + 1.0).color(aa, ap, af, 1.0f).uv(t, z).uv2(an, ao).endVertex();
            bufferBuilder.vertex(d, e, r).color(aa, ap, af, 1.0f).uv(t, x).uv2(an, ao).endVertex();
            bufferBuilder.vertex(d + 1.0, e, r).color(aa, ap, af, 1.0f).uv(v, x).uv2(an, ao).endVertex();
            bufferBuilder.vertex(d + 1.0, e, r + 1.0).color(aa, ap, af, 1.0f).uv(v, z).uv2(an, ao).endVertex();
            bl82 = true;
        }
        for (int aq = 0; aq < 4; ++aq) {
            Block block;
            boolean bl9;
            Direction direction;
            double au;
            double at;
            double as;
            double ar;
            if (aq == 0) {
                v = n;
                x = q;
                ar = d;
                as = d + 1.0;
                at = r + (double)0.001f;
                au = r + (double)0.001f;
                direction = Direction.NORTH;
                bl9 = bl4;
            } else if (aq == 1) {
                v = p;
                x = o;
                ar = d + 1.0;
                as = d;
                at = r + 1.0 - (double)0.001f;
                au = r + 1.0 - (double)0.001f;
                direction = Direction.SOUTH;
                bl9 = bl5;
            } else if (aq == 2) {
                v = o;
                x = n;
                ar = d + (double)0.001f;
                as = d + (double)0.001f;
                at = r + 1.0;
                au = r;
                direction = Direction.WEST;
                bl9 = bl6;
            } else {
                v = q;
                x = p;
                ar = d + 1.0 - (double)0.001f;
                as = d + 1.0 - (double)0.001f;
                at = r;
                au = r + 1.0;
                direction = Direction.EAST;
                bl9 = bl7;
            }
            if (!bl9 || LiquidBlockRenderer.isFaceOccluded(blockAndBiomeGetter, blockPos, direction, Math.max(v, x))) continue;
            bl82 = true;
            BlockPos blockPos2 = blockPos.relative(direction);
            TextureAtlasSprite textureAtlasSprite2 = textureAtlasSprites[1];
            if (!bl && ((block = blockAndBiomeGetter.getBlockState(blockPos2).getBlock()) == Blocks.GLASS || block instanceof StainedGlassBlock)) {
                textureAtlasSprite2 = this.waterOverlay;
            }
            float av = textureAtlasSprite2.getU(0.0);
            float aw = textureAtlasSprite2.getU(8.0);
            aj = textureAtlasSprite2.getV((1.0f - v) * 16.0f * 0.5f);
            ak = textureAtlasSprite2.getV((1.0f - x) * 16.0f * 0.5f);
            al = textureAtlasSprite2.getV(8.0);
            int ax = this.getLightColor(blockAndBiomeGetter, blockPos2);
            int ay = ax >> 16 & 0xFFFF;
            int az = ax & 0xFFFF;
            float ba = aq < 2 ? 0.8f : 0.6f;
            float bb = 1.0f * ba * f;
            float bc = 1.0f * ba * g;
            float bd = 1.0f * ba * h;
            bufferBuilder.vertex(ar, e + (double)v, at).color(bb, bc, bd, 1.0f).uv(av, aj).uv2(ay, az).endVertex();
            bufferBuilder.vertex(as, e + (double)x, au).color(bb, bc, bd, 1.0f).uv(aw, ak).uv2(ay, az).endVertex();
            bufferBuilder.vertex(as, e + 0.0, au).color(bb, bc, bd, 1.0f).uv(aw, al).uv2(ay, az).endVertex();
            bufferBuilder.vertex(ar, e + 0.0, at).color(bb, bc, bd, 1.0f).uv(av, al).uv2(ay, az).endVertex();
            if (textureAtlasSprite2 == this.waterOverlay) continue;
            bufferBuilder.vertex(ar, e + 0.0, at).color(bb, bc, bd, 1.0f).uv(av, al).uv2(ay, az).endVertex();
            bufferBuilder.vertex(as, e + 0.0, au).color(bb, bc, bd, 1.0f).uv(aw, al).uv2(ay, az).endVertex();
            bufferBuilder.vertex(as, e + (double)x, au).color(bb, bc, bd, 1.0f).uv(aw, ak).uv2(ay, az).endVertex();
            bufferBuilder.vertex(ar, e + (double)v, at).color(bb, bc, bd, 1.0f).uv(av, aj).uv2(ay, az).endVertex();
        }
        return bl82;
    }

    private int getLightColor(BlockAndBiomeGetter blockAndBiomeGetter, BlockPos blockPos) {
        int i = blockAndBiomeGetter.getLightColor(blockPos);
        int j = blockAndBiomeGetter.getLightColor(blockPos.above());
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

