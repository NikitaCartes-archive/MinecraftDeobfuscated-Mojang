package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;

@Environment(EnvType.CLIENT)
public class LlamaModel<T extends AbstractChestedHorse> extends EntityModel<T> {
	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart leg0;
	private final ModelPart leg1;
	private final ModelPart leg2;
	private final ModelPart leg3;
	private final ModelPart chest1;
	private final ModelPart chest2;

	public LlamaModel(float f) {
		this.texWidth = 128;
		this.texHeight = 64;
		this.head = new ModelPart(this, 0, 0);
		this.head.addBox(-2.0F, -14.0F, -10.0F, 4.0F, 4.0F, 9.0F, f);
		this.head.setPos(0.0F, 7.0F, -6.0F);
		this.head.texOffs(0, 14).addBox(-4.0F, -16.0F, -6.0F, 8.0F, 18.0F, 6.0F, f);
		this.head.texOffs(17, 0).addBox(-4.0F, -19.0F, -4.0F, 3.0F, 3.0F, 2.0F, f);
		this.head.texOffs(17, 0).addBox(1.0F, -19.0F, -4.0F, 3.0F, 3.0F, 2.0F, f);
		this.body = new ModelPart(this, 29, 0);
		this.body.addBox(-6.0F, -10.0F, -7.0F, 12.0F, 18.0F, 10.0F, f);
		this.body.setPos(0.0F, 5.0F, 2.0F);
		this.chest1 = new ModelPart(this, 45, 28);
		this.chest1.addBox(-3.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F, f);
		this.chest1.setPos(-8.5F, 3.0F, 3.0F);
		this.chest1.yRot = (float) (Math.PI / 2);
		this.chest2 = new ModelPart(this, 45, 41);
		this.chest2.addBox(-3.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F, f);
		this.chest2.setPos(5.5F, 3.0F, 3.0F);
		this.chest2.yRot = (float) (Math.PI / 2);
		int i = 4;
		int j = 14;
		this.leg0 = new ModelPart(this, 29, 29);
		this.leg0.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, f);
		this.leg0.setPos(-2.5F, 10.0F, 6.0F);
		this.leg1 = new ModelPart(this, 29, 29);
		this.leg1.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, f);
		this.leg1.setPos(2.5F, 10.0F, 6.0F);
		this.leg2 = new ModelPart(this, 29, 29);
		this.leg2.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, f);
		this.leg2.setPos(-2.5F, 10.0F, -4.0F);
		this.leg3 = new ModelPart(this, 29, 29);
		this.leg3.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, f);
		this.leg3.setPos(2.5F, 10.0F, -4.0F);
		this.leg0.x--;
		this.leg1.x++;
		this.leg0.z += 0.0F;
		this.leg1.z += 0.0F;
		this.leg2.x--;
		this.leg3.x++;
		this.leg2.z--;
		this.leg3.z--;
	}

	public void setupAnim(T abstractChestedHorse, float f, float g, float h, float i, float j) {
		this.head.xRot = j * (float) (Math.PI / 180.0);
		this.head.yRot = i * (float) (Math.PI / 180.0);
		this.body.xRot = (float) (Math.PI / 2);
		this.leg0.xRot = Mth.cos(f * 0.6662F) * 1.4F * g;
		this.leg1.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 1.4F * g;
		this.leg2.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 1.4F * g;
		this.leg3.xRot = Mth.cos(f * 0.6662F) * 1.4F * g;
		boolean bl = !abstractChestedHorse.isBaby() && abstractChestedHorse.hasChest();
		this.chest1.visible = bl;
		this.chest2.visible = bl;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
		if (this.young) {
			float l = 2.0F;
			poseStack.pushPose();
			float m = 0.7F;
			poseStack.scale(0.71428573F, 0.64935064F, 0.7936508F);
			poseStack.translate(0.0, 1.3125, 0.22F);
			this.head.render(poseStack, vertexConsumer, i, j, f, g, h, k);
			poseStack.popPose();
			poseStack.pushPose();
			float n = 1.1F;
			poseStack.scale(0.625F, 0.45454544F, 0.45454544F);
			poseStack.translate(0.0, 2.0625, 0.0);
			this.body.render(poseStack, vertexConsumer, i, j, f, g, h, k);
			poseStack.popPose();
			poseStack.pushPose();
			poseStack.scale(0.45454544F, 0.41322312F, 0.45454544F);
			poseStack.translate(0.0, 2.0625, 0.0);
			ImmutableList.of(this.leg0, this.leg1, this.leg2, this.leg3, this.chest1, this.chest2)
				.forEach(modelPart -> modelPart.render(poseStack, vertexConsumer, i, j, f, g, h, k));
			poseStack.popPose();
		} else {
			ImmutableList.of(this.head, this.body, this.leg0, this.leg1, this.leg2, this.leg3, this.chest1, this.chest2)
				.forEach(modelPart -> modelPart.render(poseStack, vertexConsumer, i, j, f, g, h, k));
		}
	}
}
