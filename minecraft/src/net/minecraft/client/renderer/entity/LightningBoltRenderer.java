package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.global.LightningBolt;

@Environment(EnvType.CLIENT)
public class LightningBoltRenderer extends EntityRenderer<LightningBolt> {
	public LightningBoltRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
	}

	public void render(LightningBolt lightningBolt, double d, double e, double f, float g, float h) {
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		GlStateManager.disableTexture();
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
		double[] ds = new double[8];
		double[] es = new double[8];
		double i = 0.0;
		double j = 0.0;
		Random random = new Random(lightningBolt.seed);

		for (int k = 7; k >= 0; k--) {
			ds[k] = i;
			es[k] = j;
			i += (double)(random.nextInt(11) - 5);
			j += (double)(random.nextInt(11) - 5);
		}

		for (int l = 0; l < 4; l++) {
			Random random2 = new Random(lightningBolt.seed);

			for (int m = 0; m < 3; m++) {
				int n = 7;
				int o = 0;
				if (m > 0) {
					n = 7 - m;
				}

				if (m > 0) {
					o = n - 2;
				}

				double p = ds[n] - i;
				double q = es[n] - j;

				for (int r = n; r >= o; r--) {
					double s = p;
					double t = q;
					if (m == 0) {
						p += (double)(random2.nextInt(11) - 5);
						q += (double)(random2.nextInt(11) - 5);
					} else {
						p += (double)(random2.nextInt(31) - 15);
						q += (double)(random2.nextInt(31) - 15);
					}

					bufferBuilder.begin(5, DefaultVertexFormat.POSITION_COLOR);
					float u = 0.5F;
					float v = 0.45F;
					float w = 0.45F;
					float x = 0.5F;
					double y = 0.1 + (double)l * 0.2;
					if (m == 0) {
						y *= (double)r * 0.1 + 1.0;
					}

					double z = 0.1 + (double)l * 0.2;
					if (m == 0) {
						z *= (double)(r - 1) * 0.1 + 1.0;
					}

					for (int aa = 0; aa < 5; aa++) {
						double ab = d - y;
						double ac = f - y;
						if (aa == 1 || aa == 2) {
							ab += y * 2.0;
						}

						if (aa == 2 || aa == 3) {
							ac += y * 2.0;
						}

						double ad = d - z;
						double ae = f - z;
						if (aa == 1 || aa == 2) {
							ad += z * 2.0;
						}

						if (aa == 2 || aa == 3) {
							ae += z * 2.0;
						}

						bufferBuilder.vertex(ad + p, e + (double)(r * 16), ae + q).color(0.45F, 0.45F, 0.5F, 0.3F).endVertex();
						bufferBuilder.vertex(ab + s, e + (double)((r + 1) * 16), ac + t).color(0.45F, 0.45F, 0.5F, 0.3F).endVertex();
					}

					tesselator.end();
				}
			}
		}

		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.enableTexture();
	}

	@Nullable
	protected ResourceLocation getTextureLocation(LightningBolt lightningBolt) {
		return null;
	}
}
