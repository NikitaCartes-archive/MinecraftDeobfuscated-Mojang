package net.minecraft.client.model;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public class PlayerModel<T extends LivingEntity> extends HumanoidModel<T> {
	private List<ModelPart> cubes = Lists.<ModelPart>newArrayList();
	public final ModelPart leftSleeve;
	public final ModelPart rightSleeve;
	public final ModelPart leftPants;
	public final ModelPart rightPants;
	public final ModelPart jacket;
	private final ModelPart cloak;
	private final ModelPart ear;
	private final boolean slim;

	public PlayerModel(float f, boolean bl) {
		super(f, 0.0F, 64, 64);
		this.slim = bl;
		this.ear = new ModelPart(this, 24, 0);
		this.ear.addBox(-3.0F, -6.0F, -1.0F, 6.0F, 6.0F, 1.0F, f);
		this.cloak = new ModelPart(this, 0, 0);
		this.cloak.setTexSize(64, 32);
		this.cloak.addBox(-5.0F, 0.0F, -1.0F, 10.0F, 16.0F, 1.0F, f);
		if (bl) {
			this.leftArm = new ModelPart(this, 32, 48);
			this.leftArm.addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, f);
			this.leftArm.setPos(5.0F, 2.5F, 0.0F);
			this.rightArm = new ModelPart(this, 40, 16);
			this.rightArm.addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, f);
			this.rightArm.setPos(-5.0F, 2.5F, 0.0F);
			this.leftSleeve = new ModelPart(this, 48, 48);
			this.leftSleeve.addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, f + 0.25F);
			this.leftSleeve.setPos(5.0F, 2.5F, 0.0F);
			this.rightSleeve = new ModelPart(this, 40, 32);
			this.rightSleeve.addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, f + 0.25F);
			this.rightSleeve.setPos(-5.0F, 2.5F, 10.0F);
		} else {
			this.leftArm = new ModelPart(this, 32, 48);
			this.leftArm.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, f);
			this.leftArm.setPos(5.0F, 2.0F, 0.0F);
			this.leftSleeve = new ModelPart(this, 48, 48);
			this.leftSleeve.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, f + 0.25F);
			this.leftSleeve.setPos(5.0F, 2.0F, 0.0F);
			this.rightSleeve = new ModelPart(this, 40, 32);
			this.rightSleeve.addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, f + 0.25F);
			this.rightSleeve.setPos(-5.0F, 2.0F, 10.0F);
		}

		this.leftLeg = new ModelPart(this, 16, 48);
		this.leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, f);
		this.leftLeg.setPos(1.9F, 12.0F, 0.0F);
		this.leftPants = new ModelPart(this, 0, 48);
		this.leftPants.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, f + 0.25F);
		this.leftPants.setPos(1.9F, 12.0F, 0.0F);
		this.rightPants = new ModelPart(this, 0, 32);
		this.rightPants.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, f + 0.25F);
		this.rightPants.setPos(-1.9F, 12.0F, 0.0F);
		this.jacket = new ModelPart(this, 16, 32);
		this.jacket.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, f + 0.25F);
		this.jacket.setPos(0.0F, 0.0F, 0.0F);
	}

	@Override
	public void render(T livingEntity, float f, float g, float h, float i, float j, float k) {
		super.render(livingEntity, f, g, h, i, j, k);
		RenderSystem.pushMatrix();
		if (this.young) {
			float l = 2.0F;
			RenderSystem.scalef(0.5F, 0.5F, 0.5F);
			RenderSystem.translatef(0.0F, 24.0F * k, 0.0F);
			this.leftPants.render(k);
			this.rightPants.render(k);
			this.leftSleeve.render(k);
			this.rightSleeve.render(k);
			this.jacket.render(k);
		} else {
			if (livingEntity.isCrouching()) {
				RenderSystem.translatef(0.0F, 0.2F, 0.0F);
			}

			this.leftPants.render(k);
			this.rightPants.render(k);
			this.leftSleeve.render(k);
			this.rightSleeve.render(k);
			this.jacket.render(k);
		}

		RenderSystem.popMatrix();
	}

	public void renderEars(float f) {
		this.ear.copyFrom(this.head);
		this.ear.x = 0.0F;
		this.ear.y = 0.0F;
		this.ear.render(f);
	}

	public void renderCloak(float f) {
		this.cloak.render(f);
	}

	@Override
	public void setupAnim(T livingEntity, float f, float g, float h, float i, float j, float k) {
		super.setupAnim(livingEntity, f, g, h, i, j, k);
		this.leftPants.copyFrom(this.leftLeg);
		this.rightPants.copyFrom(this.rightLeg);
		this.leftSleeve.copyFrom(this.leftArm);
		this.rightSleeve.copyFrom(this.rightArm);
		this.jacket.copyFrom(this.body);
		if (livingEntity.isCrouching()) {
			this.cloak.y = 2.0F;
		} else {
			this.cloak.y = 0.0F;
		}
	}

	@Override
	public void setAllVisible(boolean bl) {
		super.setAllVisible(bl);
		this.leftSleeve.visible = bl;
		this.rightSleeve.visible = bl;
		this.leftPants.visible = bl;
		this.rightPants.visible = bl;
		this.jacket.visible = bl;
		this.cloak.visible = bl;
		this.ear.visible = bl;
	}

	@Override
	public void translateToHand(float f, HumanoidArm humanoidArm) {
		ModelPart modelPart = this.getArm(humanoidArm);
		if (this.slim) {
			float g = 0.5F * (float)(humanoidArm == HumanoidArm.RIGHT ? 1 : -1);
			modelPart.x += g;
			modelPart.translateTo(f);
			modelPart.x -= g;
		} else {
			modelPart.translateTo(f);
		}
	}

	public ModelPart getRandomModelPart(Random random) {
		return (ModelPart)this.cubes.get(random.nextInt(this.cubes.size()));
	}

	@Override
	public void accept(ModelPart modelPart) {
		if (this.cubes == null) {
			this.cubes = Lists.<ModelPart>newArrayList();
		}

		this.cubes.add(modelPart);
	}
}
