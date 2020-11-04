package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public abstract class Model {
	protected final Function<ResourceLocation, RenderType> renderType;

	public Model(Function<ResourceLocation, RenderType> function) {
		this.renderType = function;
	}

	public final RenderType renderType(ResourceLocation resourceLocation) {
		return (RenderType)this.renderType.apply(resourceLocation);
	}

	public abstract void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k);
}
