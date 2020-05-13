package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Shulker;

@Environment(EnvType.CLIENT)
public class ShulkerModel<T extends Shulker> extends ListModel<T> {
	private final ModelPart base;
	private final ModelPart lid = new ModelPart(64, 64, 0, 0);
	private final ModelPart head;

	public ShulkerModel() {
		super(RenderType::entityCutoutNoCullZOffset);
		this.base = new ModelPart(64, 64, 0, 28);
		this.head = new ModelPart(64, 64, 0, 52);
		this.lid.addBox(-8.0F, -16.0F, -8.0F, 16.0F, 12.0F, 16.0F);
		this.lid.setPos(0.0F, 24.0F, 0.0F);
		this.base.addBox(-8.0F, -8.0F, -8.0F, 16.0F, 8.0F, 16.0F);
		this.base.setPos(0.0F, 24.0F, 0.0F);
		this.head.addBox(-3.0F, 0.0F, -3.0F, 6.0F, 6.0F, 6.0F);
		this.head.setPos(0.0F, 12.0F, 0.0F);
	}

	public void setupAnim(T shulker, float f, float g, float h, float i, float j) {
		float k = h - (float)shulker.tickCount;
		float l = (0.5F + shulker.getClientPeekAmount(k)) * (float) Math.PI;
		float m = -1.0F + Mth.sin(l);
		float n = 0.0F;
		if (l > (float) Math.PI) {
			n = Mth.sin(h * 0.1F) * 0.7F;
		}

		this.lid.setPos(0.0F, 16.0F + Mth.sin(l) * 8.0F + n, 0.0F);
		if (shulker.getClientPeekAmount(k) > 0.3F) {
			this.lid.yRot = m * m * m * m * (float) Math.PI * 0.125F;
		} else {
			this.lid.yRot = 0.0F;
		}

		this.head.xRot = j * (float) (Math.PI / 180.0);
		this.head.yRot = (shulker.yHeadRot - 180.0F - shulker.yBodyRot) * (float) (Math.PI / 180.0);
	}

	@Override
	public Iterable<ModelPart> parts() {
		return ImmutableList.<ModelPart>of(this.base, this.lid);
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
