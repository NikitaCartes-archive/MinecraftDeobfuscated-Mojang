package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

@Environment(EnvType.CLIENT)
public class BeaconRenderer extends BlockEntityRenderer<BeaconBlockEntity> {
	private static final ResourceLocation BEAM_LOCATION = new ResourceLocation("textures/entity/beacon_beam.png");

	public void render(BeaconBlockEntity beaconBlockEntity, double d, double e, double f, float g, int i) {
		this.renderBeaconBeam(d, e, f, (double)g, beaconBlockEntity.getBeamSections(), beaconBlockEntity.getLevel().getGameTime());
	}

	private void renderBeaconBeam(double d, double e, double f, double g, List<BeaconBlockEntity.BeaconBeamSection> list, long l) {
		GlStateManager.alphaFunc(516, 0.1F);
		this.bindTexture(BEAM_LOCATION);
		GlStateManager.disableFog();
		int i = 0;

		for (int j = 0; j < list.size(); j++) {
			BeaconBlockEntity.BeaconBeamSection beaconBeamSection = (BeaconBlockEntity.BeaconBeamSection)list.get(j);
			renderBeaconBeam(d, e, f, g, l, i, j == list.size() - 1 ? 1024 : beaconBeamSection.getHeight(), beaconBeamSection.getColor());
			i += beaconBeamSection.getHeight();
		}

		GlStateManager.enableFog();
	}

	private static void renderBeaconBeam(double d, double e, double f, double g, long l, int i, int j, float[] fs) {
		renderBeaconBeam(d, e, f, g, 1.0, l, i, j, fs, 0.2, 0.25);
	}

	public static void renderBeaconBeam(double d, double e, double f, double g, double h, long l, int i, int j, float[] fs, double k, double m) {
		int n = i + j;
		GlStateManager.texParameter(3553, 10242, 10497);
		GlStateManager.texParameter(3553, 10243, 10497);
		GlStateManager.disableLighting();
		GlStateManager.disableCull();
		GlStateManager.disableBlend();
		GlStateManager.depthMask(true);
		GlStateManager.blendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
		);
		GlStateManager.pushMatrix();
		GlStateManager.translated(d + 0.5, e, f + 0.5);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		double o = (double)Math.floorMod(l, 40L) + g;
		double p = j < 0 ? o : -o;
		double q = Mth.frac(p * 0.2 - (double)Mth.floor(p * 0.1));
		float r = fs[0];
		float s = fs[1];
		float t = fs[2];
		GlStateManager.pushMatrix();
		GlStateManager.rotated(o * 2.25 - 45.0, 0.0, 1.0, 0.0);
		double u = 0.0;
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
		bufferBuilder.vertex(0.0, (double)n, k).uv(1.0, af).color(r, s, t, 1.0F).endVertex();
		bufferBuilder.vertex(0.0, (double)i, k).uv(1.0, ae).color(r, s, t, 1.0F).endVertex();
		bufferBuilder.vertex(k, (double)i, 0.0).uv(0.0, ae).color(r, s, t, 1.0F).endVertex();
		bufferBuilder.vertex(k, (double)n, 0.0).uv(0.0, af).color(r, s, t, 1.0F).endVertex();
		bufferBuilder.vertex(0.0, (double)n, ab).uv(1.0, af).color(r, s, t, 1.0F).endVertex();
		bufferBuilder.vertex(0.0, (double)i, ab).uv(1.0, ae).color(r, s, t, 1.0F).endVertex();
		bufferBuilder.vertex(y, (double)i, 0.0).uv(0.0, ae).color(r, s, t, 1.0F).endVertex();
		bufferBuilder.vertex(y, (double)n, 0.0).uv(0.0, af).color(r, s, t, 1.0F).endVertex();
		bufferBuilder.vertex(k, (double)n, 0.0).uv(1.0, af).color(r, s, t, 1.0F).endVertex();
		bufferBuilder.vertex(k, (double)i, 0.0).uv(1.0, ae).color(r, s, t, 1.0F).endVertex();
		bufferBuilder.vertex(0.0, (double)i, ab).uv(0.0, ae).color(r, s, t, 1.0F).endVertex();
		bufferBuilder.vertex(0.0, (double)n, ab).uv(0.0, af).color(r, s, t, 1.0F).endVertex();
		bufferBuilder.vertex(y, (double)n, 0.0).uv(1.0, af).color(r, s, t, 1.0F).endVertex();
		bufferBuilder.vertex(y, (double)i, 0.0).uv(1.0, ae).color(r, s, t, 1.0F).endVertex();
		bufferBuilder.vertex(0.0, (double)i, k).uv(0.0, ae).color(r, s, t, 1.0F).endVertex();
		bufferBuilder.vertex(0.0, (double)n, k).uv(0.0, af).color(r, s, t, 1.0F).endVertex();
		tesselator.end();
		GlStateManager.popMatrix();
		GlStateManager.enableBlend();
		GlStateManager.blendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
		);
		GlStateManager.depthMask(false);
		u = -m;
		double v = -m;
		x = -m;
		y = -m;
		ac = 0.0;
		ad = 1.0;
		ae = -1.0 + q;
		af = (double)j * h + ae;
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
		bufferBuilder.vertex(u, (double)n, v).uv(1.0, af).color(r, s, t, 0.125F).endVertex();
		bufferBuilder.vertex(u, (double)i, v).uv(1.0, ae).color(r, s, t, 0.125F).endVertex();
		bufferBuilder.vertex(m, (double)i, x).uv(0.0, ae).color(r, s, t, 0.125F).endVertex();
		bufferBuilder.vertex(m, (double)n, x).uv(0.0, af).color(r, s, t, 0.125F).endVertex();
		bufferBuilder.vertex(m, (double)n, m).uv(1.0, af).color(r, s, t, 0.125F).endVertex();
		bufferBuilder.vertex(m, (double)i, m).uv(1.0, ae).color(r, s, t, 0.125F).endVertex();
		bufferBuilder.vertex(y, (double)i, m).uv(0.0, ae).color(r, s, t, 0.125F).endVertex();
		bufferBuilder.vertex(y, (double)n, m).uv(0.0, af).color(r, s, t, 0.125F).endVertex();
		bufferBuilder.vertex(m, (double)n, x).uv(1.0, af).color(r, s, t, 0.125F).endVertex();
		bufferBuilder.vertex(m, (double)i, x).uv(1.0, ae).color(r, s, t, 0.125F).endVertex();
		bufferBuilder.vertex(m, (double)i, m).uv(0.0, ae).color(r, s, t, 0.125F).endVertex();
		bufferBuilder.vertex(m, (double)n, m).uv(0.0, af).color(r, s, t, 0.125F).endVertex();
		bufferBuilder.vertex(y, (double)n, m).uv(1.0, af).color(r, s, t, 0.125F).endVertex();
		bufferBuilder.vertex(y, (double)i, m).uv(1.0, ae).color(r, s, t, 0.125F).endVertex();
		bufferBuilder.vertex(u, (double)i, v).uv(0.0, ae).color(r, s, t, 0.125F).endVertex();
		bufferBuilder.vertex(u, (double)n, v).uv(0.0, af).color(r, s, t, 0.125F).endVertex();
		tesselator.end();
		GlStateManager.popMatrix();
		GlStateManager.enableLighting();
		GlStateManager.enableTexture();
		GlStateManager.depthMask(true);
	}

	public boolean shouldRenderOffScreen(BeaconBlockEntity beaconBlockEntity) {
		return true;
	}
}
