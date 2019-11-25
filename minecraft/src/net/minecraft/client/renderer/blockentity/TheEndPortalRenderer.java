package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
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
	private static final List<RenderType> RENDER_TYPES = (List<RenderType>)IntStream.range(0, 16)
		.mapToObj(i -> RenderType.endPortal(i + 1))
		.collect(ImmutableList.toImmutableList());

	public TheEndPortalRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
		super(blockEntityRenderDispatcher);
	}

	public void render(T theEndPortalBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		RANDOM.setSeed(31100L);
		double d = theEndPortalBlockEntity.getBlockPos().distSqr(this.renderer.camera.getPosition(), true);
		int k = this.getPasses(d);
		float g = this.getOffset();
		Matrix4f matrix4f = poseStack.last().pose();
		this.renderCube(theEndPortalBlockEntity, g, 0.15F, matrix4f, multiBufferSource.getBuffer((RenderType)RENDER_TYPES.get(0)));

		for (int l = 1; l < k; l++) {
			this.renderCube(theEndPortalBlockEntity, g, 2.0F / (float)(18 - l), matrix4f, multiBufferSource.getBuffer((RenderType)RENDER_TYPES.get(l)));
		}
	}

	private void renderCube(T theEndPortalBlockEntity, float f, float g, Matrix4f matrix4f, VertexConsumer vertexConsumer) {
		float h = (RANDOM.nextFloat() * 0.5F + 0.1F) * g;
		float i = (RANDOM.nextFloat() * 0.5F + 0.4F) * g;
		float j = (RANDOM.nextFloat() * 0.5F + 0.5F) * g;
		this.renderFace(theEndPortalBlockEntity, matrix4f, vertexConsumer, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, h, i, j, Direction.SOUTH);
		this.renderFace(theEndPortalBlockEntity, matrix4f, vertexConsumer, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, h, i, j, Direction.NORTH);
		this.renderFace(theEndPortalBlockEntity, matrix4f, vertexConsumer, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, h, i, j, Direction.EAST);
		this.renderFace(theEndPortalBlockEntity, matrix4f, vertexConsumer, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, h, i, j, Direction.WEST);
		this.renderFace(theEndPortalBlockEntity, matrix4f, vertexConsumer, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, h, i, j, Direction.DOWN);
		this.renderFace(theEndPortalBlockEntity, matrix4f, vertexConsumer, 0.0F, 1.0F, f, f, 1.0F, 1.0F, 0.0F, 0.0F, h, i, j, Direction.UP);
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
		float n,
		float o,
		float p,
		Direction direction
	) {
		if (theEndPortalBlockEntity.shouldRenderFace(direction)) {
			vertexConsumer.vertex(matrix4f, f, h, j).color(n, o, p, 1.0F).endVertex();
			vertexConsumer.vertex(matrix4f, g, h, k).color(n, o, p, 1.0F).endVertex();
			vertexConsumer.vertex(matrix4f, g, i, l).color(n, o, p, 1.0F).endVertex();
			vertexConsumer.vertex(matrix4f, f, i, m).color(n, o, p, 1.0F).endVertex();
		}
	}

	protected int getPasses(double d) {
		if (d > 36864.0) {
			return 1;
		} else if (d > 25600.0) {
			return 3;
		} else if (d > 16384.0) {
			return 5;
		} else if (d > 9216.0) {
			return 7;
		} else if (d > 4096.0) {
			return 9;
		} else if (d > 1024.0) {
			return 11;
		} else if (d > 576.0) {
			return 13;
		} else {
			return d > 256.0 ? 14 : 15;
		}
	}

	protected float getOffset() {
		return 0.75F;
	}
}
