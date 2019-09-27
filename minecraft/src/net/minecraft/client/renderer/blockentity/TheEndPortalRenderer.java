package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;

@Environment(EnvType.CLIENT)
public class TheEndPortalRenderer<T extends TheEndPortalBlockEntity> extends BlockEntityRenderer<T> {
	public static final ResourceLocation END_SKY_LOCATION = new ResourceLocation("textures/environment/end_sky.png");
	public static final ResourceLocation END_PORTAL_LOCATION = new ResourceLocation("textures/entity/end_portal.png");
	private static final Random RANDOM = new Random(31100L);

	public TheEndPortalRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
		super(blockEntityRenderDispatcher);
	}

	public void render(T theEndPortalBlockEntity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		RANDOM.setSeed(31100L);
		double h = d * d + e * e + f * f;
		int j = this.getPasses(h);
		float k = this.getOffset();
		this.renderCube(theEndPortalBlockEntity, d, e, f, k, 0.15F, multiBufferSource.getBuffer(RenderType.PORTAL(1)));

		for (int l = 1; l < j; l++) {
			this.renderCube(theEndPortalBlockEntity, d, e, f, k, 2.0F / (float)(18 - l), multiBufferSource.getBuffer(RenderType.PORTAL(l + 1)));
		}
	}

	private void renderCube(T theEndPortalBlockEntity, double d, double e, double f, float g, float h, VertexConsumer vertexConsumer) {
		float i = (RANDOM.nextFloat() * 0.5F + 0.1F) * h;
		float j = (RANDOM.nextFloat() * 0.5F + 0.4F) * h;
		float k = (RANDOM.nextFloat() * 0.5F + 0.5F) * h;
		this.renderFace(theEndPortalBlockEntity, vertexConsumer, Direction.SOUTH, d, d + 1.0, e, e + 1.0, f + 1.0, f + 1.0, f + 1.0, f + 1.0, i, j, k);
		this.renderFace(theEndPortalBlockEntity, vertexConsumer, Direction.NORTH, d, d + 1.0, e + 1.0, e, f, f, f, f, i, j, k);
		this.renderFace(theEndPortalBlockEntity, vertexConsumer, Direction.EAST, d + 1.0, d + 1.0, e + 1.0, e, f, f + 1.0, f + 1.0, f, i, j, k);
		this.renderFace(theEndPortalBlockEntity, vertexConsumer, Direction.WEST, d, d, e, e + 1.0, f, f + 1.0, f + 1.0, f, i, j, k);
		this.renderFace(theEndPortalBlockEntity, vertexConsumer, Direction.DOWN, d, d + 1.0, e, e, f, f, f + 1.0, f + 1.0, i, j, k);
		this.renderFace(theEndPortalBlockEntity, vertexConsumer, Direction.UP, d, d + 1.0, e + (double)g, e + (double)g, f + 1.0, f + 1.0, f, f, i, j, k);
	}

	private void renderFace(
		T theEndPortalBlockEntity,
		VertexConsumer vertexConsumer,
		Direction direction,
		double d,
		double e,
		double f,
		double g,
		double h,
		double i,
		double j,
		double k,
		float l,
		float m,
		float n
	) {
		if (theEndPortalBlockEntity.shouldRenderFace(direction)) {
			vertexConsumer.vertex(d, f, h).color(l, m, n, 1.0F).endVertex();
			vertexConsumer.vertex(e, f, i).color(l, m, n, 1.0F).endVertex();
			vertexConsumer.vertex(e, g, j).color(l, m, n, 1.0F).endVertex();
			vertexConsumer.vertex(d, g, k).color(l, m, n, 1.0F).endVertex();
		}
	}

	protected int getPasses(double d) {
		int i;
		if (d > 36864.0) {
			i = 1;
		} else if (d > 25600.0) {
			i = 3;
		} else if (d > 16384.0) {
			i = 5;
		} else if (d > 9216.0) {
			i = 7;
		} else if (d > 4096.0) {
			i = 9;
		} else if (d > 1024.0) {
			i = 11;
		} else if (d > 576.0) {
			i = 13;
		} else if (d > 256.0) {
			i = 14;
		} else {
			i = 15;
		}

		return i;
	}

	protected float getOffset() {
		return 0.75F;
	}
}
