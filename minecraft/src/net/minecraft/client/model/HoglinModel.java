package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Hoglin;

@Environment(EnvType.CLIENT)
public class HoglinModel extends ListModel<Hoglin> {
	private final ModelPart head;
	private final ModelPart ear0;
	private final ModelPart ear1;
	private final ModelPart body;
	private final ModelPart leg0;
	private final ModelPart leg1;
	private final ModelPart leg2;
	private final ModelPart leg3;

	public HoglinModel() {
		this.texWidth = 128;
		this.texHeight = 128;
		this.body = new ModelPart(this);
		this.body.setPos(0.0F, 7.0F, 0.0F);
		this.body.texOffs(1, 1).addBox(-8.0F, -7.0F, -13.0F, 16.0F, 14.0F, 26.0F);
		ModelPart modelPart = new ModelPart(this);
		modelPart.setPos(0.0F, -14.0F, -7.0F);
		modelPart.texOffs(5, 67).addBox(0.0F, 0.0F, -9.0F, 0.0F, 10.0F, 19.0F, 0.001F);
		this.body.addChild(modelPart);
		this.head = new ModelPart(this);
		this.head.setPos(0.0F, 2.0F, -12.0F);
		this.head.texOffs(1, 42).addBox(-7.0F, -3.0F, -19.0F, 14.0F, 6.0F, 19.0F);
		this.ear0 = new ModelPart(this);
		this.ear0.setPos(-6.0F, -2.0F, -3.0F);
		this.ear0.texOffs(4, 16).addBox(-6.0F, -1.0F, -2.0F, 6.0F, 1.0F, 4.0F);
		this.ear0.zRot = (float) (-Math.PI * 2.0 / 9.0);
		this.head.addChild(this.ear0);
		this.ear1 = new ModelPart(this);
		this.ear1.setPos(6.0F, -2.0F, -3.0F);
		this.ear1.texOffs(4, 21).addBox(0.0F, -1.0F, -2.0F, 6.0F, 1.0F, 4.0F);
		this.ear1.zRot = (float) (Math.PI * 2.0 / 9.0);
		this.head.addChild(this.ear1);
		ModelPart modelPart2 = new ModelPart(this);
		modelPart2.setPos(-7.0F, 2.0F, -12.0F);
		modelPart2.texOffs(6, 45).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 11.0F, 2.0F);
		this.head.addChild(modelPart2);
		ModelPart modelPart3 = new ModelPart(this);
		modelPart3.setPos(7.0F, 2.0F, -12.0F);
		modelPart3.texOffs(6, 45).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 11.0F, 2.0F);
		this.head.addChild(modelPart3);
		this.head.xRot = 0.87266463F;
		int i = 14;
		int j = 11;
		this.leg0 = new ModelPart(this);
		this.leg0.setPos(-4.0F, 10.0F, -8.5F);
		this.leg0.texOffs(46, 75).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 14.0F, 6.0F);
		this.leg1 = new ModelPart(this);
		this.leg1.setPos(4.0F, 10.0F, -8.5F);
		this.leg1.texOffs(71, 75).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 14.0F, 6.0F);
		this.leg2 = new ModelPart(this);
		this.leg2.setPos(-5.0F, 13.0F, 10.0F);
		this.leg2.texOffs(51, 43).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 11.0F, 5.0F);
		this.leg3 = new ModelPart(this);
		this.leg3.setPos(5.0F, 13.0F, 10.0F);
		this.leg3.texOffs(72, 43).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 11.0F, 5.0F);
	}

	@Override
	public Iterable<ModelPart> parts() {
		return ImmutableList.<ModelPart>of(this.body, this.head, this.leg0, this.leg1, this.leg2, this.leg3);
	}

	public void setupAnim(Hoglin hoglin, float f, float g, float h, float i, float j) {
		this.ear0.zRot = (float) (-Math.PI * 2.0 / 9.0) - g * 2.5F * Mth.sin(f * 3.0F);
		this.ear1.zRot = (float) (Math.PI * 2.0 / 9.0) + g * 2.5F * Mth.sin(f * 3.0F);
		this.head.yRot = i * (float) (Math.PI / 180.0);
		this.leg0.xRot = Mth.cos(f * 1.5F) * 4.4F * g;
		this.leg1.xRot = Mth.cos(f * 1.5F + (float) Math.PI) * 4.4F * g;
		this.leg2.xRot = Mth.cos(f * 1.5F + (float) Math.PI) * 4.4F * g;
		this.leg3.xRot = Mth.cos(f * 1.5F) * 4.4F * g;
	}
}
