package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class OcelotModel<T extends Entity> extends AgeableListModel<T> {
	protected final ModelPart backLegL;
	protected final ModelPart backLegR;
	protected final ModelPart frontLegL;
	protected final ModelPart frontLegR;
	protected final ModelPart tail1;
	protected final ModelPart tail2;
	protected final ModelPart head;
	protected final ModelPart body;
	protected int state = 1;

	public OcelotModel(float f) {
		super(true, 10.0F, 4.0F);
		this.head = new ModelPart(this);
		this.head.addBox("main", -2.5F, -2.0F, -3.0F, 5, 4, 5, f, 0, 0);
		this.head.addBox("nose", -1.5F, 0.0F, -4.0F, 3, 2, 2, f, 0, 24);
		this.head.addBox("ear1", -2.0F, -3.0F, 0.0F, 1, 1, 2, f, 0, 10);
		this.head.addBox("ear2", 1.0F, -3.0F, 0.0F, 1, 1, 2, f, 6, 10);
		this.head.setPos(0.0F, 15.0F, -9.0F);
		this.body = new ModelPart(this, 20, 0);
		this.body.addBox(-2.0F, 3.0F, -8.0F, 4.0F, 16.0F, 6.0F, f);
		this.body.setPos(0.0F, 12.0F, -10.0F);
		this.tail1 = new ModelPart(this, 0, 15);
		this.tail1.addBox(-0.5F, 0.0F, 0.0F, 1.0F, 8.0F, 1.0F, f);
		this.tail1.xRot = 0.9F;
		this.tail1.setPos(0.0F, 15.0F, 8.0F);
		this.tail2 = new ModelPart(this, 4, 15);
		this.tail2.addBox(-0.5F, 0.0F, 0.0F, 1.0F, 8.0F, 1.0F, f);
		this.tail2.setPos(0.0F, 20.0F, 14.0F);
		this.backLegL = new ModelPart(this, 8, 13);
		this.backLegL.addBox(-1.0F, 0.0F, 1.0F, 2.0F, 6.0F, 2.0F, f);
		this.backLegL.setPos(1.1F, 18.0F, 5.0F);
		this.backLegR = new ModelPart(this, 8, 13);
		this.backLegR.addBox(-1.0F, 0.0F, 1.0F, 2.0F, 6.0F, 2.0F, f);
		this.backLegR.setPos(-1.1F, 18.0F, 5.0F);
		this.frontLegL = new ModelPart(this, 40, 0);
		this.frontLegL.addBox(-1.0F, 0.0F, 0.0F, 2.0F, 10.0F, 2.0F, f);
		this.frontLegL.setPos(1.2F, 14.1F, -5.0F);
		this.frontLegR = new ModelPart(this, 40, 0);
		this.frontLegR.addBox(-1.0F, 0.0F, 0.0F, 2.0F, 10.0F, 2.0F, f);
		this.frontLegR.setPos(-1.2F, 14.1F, -5.0F);
	}

	@Override
	protected Iterable<ModelPart> headParts() {
		return ImmutableList.<ModelPart>of(this.head);
	}

	@Override
	protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.<ModelPart>of(this.body, this.backLegL, this.backLegR, this.frontLegL, this.frontLegR, this.tail1, this.tail2);
	}

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j) {
		this.head.xRot = j * (float) (Math.PI / 180.0);
		this.head.yRot = i * (float) (Math.PI / 180.0);
		if (this.state != 3) {
			this.body.xRot = (float) (Math.PI / 2);
			if (this.state == 2) {
				this.backLegL.xRot = Mth.cos(f * 0.6662F) * g;
				this.backLegR.xRot = Mth.cos(f * 0.6662F + 0.3F) * g;
				this.frontLegL.xRot = Mth.cos(f * 0.6662F + (float) Math.PI + 0.3F) * g;
				this.frontLegR.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * g;
				this.tail2.xRot = 1.7278761F + (float) (Math.PI / 10) * Mth.cos(f) * g;
			} else {
				this.backLegL.xRot = Mth.cos(f * 0.6662F) * g;
				this.backLegR.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * g;
				this.frontLegL.xRot = Mth.cos(f * 0.6662F + (float) Math.PI) * g;
				this.frontLegR.xRot = Mth.cos(f * 0.6662F) * g;
				if (this.state == 1) {
					this.tail2.xRot = 1.7278761F + (float) (Math.PI / 4) * Mth.cos(f) * g;
				} else {
					this.tail2.xRot = 1.7278761F + 0.47123894F * Mth.cos(f) * g;
				}
			}
		}
	}

	@Override
	public void prepareMobModel(T entity, float f, float g, float h) {
		this.body.y = 12.0F;
		this.body.z = -10.0F;
		this.head.y = 15.0F;
		this.head.z = -9.0F;
		this.tail1.y = 15.0F;
		this.tail1.z = 8.0F;
		this.tail2.y = 20.0F;
		this.tail2.z = 14.0F;
		this.frontLegL.y = 14.1F;
		this.frontLegL.z = -5.0F;
		this.frontLegR.y = 14.1F;
		this.frontLegR.z = -5.0F;
		this.backLegL.y = 18.0F;
		this.backLegL.z = 5.0F;
		this.backLegR.y = 18.0F;
		this.backLegR.z = 5.0F;
		this.tail1.xRot = 0.9F;
		if (entity.isCrouching()) {
			this.body.y++;
			this.head.y += 2.0F;
			this.tail1.y++;
			this.tail2.y += -4.0F;
			this.tail2.z += 2.0F;
			this.tail1.xRot = (float) (Math.PI / 2);
			this.tail2.xRot = (float) (Math.PI / 2);
			this.state = 0;
		} else if (entity.isSprinting()) {
			this.tail2.y = this.tail1.y;
			this.tail2.z += 2.0F;
			this.tail1.xRot = (float) (Math.PI / 2);
			this.tail2.xRot = (float) (Math.PI / 2);
			this.state = 2;
		} else {
			this.state = 1;
		}
	}
}
