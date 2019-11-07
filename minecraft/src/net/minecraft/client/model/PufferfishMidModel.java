package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class PufferfishMidModel<T extends Entity> extends ListModel<T> {
	private final ModelPart cube;
	private final ModelPart finBlue0;
	private final ModelPart finBlue1;
	private final ModelPart finTop0;
	private final ModelPart finTop1;
	private final ModelPart finSide0;
	private final ModelPart finSide1;
	private final ModelPart finSide2;
	private final ModelPart finSide3;
	private final ModelPart finBottom0;
	private final ModelPart finBottom1;

	public PufferfishMidModel() {
		this.texWidth = 32;
		this.texHeight = 32;
		int i = 22;
		this.cube = new ModelPart(this, 12, 22);
		this.cube.addBox(-2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F);
		this.cube.setPos(0.0F, 22.0F, 0.0F);
		this.finBlue0 = new ModelPart(this, 24, 0);
		this.finBlue0.addBox(-2.0F, 0.0F, 0.0F, 2.0F, 0.0F, 2.0F);
		this.finBlue0.setPos(-2.5F, 17.0F, -1.5F);
		this.finBlue1 = new ModelPart(this, 24, 3);
		this.finBlue1.addBox(0.0F, 0.0F, 0.0F, 2.0F, 0.0F, 2.0F);
		this.finBlue1.setPos(2.5F, 17.0F, -1.5F);
		this.finTop0 = new ModelPart(this, 15, 16);
		this.finTop0.addBox(-2.5F, -1.0F, 0.0F, 5.0F, 1.0F, 1.0F);
		this.finTop0.setPos(0.0F, 17.0F, -2.5F);
		this.finTop0.xRot = (float) (Math.PI / 4);
		this.finTop1 = new ModelPart(this, 10, 16);
		this.finTop1.addBox(-2.5F, -1.0F, -1.0F, 5.0F, 1.0F, 1.0F);
		this.finTop1.setPos(0.0F, 17.0F, 2.5F);
		this.finTop1.xRot = (float) (-Math.PI / 4);
		this.finSide0 = new ModelPart(this, 8, 16);
		this.finSide0.addBox(-1.0F, -5.0F, 0.0F, 1.0F, 5.0F, 1.0F);
		this.finSide0.setPos(-2.5F, 22.0F, -2.5F);
		this.finSide0.yRot = (float) (-Math.PI / 4);
		this.finSide1 = new ModelPart(this, 8, 16);
		this.finSide1.addBox(-1.0F, -5.0F, 0.0F, 1.0F, 5.0F, 1.0F);
		this.finSide1.setPos(-2.5F, 22.0F, 2.5F);
		this.finSide1.yRot = (float) (Math.PI / 4);
		this.finSide2 = new ModelPart(this, 4, 16);
		this.finSide2.addBox(0.0F, -5.0F, 0.0F, 1.0F, 5.0F, 1.0F);
		this.finSide2.setPos(2.5F, 22.0F, 2.5F);
		this.finSide2.yRot = (float) (-Math.PI / 4);
		this.finSide3 = new ModelPart(this, 0, 16);
		this.finSide3.addBox(0.0F, -5.0F, 0.0F, 1.0F, 5.0F, 1.0F);
		this.finSide3.setPos(2.5F, 22.0F, -2.5F);
		this.finSide3.yRot = (float) (Math.PI / 4);
		this.finBottom0 = new ModelPart(this, 8, 22);
		this.finBottom0.addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		this.finBottom0.setPos(0.5F, 22.0F, 2.5F);
		this.finBottom0.xRot = (float) (Math.PI / 4);
		this.finBottom1 = new ModelPart(this, 17, 21);
		this.finBottom1.addBox(-2.5F, 0.0F, 0.0F, 5.0F, 1.0F, 1.0F);
		this.finBottom1.setPos(0.0F, 22.0F, -2.5F);
		this.finBottom1.xRot = (float) (-Math.PI / 4);
	}

	@Override
	public Iterable<ModelPart> parts() {
		return ImmutableList.<ModelPart>of(
			this.cube,
			this.finBlue0,
			this.finBlue1,
			this.finTop0,
			this.finTop1,
			this.finSide0,
			this.finSide1,
			this.finSide2,
			this.finSide3,
			this.finBottom0,
			this.finBottom1
		);
	}

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j) {
		this.finBlue0.zRot = -0.2F + 0.4F * Mth.sin(h * 0.2F);
		this.finBlue1.zRot = 0.2F - 0.4F * Mth.sin(h * 0.2F);
	}
}
