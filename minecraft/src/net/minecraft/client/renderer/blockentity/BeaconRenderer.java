package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class BeaconRenderer implements BlockEntityRenderer<BeaconBlockEntity> {
	public static final ResourceLocation BEAM_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/beacon_beam.png");
	public static final int MAX_RENDER_Y = 1024;

	public BeaconRenderer(BlockEntityRendererProvider.Context context) {
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

	private static void renderBeaconBeam(PoseStack poseStack, MultiBufferSource multiBufferSource, float f, long l, int i, int j, int k) {
		renderBeaconBeam(poseStack, multiBufferSource, BEAM_LOCATION, f, 1.0F, l, i, j, k, 0.2F, 0.25F);
	}

	public static void renderBeaconBeam(
		PoseStack poseStack, MultiBufferSource multiBufferSource, ResourceLocation resourceLocation, float f, float g, long l, int i, int j, int k, float h, float m
	) {
		int n = i + j;
		poseStack.pushPose();
		poseStack.translate(0.5, 0.0, 0.5);
		float o = (float)Math.floorMod(l, 40) + f;
		float p = j < 0 ? o : -o;
		float q = Mth.frac(p * 0.2F - (float)Mth.floor(p * 0.1F));
		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(o * 2.25F - 45.0F));
		float r = 0.0F;
		float u = 0.0F;
		float v = -h;
		float w = 0.0F;
		float x = 0.0F;
		float y = -h;
		float z = 0.0F;
		float aa = 1.0F;
		float ab = -1.0F + q;
		float ac = (float)j * g * (0.5F / h) + ab;
		renderPart(
			poseStack, multiBufferSource.getBuffer(RenderType.beaconBeam(resourceLocation, false)), k, i, n, 0.0F, h, h, 0.0F, v, 0.0F, 0.0F, y, 0.0F, 1.0F, ac, ab
		);
		poseStack.popPose();
		r = -m;
		float s = -m;
		u = -m;
		v = -m;
		z = 0.0F;
		aa = 1.0F;
		ab = -1.0F + q;
		ac = (float)j * g + ab;
		renderPart(
			poseStack,
			multiBufferSource.getBuffer(RenderType.beaconBeam(resourceLocation, true)),
			FastColor.ARGB32.color(32, k),
			i,
			n,
			r,
			s,
			m,
			u,
			v,
			m,
			m,
			m,
			0.0F,
			1.0F,
			ac,
			ab
		);
		poseStack.popPose();
	}

	private static void renderPart(
		PoseStack poseStack,
		VertexConsumer vertexConsumer,
		int i,
		int j,
		int k,
		float f,
		float g,
		float h,
		float l,
		float m,
		float n,
		float o,
		float p,
		float q,
		float r,
		float s,
		float t
	) {
		PoseStack.Pose pose = poseStack.last();
		renderQuad(pose, vertexConsumer, i, j, k, f, g, h, l, q, r, s, t);
		renderQuad(pose, vertexConsumer, i, j, k, o, p, m, n, q, r, s, t);
		renderQuad(pose, vertexConsumer, i, j, k, h, l, o, p, q, r, s, t);
		renderQuad(pose, vertexConsumer, i, j, k, m, n, f, g, q, r, s, t);
	}

	private static void renderQuad(
		PoseStack.Pose pose, VertexConsumer vertexConsumer, int i, int j, int k, float f, float g, float h, float l, float m, float n, float o, float p
	) {
		addVertex(pose, vertexConsumer, i, k, f, g, n, o);
		addVertex(pose, vertexConsumer, i, j, f, g, n, p);
		addVertex(pose, vertexConsumer, i, j, h, l, m, p);
		addVertex(pose, vertexConsumer, i, k, h, l, m, o);
	}

	private static void addVertex(PoseStack.Pose pose, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
		vertexConsumer.addVertex(pose, f, (float)j, g)
			.setColor(i)
			.setUv(h, k)
			.setOverlay(OverlayTexture.NO_OVERLAY)
			.setLight(15728880)
			.setNormal(pose, 0.0F, 1.0F, 0.0F);
	}

	public boolean shouldRenderOffScreen(BeaconBlockEntity beaconBlockEntity) {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 256;
	}

	public boolean shouldRender(BeaconBlockEntity beaconBlockEntity, Vec3 vec3) {
		return Vec3.atCenterOf(beaconBlockEntity.getBlockPos()).multiply(1.0, 0.0, 1.0).closerThan(vec3.multiply(1.0, 0.0, 1.0), (double)this.getViewDistance());
	}
}
