package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public abstract class ListModel<E extends Entity> extends EntityModel<E> {
	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h) {
		this.parts().forEach(modelPart -> modelPart.render(poseStack, vertexConsumer, 0.0625F, i, j, null, f, g, h));
	}

	public abstract Iterable<ModelPart> parts();
}
