package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

@Environment(EnvType.CLIENT)
public class BeaconRenderer extends BlockEntityRenderer<BeaconBlockEntity> {
	public static final ResourceLocation BEAM_LOCATION = new ResourceLocation("textures/entity/beacon_beam.png");

	public BeaconRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
		super(blockEntityRenderDispatcher);
	}

	public void render(BeaconBlockEntity beaconBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		long l = beaconBlockEntity.getLevel().getGameTime();
		List<BeaconBlockEntity.BeaconBeamSection> list = beaconBlockEntity.getBeamSections();
		int k = 0;

		for (int m = 0; m < list.size(); m++) {
			BeaconBlockEntity.BeaconBeamSection beaconBeamSection = (BeaconBlockEntity.BeaconBeamSection)list.get(m);
			renderBeaconBeam(poseStack, multiBufferSource, f, l, k, m == list.size() - 1 ? 1024 : beaconBeamSection.getHeight(), beaconBeamSection.getColor());
			k += beaconBeamSection.getHeight();
		}
	}

	private static void renderBeaconBeam(PoseStack poseStack, MultiBufferSource multiBufferSource, float f, long l, int i, int j, float[] fs) {
		renderBeaconBeam(poseStack, multiBufferSource, BEAM_LOCATION, f, 1.0F, l, i, j, fs, 0.2F, 0.25F);
	}

	public static void renderBeaconBeam(
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		ResourceLocation resourceLocation,
		float f,
		float g,
		long l,
		int i,
		int j,
		float[] fs,
		float h,
		float k
	) {
		int m = i + j;
		poseStack.pushPose();
		poseStack.translate(0.5, 0.0, 0.5);
		float n = (float)Math.floorMod(l, 40L) + f;
		float o = j < 0 ? n : -n;
		float p = Mth.frac(o * 0.2F - (float)Mth.floor(o * 0.1F));
		float q = fs[0];
		float r = fs[1];
		float s = fs[2];
		poseStack.pushPose();
		poseStack.mulPose(Vector3f.YP.rotationDegrees(n * 2.25F - 45.0F));
		float t = 0.0F;
		float w = 0.0F;
		float x = -h;
		float y = 0.0F;
		float z = 0.0F;
		float aa = -h;
		float ab = 0.0F;
		float ac = 1.0F;
		float ad = -1.0F + p;
		float ae = (float)j * g * (0.5F / h) + ad;
		renderPart(
			poseStack,
			multiBufferSource.getBuffer(RenderType.beaconBeam(resourceLocation, false)),
			q,
			r,
			s,
			1.0F,
			i,
			m,
			0.0F,
			h,
			h,
			0.0F,
			x,
			0.0F,
			0.0F,
			aa,
			0.0F,
			1.0F,
			ae,
			ad
		);
		poseStack.popPose();
		t = -k;
		float u = -k;
		w = -k;
		x = -k;
		ab = 0.0F;
		ac = 1.0F;
		ad = -1.0F + p;
		ae = (float)j * g + ad;
		renderPart(
			poseStack, multiBufferSource.getBuffer(RenderType.beaconBeam(resourceLocation, true)), q, r, s, 0.125F, i, m, t, u, k, w, x, k, k, k, 0.0F, 1.0F, ae, ad
		);
		poseStack.popPose();
	}

	private static void renderPart(
		PoseStack poseStack,
		VertexConsumer vertexConsumer,
		float f,
		float g,
		float h,
		float i,
		int j,
		int k,
		float l,
		float m,
		float n,
		float o,
		float p,
		float q,
		float r,
		float s,
		float t,
		float u,
		float v,
		float w
	) {
		PoseStack.Pose pose = poseStack.last();
		Matrix4f matrix4f = pose.pose();
		Matrix3f matrix3f = pose.normal();
		renderQuad(matrix4f, matrix3f, vertexConsumer, f, g, h, i, j, k, l, m, n, o, t, u, v, w);
		renderQuad(matrix4f, matrix3f, vertexConsumer, f, g, h, i, j, k, r, s, p, q, t, u, v, w);
		renderQuad(matrix4f, matrix3f, vertexConsumer, f, g, h, i, j, k, n, o, r, s, t, u, v, w);
		renderQuad(matrix4f, matrix3f, vertexConsumer, f, g, h, i, j, k, p, q, l, m, t, u, v, w);
	}

	private static void renderQuad(
		Matrix4f matrix4f,
		Matrix3f matrix3f,
		VertexConsumer vertexConsumer,
		float f,
		float g,
		float h,
		float i,
		int j,
		int k,
		float l,
		float m,
		float n,
		float o,
		float p,
		float q,
		float r,
		float s
	) {
		addVertex(matrix4f, matrix3f, vertexConsumer, f, g, h, i, k, l, m, q, r);
		addVertex(matrix4f, matrix3f, vertexConsumer, f, g, h, i, j, l, m, q, s);
		addVertex(matrix4f, matrix3f, vertexConsumer, f, g, h, i, j, n, o, p, s);
		addVertex(matrix4f, matrix3f, vertexConsumer, f, g, h, i, k, n, o, p, r);
	}

	private static void addVertex(
		Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer vertexConsumer, float f, float g, float h, float i, int j, float k, float l, float m, float n
	) {
		vertexConsumer.vertex(matrix4f, k, (float)j, l)
			.color(f, g, h, i)
			.uv(m, n)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(15728880)
			.normal(matrix3f, 0.0F, 1.0F, 0.0F)
			.endVertex();
	}

	public boolean shouldRenderOffScreen(BeaconBlockEntity beaconBlockEntity) {
		return true;
	}
}
