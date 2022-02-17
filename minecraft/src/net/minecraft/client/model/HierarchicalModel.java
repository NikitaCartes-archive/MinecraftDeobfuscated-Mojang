package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Optional;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public abstract class HierarchicalModel<E extends Entity> extends EntityModel<E> {
	public HierarchicalModel() {
		this(RenderType::entityCutoutNoCull);
	}

	public HierarchicalModel(Function<ResourceLocation, RenderType> function) {
		super(function);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
		this.root().render(poseStack, vertexConsumer, i, j, f, g, h, k);
	}

	public abstract ModelPart root();

	public Optional<ModelPart> getAnyDescendantWithName(String string) {
		return this.root().getAllParts().filter(modelPart -> modelPart.hasChild(string)).findFirst().map(modelPart -> modelPart.getChild(string));
	}
}
