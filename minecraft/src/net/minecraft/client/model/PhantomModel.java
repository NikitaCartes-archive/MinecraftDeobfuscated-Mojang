package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class PhantomModel<T extends Entity> extends ListModel<T> {
	private final ModelPart body;
	private final ModelPart leftWingBase;
	private final ModelPart leftWingTip;
	private final ModelPart rightWingBase;
	private final ModelPart rightWingTip;
	private final ModelPart tailBase;
	private final ModelPart tailTip;

	public PhantomModel() {
		super(RenderType::entityCutoutNoCull);
		this.texWidth = 64;
		this.texHeight = 64;
		this.body = new ModelPart(this, 0, 8);
		this.body.addBox(-3.0F, -2.0F, -8.0F, 5.0F, 3.0F, 9.0F);
		this.tailBase = new ModelPart(this, 3, 20);
		this.tailBase.addBox(-2.0F, 0.0F, 0.0F, 3.0F, 2.0F, 6.0F);
		this.tailBase.setPos(0.0F, -2.0F, 1.0F);
		this.body.addChild(this.tailBase);
		this.tailTip = new ModelPart(this, 4, 29);
		this.tailTip.addBox(-1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 6.0F);
		this.tailTip.setPos(0.0F, 0.5F, 6.0F);
		this.tailBase.addChild(this.tailTip);
		this.leftWingBase = new ModelPart(this, 23, 12);
		this.leftWingBase.addBox(0.0F, 0.0F, 0.0F, 6.0F, 2.0F, 9.0F);
		this.leftWingBase.setPos(2.0F, -2.0F, -8.0F);
		this.leftWingTip = new ModelPart(this, 16, 24);
		this.leftWingTip.addBox(0.0F, 0.0F, 0.0F, 13.0F, 1.0F, 9.0F);
		this.leftWingTip.setPos(6.0F, 0.0F, 0.0F);
		this.leftWingBase.addChild(this.leftWingTip);
		this.rightWingBase = new ModelPart(this, 23, 12);
		this.rightWingBase.mirror = true;
		this.rightWingBase.addBox(-6.0F, 0.0F, 0.0F, 6.0F, 2.0F, 9.0F);
		this.rightWingBase.setPos(-3.0F, -2.0F, -8.0F);
		this.rightWingTip = new ModelPart(this, 16, 24);
		this.rightWingTip.mirror = true;
		this.rightWingTip.addBox(-13.0F, 0.0F, 0.0F, 13.0F, 1.0F, 9.0F);
		this.rightWingTip.setPos(-6.0F, 0.0F, 0.0F);
		this.rightWingBase.addChild(this.rightWingTip);
		this.leftWingBase.zRot = 0.1F;
		this.leftWingTip.zRot = 0.1F;
		this.rightWingBase.zRot = -0.1F;
		this.rightWingTip.zRot = -0.1F;
		this.body.xRot = -0.1F;
		ModelPart modelPart = new ModelPart(this, 0, 0);
		modelPart.addBox(-4.0F, -2.0F, -5.0F, 7.0F, 3.0F, 5.0F);
		modelPart.setPos(0.0F, 1.0F, -7.0F);
		modelPart.xRot = 0.2F;
		this.body.addChild(modelPart);
		this.body.addChild(this.leftWingBase);
		this.body.addChild(this.rightWingBase);
	}

	@Override
	public Iterable<ModelPart> parts() {
		return ImmutableList.<ModelPart>of(this.body);
	}

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j, float k) {
		float l = ((float)(entity.getId() * 3) + h) * 0.13F;
		float m = 16.0F;
		this.leftWingBase.zRot = Mth.cos(l) * 16.0F * (float) (Math.PI / 180.0);
		this.leftWingTip.zRot = Mth.cos(l) * 16.0F * (float) (Math.PI / 180.0);
		this.rightWingBase.zRot = -this.leftWingBase.zRot;
		this.rightWingTip.zRot = -this.leftWingTip.zRot;
		this.tailBase.xRot = -(5.0F + Mth.cos(l * 2.0F) * 5.0F) * (float) (Math.PI / 180.0);
		this.tailTip.xRot = -(5.0F + Mth.cos(l * 2.0F) * 5.0F) * (float) (Math.PI / 180.0);
	}
}
