package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;

@Environment(EnvType.CLIENT)
public class TheEndPortalRenderer<T extends TheEndPortalBlockEntity> implements BlockEntityRenderer<T> {
	public static final ResourceLocation END_SKY_LOCATION = new ResourceLocation("textures/environment/end_sky.png");
	public static final ResourceLocation END_PORTAL_LOCATION = new ResourceLocation("textures/entity/end_portal.png");

	public TheEndPortalRenderer(BlockEntityRendererProvider.Context context) {
	}

	public void render(T theEndPortalBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		float g = this.getOffset();
		Matrix4f matrix4f = poseStack.last().pose();
		this.renderCube(theEndPortalBlockEntity, g, matrix4f, multiBufferSource.getBuffer(this.renderType()));
	}

	private void renderCube(T theEndPortalBlockEntity, float f, Matrix4f matrix4f, VertexConsumer vertexConsumer) {
		this.renderFace(theEndPortalBlockEntity, matrix4f, vertexConsumer, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, Direction.SOUTH);
		this.renderFace(theEndPortalBlockEntity, matrix4f, vertexConsumer, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, Direction.NORTH);
		this.renderFace(theEndPortalBlockEntity, matrix4f, vertexConsumer, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, Direction.EAST);
		this.renderFace(theEndPortalBlockEntity, matrix4f, vertexConsumer, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, Direction.WEST);
		this.renderFace(theEndPortalBlockEntity, matrix4f, vertexConsumer, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, Direction.DOWN);
		this.renderFace(theEndPortalBlockEntity, matrix4f, vertexConsumer, 0.0F, 1.0F, f, f, 1.0F, 1.0F, 0.0F, 0.0F, Direction.UP);
	}

	private void renderFace(
		T theEndPortalBlockEntity,
		Matrix4f matrix4f,
		VertexConsumer vertexConsumer,
		float f,
		float g,
		float h,
		float i,
		float j,
		float k,
		float l,
		float m,
		Direction direction
	) {
		if (theEndPortalBlockEntity.shouldRenderFace(direction)) {
			vertexConsumer.vertex(matrix4f, f, h, j).endVertex();
			vertexConsumer.vertex(matrix4f, g, h, k).endVertex();
			vertexConsumer.vertex(matrix4f, g, i, l).endVertex();
			vertexConsumer.vertex(matrix4f, f, i, m).endVertex();
		}
	}

	protected float getOffset() {
		return 0.75F;
	}

	protected RenderType renderType() {
		return RenderType.endPortal();
	}
}
