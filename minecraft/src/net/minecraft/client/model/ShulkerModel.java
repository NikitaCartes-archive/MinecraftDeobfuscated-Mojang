package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Shulker;

@Environment(EnvType.CLIENT)
public class ShulkerModel<T extends Shulker> extends EntityModel<T> {
	private final ModelPart base;
	private final ModelPart lid;
	private final ModelPart head;

	public ShulkerModel() {
		this.texHeight = 64;
		this.texWidth = 64;
		this.lid = new ModelPart(this);
		this.base = new ModelPart(this);
		this.head = new ModelPart(this);
		this.lid.texOffs(0, 0).addBox(-8.0F, -16.0F, -8.0F, 16, 12, 16);
		this.lid.setPos(0.0F, 24.0F, 0.0F);
		this.base.texOffs(0, 28).addBox(-8.0F, -8.0F, -8.0F, 16, 8, 16);
		this.base.setPos(0.0F, 24.0F, 0.0F);
		this.head.texOffs(0, 52).addBox(-3.0F, 0.0F, -3.0F, 6, 6, 6);
		this.head.setPos(0.0F, 12.0F, 0.0F);
	}

	public void setupAnim(T shulker, float f, float g, float h, float i, float j, float k) {
		float l = h - (float)shulker.tickCount;
		float m = (0.5F + shulker.getClientPeekAmount(l)) * (float) Math.PI;
		float n = -1.0F + Mth.sin(m);
		float o = 0.0F;
		if (m > (float) Math.PI) {
			o = Mth.sin(h * 0.1F) * 0.7F;
		}

		this.lid.setPos(0.0F, 16.0F + Mth.sin(m) * 8.0F + o, 0.0F);
		if (shulker.getClientPeekAmount(l) > 0.3F) {
			this.lid.yRot = n * n * n * n * (float) Math.PI * 0.125F;
		} else {
			this.lid.yRot = 0.0F;
		}

		this.head.xRot = j * (float) (Math.PI / 180.0);
		this.head.yRot = i * (float) (Math.PI / 180.0);
	}

	public void render(T shulker, float f, float g, float h, float i, float j, float k) {
		this.base.render(k);
		this.lid.render(k);
	}

	public ModelPart getBase() {
		return this.base;
	}

	public ModelPart getLid() {
		return this.lid;
	}

	public ModelPart getHead() {
		return this.head;
	}
}
