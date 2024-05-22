package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public abstract class ColorableHierarchicalModel<E extends Entity> extends HierarchicalModel<E> {
	private int color = -1;

	public void setColor(int i) {
		this.color = i;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, int k) {
		super.renderToBuffer(poseStack, vertexConsumer, i, j, FastColor.ARGB32.multiply(k, this.color));
	}
}
