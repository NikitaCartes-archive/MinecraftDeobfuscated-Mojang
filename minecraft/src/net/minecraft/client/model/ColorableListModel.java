package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public abstract class ColorableListModel<E extends Entity> extends ListModel<E> {
	private float r = 1.0F;
	private float g = 1.0F;
	private float b = 1.0F;

	public ColorableListModel(Function<ResourceLocation, RenderType> function) {
		super(function);
	}

	public void setColor(float f, float g, float h) {
		this.r = f;
		this.g = g;
		this.b = h;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h) {
		super.renderToBuffer(poseStack, vertexConsumer, i, j, this.r * f, this.g * g, this.b * h);
	}
}
