package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public abstract class Model implements Consumer<ModelPart> {
	protected final Function<ResourceLocation, RenderType> renderType;
	public int texWidth = 64;
	public int texHeight = 32;

	public Model(Function<ResourceLocation, RenderType> function) {
		this.renderType = function;
	}

	public void accept(ModelPart modelPart) {
	}

	public final RenderType renderType(ResourceLocation resourceLocation) {
		return (RenderType)this.renderType.apply(resourceLocation);
	}

	public abstract void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k);
}
