package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class WorldBorderRenderer {
	private static final ResourceLocation FORCEFIELD_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/forcefield.png");

	public void render(WorldBorder worldBorder, Vec3 vec3, double d, double e) {
		double f = worldBorder.getMinX();
		double g = worldBorder.getMaxX();
		double h = worldBorder.getMinZ();
		double i = worldBorder.getMaxZ();
		if (!(vec3.x < g - d) || !(vec3.x > f + d) || !(vec3.z < i - d) || !(vec3.z > h + d)) {
			double j = 1.0 - worldBorder.getDistanceToBorder(vec3.x, vec3.z) / d;
			j = Math.pow(j, 4.0);
			j = Mth.clamp(j, 0.0, 1.0);
			double k = vec3.x;
			double l = vec3.z;
			float m = (float)e;
			RenderSystem.enableBlend();
			RenderSystem.enableDepthTest();
			RenderSystem.blendFuncSeparate(
				GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
			);
			RenderSystem.setShaderTexture(0, FORCEFIELD_LOCATION);
			RenderSystem.depthMask(Minecraft.useShaderTransparency());
			int n = worldBorder.getStatus().getColor();
			float o = (float)ARGB.red(n) / 255.0F;
			float p = (float)ARGB.green(n) / 255.0F;
			float q = (float)ARGB.blue(n) / 255.0F;
			RenderSystem.setShaderColor(o, p, q, (float)j);
			RenderSystem.setShader(CoreShaders.POSITION_TEX);
			RenderSystem.polygonOffset(-3.0F, -3.0F);
			RenderSystem.enablePolygonOffset();
			RenderSystem.disableCull();
			float r = (float)(Util.getMillis() % 3000L) / 3000.0F;
			float s = (float)(-Mth.frac(vec3.y * 0.5));
			float t = s + m;
			BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
			double u = Math.max((double)Mth.floor(l - d), h);
			double v = Math.min((double)Mth.ceil(l + d), i);
			float w = (float)(Mth.floor(u) & 1) * 0.5F;
			if (k > g - d) {
				float x = w;

				for (double y = u; y < v; x += 0.5F) {
					double z = Math.min(1.0, v - y);
					float aa = (float)z * 0.5F;
					bufferBuilder.addVertex((float)(g - k), -m, (float)(y - l)).setUv(r - x, r + t);
					bufferBuilder.addVertex((float)(g - k), -m, (float)(y + z - l)).setUv(r - (aa + x), r + t);
					bufferBuilder.addVertex((float)(g - k), m, (float)(y + z - l)).setUv(r - (aa + x), r + s);
					bufferBuilder.addVertex((float)(g - k), m, (float)(y - l)).setUv(r - x, r + s);
					y++;
				}
			}

			if (k < f + d) {
				float x = w;

				for (double y = u; y < v; x += 0.5F) {
					double z = Math.min(1.0, v - y);
					float aa = (float)z * 0.5F;
					bufferBuilder.addVertex((float)(f - k), -m, (float)(y - l)).setUv(r + x, r + t);
					bufferBuilder.addVertex((float)(f - k), -m, (float)(y + z - l)).setUv(r + aa + x, r + t);
					bufferBuilder.addVertex((float)(f - k), m, (float)(y + z - l)).setUv(r + aa + x, r + s);
					bufferBuilder.addVertex((float)(f - k), m, (float)(y - l)).setUv(r + x, r + s);
					y++;
				}
			}

			u = Math.max((double)Mth.floor(k - d), f);
			v = Math.min((double)Mth.ceil(k + d), g);
			w = (float)(Mth.floor(u) & 1) * 0.5F;
			if (l > i - d) {
				float x = w;

				for (double y = u; y < v; x += 0.5F) {
					double z = Math.min(1.0, v - y);
					float aa = (float)z * 0.5F;
					bufferBuilder.addVertex((float)(y - k), -m, (float)(i - l)).setUv(r + x, r + t);
					bufferBuilder.addVertex((float)(y + z - k), -m, (float)(i - l)).setUv(r + aa + x, r + t);
					bufferBuilder.addVertex((float)(y + z - k), m, (float)(i - l)).setUv(r + aa + x, r + s);
					bufferBuilder.addVertex((float)(y - k), m, (float)(i - l)).setUv(r + x, r + s);
					y++;
				}
			}

			if (l < h + d) {
				float x = w;

				for (double y = u; y < v; x += 0.5F) {
					double z = Math.min(1.0, v - y);
					float aa = (float)z * 0.5F;
					bufferBuilder.addVertex((float)(y - k), -m, (float)(h - l)).setUv(r - x, r + t);
					bufferBuilder.addVertex((float)(y + z - k), -m, (float)(h - l)).setUv(r - (aa + x), r + t);
					bufferBuilder.addVertex((float)(y + z - k), m, (float)(h - l)).setUv(r - (aa + x), r + s);
					bufferBuilder.addVertex((float)(y - k), m, (float)(h - l)).setUv(r - x, r + s);
					y++;
				}
			}

			MeshData meshData = bufferBuilder.build();
			if (meshData != null) {
				BufferUploader.drawWithShader(meshData);
			}

			RenderSystem.enableCull();
			RenderSystem.polygonOffset(0.0F, 0.0F);
			RenderSystem.disablePolygonOffset();
			RenderSystem.disableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.depthMask(true);
		}
	}
}
