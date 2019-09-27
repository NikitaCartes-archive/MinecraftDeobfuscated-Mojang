package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public abstract class EntityModel<T extends Entity> extends Model {
	public float attackTime;
	public boolean riding;
	public boolean young = true;

	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i) {
		this.renderToBuffer(poseStack, vertexConsumer, i, 1.0F, 1.0F, 1.0F);
	}

	public abstract void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, float f, float g, float h);

	public abstract void setupAnim(T entity, float f, float g, float h, float i, float j, float k);

	public void prepareMobModel(T entity, float f, float g, float h) {
	}

	public void copyPropertiesTo(EntityModel<T> entityModel) {
		entityModel.attackTime = this.attackTime;
		entityModel.riding = this.riding;
		entityModel.young = this.young;
	}
}
