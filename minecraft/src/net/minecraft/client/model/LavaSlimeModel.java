package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Slime;

@Environment(EnvType.CLIENT)
public class LavaSlimeModel<T extends Slime> extends EntityModel<T> {
	private final ModelPart[] bodyCubes = new ModelPart[8];
	private final ModelPart insideCube;

	public LavaSlimeModel() {
		for (int i = 0; i < this.bodyCubes.length; i++) {
			int j = 0;
			int k = i;
			if (i == 2) {
				j = 24;
				k = 10;
			} else if (i == 3) {
				j = 24;
				k = 19;
			}

			this.bodyCubes[i] = new ModelPart(this, j, k);
			this.bodyCubes[i].addBox(-4.0F, (float)(16 + i), -4.0F, 8.0F, 1.0F, 8.0F);
		}

		this.insideCube = new ModelPart(this, 0, 16);
		this.insideCube.addBox(-2.0F, 18.0F, -2.0F, 4.0F, 4.0F, 4.0F);
	}

	public void prepareMobModel(T slime, float f, float g, float h) {
		float i = Mth.lerp(h, slime.oSquish, slime.squish);
		if (i < 0.0F) {
			i = 0.0F;
		}

		for (int j = 0; j < this.bodyCubes.length; j++) {
			this.bodyCubes[j].y = (float)(-(4 - j)) * i * 1.7F;
		}
	}

	public void render(T slime, float f, float g, float h, float i, float j, float k) {
		this.setupAnim(slime, f, g, h, i, j, k);
		this.insideCube.render(k);

		for (ModelPart modelPart : this.bodyCubes) {
			modelPart.render(k);
		}
	}
}
