package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.LightningBoltRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LightningBolt;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class LightningBoltRenderer extends EntityRenderer<LightningBolt, LightningBoltRenderState> {
	public LightningBoltRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	public void render(LightningBoltRenderState lightningBoltRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		float[] fs = new float[8];
		float[] gs = new float[8];
		float f = 0.0F;
		float g = 0.0F;
		RandomSource randomSource = RandomSource.create(lightningBoltRenderState.seed);

		for (int j = 7; j >= 0; j--) {
			fs[j] = f;
			gs[j] = g;
			f += (float)(randomSource.nextInt(11) - 5);
			g += (float)(randomSource.nextInt(11) - 5);
		}

		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.lightning());
		Matrix4f matrix4f = poseStack.last().pose();

		for (int k = 0; k < 4; k++) {
			RandomSource randomSource2 = RandomSource.create(lightningBoltRenderState.seed);

			for (int l = 0; l < 3; l++) {
				int m = 7;
				int n = 0;
				if (l > 0) {
					m = 7 - l;
				}

				if (l > 0) {
					n = m - 2;
				}

				float h = fs[m] - f;
				float o = gs[m] - g;

				for (int p = m; p >= n; p--) {
					float q = h;
					float r = o;
					if (l == 0) {
						h += (float)(randomSource2.nextInt(11) - 5);
						o += (float)(randomSource2.nextInt(11) - 5);
					} else {
						h += (float)(randomSource2.nextInt(31) - 15);
						o += (float)(randomSource2.nextInt(31) - 15);
					}

					float s = 0.5F;
					float t = 0.45F;
					float u = 0.45F;
					float v = 0.5F;
					float w = 0.1F + (float)k * 0.2F;
					if (l == 0) {
						w *= (float)p * 0.1F + 1.0F;
					}

					float x = 0.1F + (float)k * 0.2F;
					if (l == 0) {
						x *= ((float)p - 1.0F) * 0.1F + 1.0F;
					}

					quad(matrix4f, vertexConsumer, h, o, p, q, r, 0.45F, 0.45F, 0.5F, w, x, false, false, true, false);
					quad(matrix4f, vertexConsumer, h, o, p, q, r, 0.45F, 0.45F, 0.5F, w, x, true, false, true, true);
					quad(matrix4f, vertexConsumer, h, o, p, q, r, 0.45F, 0.45F, 0.5F, w, x, true, true, false, true);
					quad(matrix4f, vertexConsumer, h, o, p, q, r, 0.45F, 0.45F, 0.5F, w, x, false, true, false, false);
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
		vertexConsumer.addVertex(matrix4f, f + (bl ? o : -o), (float)(i * 16), g + (bl2 ? o : -o)).setColor(k, l, m, 0.3F);
		vertexConsumer.addVertex(matrix4f, h + (bl ? n : -n), (float)((i + 1) * 16), j + (bl2 ? n : -n)).setColor(k, l, m, 0.3F);
		vertexConsumer.addVertex(matrix4f, h + (bl3 ? n : -n), (float)((i + 1) * 16), j + (bl4 ? n : -n)).setColor(k, l, m, 0.3F);
		vertexConsumer.addVertex(matrix4f, f + (bl3 ? o : -o), (float)(i * 16), g + (bl4 ? o : -o)).setColor(k, l, m, 0.3F);
	}

	public ResourceLocation getTextureLocation(LightningBoltRenderState lightningBoltRenderState) {
		return TextureAtlas.LOCATION_BLOCKS;
	}

	public LightningBoltRenderState createRenderState() {
		return new LightningBoltRenderState();
	}

	public void extractRenderState(LightningBolt lightningBolt, LightningBoltRenderState lightningBoltRenderState, float f) {
		super.extractRenderState(lightningBolt, lightningBoltRenderState, f);
		lightningBoltRenderState.seed = lightningBolt.seed;
	}

	protected boolean affectedByCulling(LightningBolt lightningBolt) {
		return false;
	}
}
