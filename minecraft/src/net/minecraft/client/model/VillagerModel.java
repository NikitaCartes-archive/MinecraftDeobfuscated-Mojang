package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.HeadedModel;
import net.minecraft.client.renderer.entity.VillagerHeadModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.AbstractVillager;

@Environment(EnvType.CLIENT)
public class VillagerModel<T extends Entity> extends EntityModel<T> implements HeadedModel, VillagerHeadModel {
	protected final ModelPart head;
	protected ModelPart hat;
	protected final ModelPart hatRim;
	protected final ModelPart body;
	protected final ModelPart jacket;
	protected final ModelPart arms;
	protected final ModelPart leg0;
	protected final ModelPart leg1;
	protected final ModelPart nose;

	public VillagerModel(float f) {
		this(f, 64, 64);
	}

	public VillagerModel(float f, int i, int j) {
		float g = 0.5F;
		this.head = new ModelPart(this).setTexSize(i, j);
		this.head.setPos(0.0F, 0.0F, 0.0F);
		this.head.texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8, 10, 8, f);
		this.hat = new ModelPart(this).setTexSize(i, j);
		this.hat.setPos(0.0F, 0.0F, 0.0F);
		this.hat.texOffs(32, 0).addBox(-4.0F, -10.0F, -4.0F, 8, 10, 8, f + 0.5F);
		this.head.addChild(this.hat);
		this.hatRim = new ModelPart(this).setTexSize(i, j);
		this.hatRim.setPos(0.0F, 0.0F, 0.0F);
		this.hatRim.texOffs(30, 47).addBox(-8.0F, -8.0F, -6.0F, 16, 16, 1, f);
		this.hatRim.xRot = (float) (-Math.PI / 2);
		this.hat.addChild(this.hatRim);
		this.nose = new ModelPart(this).setTexSize(i, j);
		this.nose.setPos(0.0F, -2.0F, 0.0F);
		this.nose.texOffs(24, 0).addBox(-1.0F, -1.0F, -6.0F, 2, 4, 2, f);
		this.head.addChild(this.nose);
		this.body = new ModelPart(this).setTexSize(i, j);
		this.body.setPos(0.0F, 0.0F, 0.0F);
		this.body.texOffs(16, 20).addBox(-4.0F, 0.0F, -3.0F, 8, 12, 6, f);
		this.jacket = new ModelPart(this).setTexSize(i, j);
		this.jacket.setPos(0.0F, 0.0F, 0.0F);
		this.jacket.texOffs(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8, 18, 6, f + 0.5F);
		this.body.addChild(this.jacket);
		this.arms = new ModelPart(this).setTexSize(i, j);
		this.arms.setPos(0.0F, 2.0F, 0.0F);
		this.arms.texOffs(44, 22).addBox(-8.0F, -2.0F, -2.0F, 4, 8, 4, f);
		this.arms.texOffs(44, 22).addBox(4.0F, -2.0F, -2.0F, 4, 8, 4, f, true);
		this.arms.texOffs(40, 38).addBox(-4.0F, 2.0F, -2.0F, 8, 4, 4, f);
		this.leg0 = new ModelPart(this, 0, 22).setTexSize(i, j);
		this.leg0.setPos(-2.0F, 12.0F, 0.0F);
		this.leg0.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, f);
		this.leg1 = new ModelPart(this, 0, 22).setTexSize(i, j);
		this.leg1.mirror = true;
		this.leg1.setPos(2.0F, 12.0F, 0.0F);
		this.leg1.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, f);
	}

	@Override
	public void render(T entity, float f, float g, float h, float i, float j, float k) {
		this.setupAnim(entity, f, g, h, i, j, k);
		this.head.render(k);
		this.body.render(k);
		this.leg0.render(k);
		this.leg1.render(k);
		this.arms.render(k);
	}

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
		boolean bl = false;
		if (entity instanceof AbstractVillager) {
			bl = ((AbstractVillager)entity).getUnhappyCounter() > 0;
		}

		this.head.yRot = i * (float) (Math.PI / 180.0);
		this.head.xRot = j * (float) (Math.PI / 180.0);
		if (bl) {
			this.head.zRot = 0.3F * Mth.sin(0.45F * h);
			this.head.xRot = 0.4F;
		} else {
			this.head.zRot = 0.0F;
		}

		this.arms.y = 3.0F;
		this.arms.z = -1.0F;
		this.arms.xRot = -0.75F;
		this.leg0.xRot = Mth.cos(f * 0.6662F) * 1.4F * g * 0.5F;
		this.leg1.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * 1.4F * g * 0.5F;
		this.leg0.yRot = 0.0F;
		this.leg1.yRot = 0.0F;
	}

	@Override
	public ModelPart getHead() {
		return this.head;
	}

	@Override
	public void hatVisible(boolean bl) {
		this.head.visible = bl;
		this.hat.visible = bl;
		this.hatRim.visible = bl;
	}
}
