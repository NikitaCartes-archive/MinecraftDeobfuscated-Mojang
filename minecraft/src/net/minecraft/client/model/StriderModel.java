package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Strider;

@Environment(EnvType.CLIENT)
public class StriderModel<T extends Strider> extends ListModel<T> {
	private final ModelPart rightLeg;
	private final ModelPart leftLeg;
	private final ModelPart body;
	private final ModelPart bristle0;
	private final ModelPart bristle1;
	private final ModelPart bristle2;
	private final ModelPart bristle3;
	private final ModelPart bristle4;
	private final ModelPart bristle5;

	public StriderModel() {
		this.texWidth = 64;
		this.texHeight = 128;
		this.rightLeg = new ModelPart(this, 0, 32);
		this.rightLeg.setPos(-4.0F, 8.0F, 0.0F);
		this.rightLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 16.0F, 4.0F, 0.0F);
		this.leftLeg = new ModelPart(this, 0, 55);
		this.leftLeg.setPos(4.0F, 8.0F, 0.0F);
		this.leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 16.0F, 4.0F, 0.0F);
		this.body = new ModelPart(this, 0, 0);
		this.body.setPos(0.0F, 1.0F, 0.0F);
		this.body.addBox(-8.0F, -6.0F, -8.0F, 16.0F, 14.0F, 16.0F, 0.0F);
		this.bristle0 = new ModelPart(this, 16, 65);
		this.bristle0.setPos(-8.0F, 4.0F, -8.0F);
		this.bristle0.addBox(-12.0F, 0.0F, 0.0F, 12.0F, 0.0F, 16.0F, 0.0F, true);
		this.setRotationAngle(this.bristle0, 0.0F, 0.0F, -1.2217305F);
		this.bristle1 = new ModelPart(this, 16, 49);
		this.bristle1.setPos(-8.0F, -1.0F, -8.0F);
		this.bristle1.addBox(-12.0F, 0.0F, 0.0F, 12.0F, 0.0F, 16.0F, 0.0F, true);
		this.setRotationAngle(this.bristle1, 0.0F, 0.0F, -1.134464F);
		this.bristle2 = new ModelPart(this, 16, 33);
		this.bristle2.setPos(-8.0F, -5.0F, -8.0F);
		this.bristle2.addBox(-12.0F, 0.0F, 0.0F, 12.0F, 0.0F, 16.0F, 0.0F, true);
		this.setRotationAngle(this.bristle2, 0.0F, 0.0F, -0.87266463F);
		this.bristle3 = new ModelPart(this, 16, 33);
		this.bristle3.setPos(8.0F, -6.0F, -8.0F);
		this.bristle3.addBox(0.0F, 0.0F, 0.0F, 12.0F, 0.0F, 16.0F, 0.0F);
		this.setRotationAngle(this.bristle3, 0.0F, 0.0F, 0.87266463F);
		this.bristle4 = new ModelPart(this, 16, 49);
		this.bristle4.setPos(8.0F, -2.0F, -8.0F);
		this.bristle4.addBox(0.0F, 0.0F, 0.0F, 12.0F, 0.0F, 16.0F, 0.0F);
		this.setRotationAngle(this.bristle4, 0.0F, 0.0F, 1.134464F);
		this.bristle5 = new ModelPart(this, 16, 65);
		this.bristle5.setPos(8.0F, 3.0F, -8.0F);
		this.bristle5.addBox(0.0F, 0.0F, 0.0F, 12.0F, 0.0F, 16.0F, 0.0F);
		this.setRotationAngle(this.bristle5, 0.0F, 0.0F, 1.2217305F);
		this.body.addChild(this.bristle0);
		this.body.addChild(this.bristle1);
		this.body.addChild(this.bristle2);
		this.body.addChild(this.bristle3);
		this.body.addChild(this.bristle4);
		this.body.addChild(this.bristle5);
	}

	public void setupAnim(Strider strider, float f, float g, float h, float i, float j) {
		g = Math.min(0.25F, g);
		if (strider.getPassengers().size() <= 0) {
			this.body.xRot = j * (float) (Math.PI / 180.0);
			this.body.yRot = i * (float) (Math.PI / 180.0);
		} else {
			this.body.xRot = 0.0F;
			this.body.yRot = 0.0F;
		}

		float k = 1.5F;
		this.body.zRot = 0.1F * Mth.sin(f * 1.5F) * 4.0F * g;
		this.body.y = 2.0F;
		this.body.y = this.body.y - 2.0F * Mth.cos(f * 1.5F) * 2.0F * g;
		this.leftLeg.xRot = Mth.sin(f * 1.5F * 0.5F) * 2.0F * g;
		this.rightLeg.xRot = Mth.sin(f * 1.5F * 0.5F + (float) Math.PI) * 2.0F * g;
		this.leftLeg.zRot = (float) (Math.PI / 18) * Mth.cos(f * 1.5F * 0.5F) * g;
		this.rightLeg.zRot = (float) (Math.PI / 18) * Mth.cos(f * 1.5F * 0.5F + (float) Math.PI) * g;
		this.leftLeg.y = 8.0F + 2.0F * Mth.sin(f * 1.5F * 0.5F + (float) Math.PI) * 2.0F * g;
		this.rightLeg.y = 8.0F + 2.0F * Mth.sin(f * 1.5F * 0.5F) * 2.0F * g;
		this.bristle0.zRot = -1.2217305F;
		this.bristle1.zRot = -1.134464F;
		this.bristle2.zRot = -0.87266463F;
		this.bristle3.zRot = 0.87266463F;
		this.bristle4.zRot = 1.134464F;
		this.bristle5.zRot = 1.2217305F;
		float l = Mth.cos(f * 1.5F + (float) Math.PI) * g;
		this.bristle0.zRot += l * 1.3F;
		this.bristle1.zRot += l * 1.2F;
		this.bristle2.zRot += l * 0.6F;
		this.bristle3.zRot += l * 0.6F;
		this.bristle4.zRot += l * 1.2F;
		this.bristle5.zRot += l * 1.3F;
		float m = 1.0F;
		float n = 1.0F;
		this.bristle0.zRot = this.bristle0.zRot + 0.05F * Mth.sin(h * 1.0F * -0.4F);
		this.bristle1.zRot = this.bristle1.zRot + 0.1F * Mth.sin(h * 1.0F * 0.2F);
		this.bristle2.zRot = this.bristle2.zRot + 0.1F * Mth.sin(h * 1.0F * 0.4F);
		this.bristle3.zRot = this.bristle3.zRot + 0.1F * Mth.sin(h * 1.0F * 0.4F);
		this.bristle4.zRot = this.bristle4.zRot + 0.1F * Mth.sin(h * 1.0F * 0.2F);
		this.bristle5.zRot = this.bristle5.zRot + 0.05F * Mth.sin(h * 1.0F * -0.4F);
	}

	public void setRotationAngle(ModelPart modelPart, float f, float g, float h) {
		modelPart.xRot = f;
		modelPart.yRot = g;
		modelPart.zRot = h;
	}

	@Override
	public Iterable<ModelPart> parts() {
		return ImmutableList.<ModelPart>of(this.body, this.leftLeg, this.rightLeg);
	}
}
