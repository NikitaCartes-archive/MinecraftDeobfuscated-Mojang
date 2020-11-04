package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LightningBolt;

@Environment(EnvType.CLIENT)
public class LightningBoltRenderer extends EntityRenderer<LightningBolt> {
	public LightningBoltRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	public void render(LightningBolt lightningBolt, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		float[] fs = new float[8];
		float[] gs = new float[8];
		float h = 0.0F;
		float j = 0.0F;
		Random random = new Random(lightningBolt.seed);

		for (int k = 7; k >= 0; k--) {
			fs[k] = h;
			gs[k] = j;
			h += (float)(random.nextInt(11) - 5);
			j += (float)(random.nextInt(11) - 5);
		}

		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lightning());
		Matrix4f matrix4f = poseStack.last().pose();

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

				float p = fs[n] - h;
				float q = gs[n] - j;

				for (int r = n; r >= o; r--) {
					float s = p;
					float t = q;
					if (m == 0) {
						p += (float)(random2.nextInt(11) - 5);
						q += (float)(random2.nextInt(11) - 5);
					} else {
						p += (float)(random2.nextInt(31) - 15);
						q += (float)(random2.nextInt(31) - 15);
					}

					float u = 0.5F;
					float v = 0.45F;
					float w = 0.45F;
					float x = 0.5F;
					float y = 0.1F + (float)l * 0.2F;
					if (m == 0) {
						y = (float)((double)y * ((double)r * 0.1 + 1.0));
					}

					float z = 0.1F + (float)l * 0.2F;
					if (m == 0) {
						z *= (float)(r - 1) * 0.1F + 1.0F;
					}

					quad(matrix4f, vertexConsumer, p, q, r, s, t, 0.45F, 0.45F, 0.5F, y, z, false, false, true, false);
					quad(matrix4f, vertexConsumer, p, q, r, s, t, 0.45F, 0.45F, 0.5F, y, z, true, false, true, true);
					quad(matrix4f, vertexConsumer, p, q, r, s, t, 0.45F, 0.45F, 0.5F, y, z, true, true, false, true);
					quad(matrix4f, vertexConsumer, p, q, r, s, t, 0.45F, 0.45F, 0.5F, y, z, false, true, false, false);
				}
			}
		}
	}

	private static void quad(
		Matrix4f matrix4f,
		VertexConsumer vertexConsumer,
		float f,
		float g,
		int i,
		float h,
		float j,
		float k,
		float l,
		float m,
		float n,
		float o,
		boolean bl,
		boolean bl2,
		boolean bl3,
		boolean bl4
	) {
		vertexConsumer.vertex(matrix4f, f + (bl ? o : -o), (float)(i * 16), g + (bl2 ? o : -o)).color(k, l, m, 0.3F).endVertex();
		vertexConsumer.vertex(matrix4f, h + (bl ? n : -n), (float)((i + 1) * 16), j + (bl2 ? n : -n)).color(k, l, m, 0.3F).endVertex();
		vertexConsumer.vertex(matrix4f, h + (bl3 ? n : -n), (float)((i + 1) * 16), j + (bl4 ? n : -n)).color(k, l, m, 0.3F).endVertex();
		vertexConsumer.vertex(matrix4f, f + (bl3 ? o : -o), (float)(i * 16), g + (bl4 ? o : -o)).color(k, l, m, 0.3F).endVertex();
	}

	public ResourceLocation getTextureLocation(LightningBolt lightningBolt) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
