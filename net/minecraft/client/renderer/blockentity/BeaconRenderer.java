/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

@Environment(value=EnvType.CLIENT)
public class BeaconRenderer
extends BlockEntityRenderer<BeaconBlockEntity> {
    private static final ResourceLocation BEAM_LOCATION = new ResourceLocation("textures/entity/beacon_beam.png");

    @Override
    public void render(BeaconBlockEntity beaconBlockEntity, double d, double e, double f, float g, int i, RenderType renderType) {
        this.renderBeaconBeam(d, e, f, g, beaconBlockEntity.getBeamSections(), beaconBlockEntity.getLevel().getGameTime());
    }

    private void renderBeaconBeam(double d, double e, double f, double g, List<BeaconBlockEntity.BeaconBeamSection> list, long l) {
        RenderSystem.defaultAlphaFunc();
        this.bindTexture(BEAM_LOCATION);
        RenderSystem.disableFog();
        int i = 0;
        for (int j = 0; j < list.size(); ++j) {
            BeaconBlockEntity.BeaconBeamSection beaconBeamSection = list.get(j);
            BeaconRenderer.renderBeaconBeam(d, e, f, g, l, i, j == list.size() - 1 ? 1024 : beaconBeamSection.getHeight(), beaconBeamSection.getColor());
            i += beaconBeamSection.getHeight();
        }
        RenderSystem.enableFog();
    }

    private static void renderBeaconBeam(double d, double e, double f, double g, long l, int i, int j, float[] fs) {
        BeaconRenderer.renderBeaconBeam(d, e, f, g, 1.0, l, i, j, fs, 0.2, 0.25);
    }

    public static void renderBeaconBeam(double d, double e, double f, double g, double h, long l, int i, int j, float[] fs, double k, double m) {
        int n = i + j;
        RenderSystem.texParameter(3553, 10242, 10497);
        RenderSystem.texParameter(3553, 10243, 10497);
        RenderSystem.disableLighting();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.pushMatrix();
        RenderSystem.translated(d + 0.5, e, f + 0.5);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        double o = (double)Math.floorMod(l, 40L) + g;
        double p = j < 0 ? o : -o;
        double q = Mth.frac(p * 0.2 - (double)Mth.floor(p * 0.1));
        float r = fs[0];
        float s = fs[1];
        float t = fs[2];
        RenderSystem.pushMatrix();
        RenderSystem.rotated(o * 2.25 - 45.0, 0.0, 1.0, 0.0);
        double u = 0.0;
        double v = k;
        double w = k;
        double x = 0.0;
        double y = -k;
        double z = 0.0;
        double aa = 0.0;
        double ab = -k;
        double ac = 0.0;
        double ad = 1.0;
        double ae = -1.0 + q;
        double af = (double)j * h * (0.5 / k) + ae;
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
        BeaconRenderer.renderPart(bufferBuilder, r, s, t, 1.0f, i, n, 0.0, v, w, 0.0, y, 0.0, 0.0, ab, 0.0, 1.0, af, ae);
        tesselator.end();
        RenderSystem.popMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        u = -m;
        v = -m;
        w = m;
        x = -m;
        y = -m;
        z = m;
        aa = m;
        ab = m;
        ac = 0.0;
        ad = 1.0;
        ae = -1.0 + q;
        af = (double)j * h + ae;
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
        BeaconRenderer.renderPart(bufferBuilder, r, s, t, 0.125f, i, n, u, v, w, x, y, z, aa, ab, 0.0, 1.0, af, ae);
        tesselator.end();
        RenderSystem.popMatrix();
        RenderSystem.enableLighting();
        RenderSystem.enableTexture();
        RenderSystem.depthMask(true);
    }

    private static void renderPart(BufferBuilder bufferBuilder, float f, float g, float h, float i, int j, int k, double d, double e, double l, double m, double n, double o, double p, double q, double r, double s, double t, double u) {
        BeaconRenderer.renderQuad(bufferBuilder, f, g, h, i, j, k, d, e, l, m, r, s, t, u);
        BeaconRenderer.renderQuad(bufferBuilder, f, g, h, i, j, k, p, q, n, o, r, s, t, u);
        BeaconRenderer.renderQuad(bufferBuilder, f, g, h, i, j, k, l, m, p, q, r, s, t, u);
        BeaconRenderer.renderQuad(bufferBuilder, f, g, h, i, j, k, n, o, d, e, r, s, t, u);
    }

    private static void renderQuad(BufferBuilder bufferBuilder, float f, float g, float h, float i, int j, int k, double d, double e, double l, double m, double n, double o, double p, double q) {
        bufferBuilder.vertex(d, k, e).uv(o, p).color(f, g, h, i).endVertex();
        bufferBuilder.vertex(d, j, e).uv(o, q).color(f, g, h, i).endVertex();
        bufferBuilder.vertex(l, j, m).uv(n, q).color(f, g, h, i).endVertex();
        bufferBuilder.vertex(l, k, m).uv(n, p).color(f, g, h, i).endVertex();
    }

    @Override
    public boolean shouldRenderOffScreen(BeaconBlockEntity beaconBlockEntity) {
        return true;
    }
}

