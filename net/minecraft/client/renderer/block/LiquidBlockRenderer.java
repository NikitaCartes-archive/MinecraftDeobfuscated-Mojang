/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.VertexConsumer;
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

    public boolean tesselate(BlockAndBiomeGetter blockAndBiomeGetter, BlockPos blockPos, VertexConsumer vertexConsumer, FluidState fluidState) {
        float aj;
        float ai;
        float ah;
        float aa;
        float z;
        float y;
        float x;
        float w;
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
        double d = blockPos.getX() & 0xF;
        double e = blockPos.getY() & 0xF;
        double r = blockPos.getZ() & 0xF;
        float s = 0.001f;
        if (bl2 && !LiquidBlockRenderer.isFaceOccluded(blockAndBiomeGetter, blockPos, Direction.UP, Math.min(Math.min(n, o), Math.min(p, q)))) {
            float ae;
            float ad;
            float ac;
            float ab;
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
            float af = (t + v + x + z) / 4.0f;
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
            ah = 1.0f * f;
            ai = 1.0f * g;
            aj = 1.0f * h;
            this.vertex(vertexConsumer, d + 0.0, e + (double)n, r + 0.0, ah, ai, aj, t, u, ag);
            this.vertex(vertexConsumer, d + 0.0, e + (double)o, r + 1.0, ah, ai, aj, v, w, ag);
            this.vertex(vertexConsumer, d + 1.0, e + (double)p, r + 1.0, ah, ai, aj, x, y, ag);
            this.vertex(vertexConsumer, d + 1.0, e + (double)q, r + 0.0, ah, ai, aj, z, aa, ag);
            if (fluidState.shouldRenderBackwardUpFace(blockAndBiomeGetter, blockPos.above())) {
                this.vertex(vertexConsumer, d + 0.0, e + (double)n, r + 0.0, ah, ai, aj, t, u, ag);
                this.vertex(vertexConsumer, d + 1.0, e + (double)q, r + 0.0, ah, ai, aj, z, aa, ag);
                this.vertex(vertexConsumer, d + 1.0, e + (double)p, r + 1.0, ah, ai, aj, x, y, ag);
                this.vertex(vertexConsumer, d + 0.0, e + (double)o, r + 1.0, ah, ai, aj, v, w, ag);
            }
        }
        if (bl3) {
            t = textureAtlasSprites[0].getU0();
            v = textureAtlasSprites[0].getU1();
            x = textureAtlasSprites[0].getV0();
            z = textureAtlasSprites[0].getV1();
            int ak = this.getLightColor(blockAndBiomeGetter, blockPos.below());
            w = 0.5f * f;
            y = 0.5f * g;
            aa = 0.5f * h;
            this.vertex(vertexConsumer, d, e, r + 1.0, w, y, aa, t, z, ak);
            this.vertex(vertexConsumer, d, e, r, w, y, aa, t, x, ak);
            this.vertex(vertexConsumer, d + 1.0, e, r, w, y, aa, v, x, ak);
            this.vertex(vertexConsumer, d + 1.0, e, r + 1.0, w, y, aa, v, z, ak);
            bl82 = true;
        }
        for (int al = 0; al < 4; ++al) {
            Block block;
            boolean bl9;
            Direction direction;
            double ap;
            double ao;
            double an;
            double am;
            if (al == 0) {
                v = n;
                x = q;
                am = d;
                an = d + 1.0;
                ao = r + (double)0.001f;
                ap = r + (double)0.001f;
                direction = Direction.NORTH;
                bl9 = bl4;
            } else if (al == 1) {
                v = p;
                x = o;
                am = d + 1.0;
                an = d;
                ao = r + 1.0 - (double)0.001f;
                ap = r + 1.0 - (double)0.001f;
                direction = Direction.SOUTH;
                bl9 = bl5;
            } else if (al == 2) {
                v = o;
                x = n;
                am = d + (double)0.001f;
                an = d + (double)0.001f;
                ao = r + 1.0;
                ap = r;
                direction = Direction.WEST;
                bl9 = bl6;
            } else {
                v = q;
                x = p;
                am = d + 1.0 - (double)0.001f;
                an = d + 1.0 - (double)0.001f;
                ao = r;
                ap = r + 1.0;
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
            ah = textureAtlasSprite2.getU(0.0);
            ai = textureAtlasSprite2.getU(8.0);
            aj = textureAtlasSprite2.getV((1.0f - v) * 16.0f * 0.5f);
            float aq = textureAtlasSprite2.getV((1.0f - x) * 16.0f * 0.5f);
            float ar = textureAtlasSprite2.getV(8.0);
            int as = this.getLightColor(blockAndBiomeGetter, blockPos2);
            float at = al < 2 ? 0.8f : 0.6f;
            float au = 1.0f * at * f;
            float av = 1.0f * at * g;
            float aw = 1.0f * at * h;
            this.vertex(vertexConsumer, am, e + (double)v, ao, au, av, aw, ah, aj, as);
            this.vertex(vertexConsumer, an, e + (double)x, ap, au, av, aw, ai, aq, as);
            this.vertex(vertexConsumer, an, e + 0.0, ap, au, av, aw, ai, ar, as);
            this.vertex(vertexConsumer, am, e + 0.0, ao, au, av, aw, ah, ar, as);
            if (textureAtlasSprite2 == this.waterOverlay) continue;
            this.vertex(vertexConsumer, am, e + 0.0, ao, au, av, aw, ah, ar, as);
            this.vertex(vertexConsumer, an, e + 0.0, ap, au, av, aw, ai, ar, as);
            this.vertex(vertexConsumer, an, e + (double)x, ap, au, av, aw, ai, aq, as);
            this.vertex(vertexConsumer, am, e + (double)v, ao, au, av, aw, ah, aj, as);
        }
        return bl82;
    }

    private void vertex(VertexConsumer vertexConsumer, double d, double e, double f, float g, float h, float i, float j, float k, int l) {
        vertexConsumer.vertex(d, e, f).color(g, h, i, 1.0f).uv(j, k).uv2(l).normal(0.0f, 1.0f, 0.0f).endVertex();
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

