package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;

@Environment(EnvType.CLIENT)
public class BoatModel extends ListModel<Boat> {
	private final ModelPart[] paddles = new ModelPart[2];
	private final ModelPart waterPatch;
	private final ImmutableList<ModelPart> parts;

	public BoatModel() {
		ModelPart[] modelParts = new ModelPart[]{
			new ModelPart(this, 0, 0).setTexSize(128, 64),
			new ModelPart(this, 0, 19).setTexSize(128, 64),
			new ModelPart(this, 0, 27).setTexSize(128, 64),
			new ModelPart(this, 0, 35).setTexSize(128, 64),
			new ModelPart(this, 0, 43).setTexSize(128, 64)
		};
		int i = 32;
		int j = 6;
		int k = 20;
		int l = 4;
		int m = 28;
		modelParts[0].addBox(-14.0F, -9.0F, -3.0F, 28.0F, 16.0F, 3.0F, 0.0F);
		modelParts[0].setPos(0.0F, 3.0F, 1.0F);
		modelParts[1].addBox(-13.0F, -7.0F, -1.0F, 18.0F, 6.0F, 2.0F, 0.0F);
		modelParts[1].setPos(-15.0F, 4.0F, 4.0F);
		modelParts[2].addBox(-8.0F, -7.0F, -1.0F, 16.0F, 6.0F, 2.0F, 0.0F);
		modelParts[2].setPos(15.0F, 4.0F, 0.0F);
		modelParts[3].addBox(-14.0F, -7.0F, -1.0F, 28.0F, 6.0F, 2.0F, 0.0F);
		modelParts[3].setPos(0.0F, 4.0F, -9.0F);
		modelParts[4].addBox(-14.0F, -7.0F, -1.0F, 28.0F, 6.0F, 2.0F, 0.0F);
		modelParts[4].setPos(0.0F, 4.0F, 9.0F);
		modelParts[0].xRot = (float) (Math.PI / 2);
		modelParts[1].yRot = (float) (Math.PI * 3.0 / 2.0);
		modelParts[2].yRot = (float) (Math.PI / 2);
		modelParts[3].yRot = (float) Math.PI;
		this.paddles[0] = this.makePaddle(true);
		this.paddles[0].setPos(3.0F, -5.0F, 9.0F);
		this.paddles[1] = this.makePaddle(false);
		this.paddles[1].setPos(3.0F, -5.0F, -9.0F);
		this.paddles[1].yRot = (float) Math.PI;
		this.paddles[0].zRot = (float) (Math.PI / 16);
		this.paddles[1].zRot = (float) (Math.PI / 16);
		this.waterPatch = new ModelPart(this, 0, 0).setTexSize(128, 64);
		this.waterPatch.addBox(-14.0F, -9.0F, -3.0F, 28.0F, 16.0F, 3.0F, 0.0F);
		this.waterPatch.setPos(0.0F, -3.0F, 1.0F);
		this.waterPatch.xRot = (float) (Math.PI / 2);
		Builder<ModelPart> builder = ImmutableList.builder();
		builder.addAll(Arrays.asList(modelParts));
		builder.addAll(Arrays.asList(this.paddles));
		this.parts = builder.build();
	}

	public void setupAnim(Boat boat, float f, float g, float h, float i, float j) {
		this.animatePaddle(boat, 0, f);
		this.animatePaddle(boat, 1, f);
	}

	public ImmutableList<ModelPart> parts() {
		return this.parts;
	}

	public ModelPart waterPatch() {
		return this.waterPatch;
	}

	protected ModelPart makePaddle(boolean bl) {
		ModelPart modelPart = new ModelPart(this, 62, bl ? 0 : 20).setTexSize(128, 64);
		int i = 20;
		int j = 7;
		int k = 6;
		float f = -5.0F;
		modelPart.addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F);
		modelPart.addBox(bl ? -1.001F : 0.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F);
		return modelPart;
	}

	protected void animatePaddle(Boat boat, int i, float f) {
		float g = boat.getRowingTime(i, f);
		ModelPart modelPart = this.paddles[i];
		modelPart.xRot = (float)Mth.clampedLerp((float) (-Math.PI / 3), (float) (-Math.PI / 12), (double)((Mth.sin(-g) + 1.0F) / 2.0F));
		modelPart.yRot = (float)Mth.clampedLerp((float) (-Math.PI / 4), (float) (Math.PI / 4), (double)((Mth.sin(-g + 1.0F) + 1.0F) / 2.0F));
		if (i == 1) {
			modelPart.yRot = (float) Math.PI - modelPart.yRot;
		}
	}
}
