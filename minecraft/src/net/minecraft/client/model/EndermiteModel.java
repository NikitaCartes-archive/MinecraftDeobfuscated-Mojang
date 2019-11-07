package net.minecraft.client.model;

import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class EndermiteModel<T extends Entity> extends ListModel<T> {
	private static final int[][] BODY_SIZES = new int[][]{{4, 3, 2}, {6, 4, 5}, {3, 3, 1}, {1, 2, 1}};
	private static final int[][] BODY_TEXS = new int[][]{{0, 0}, {0, 5}, {0, 14}, {0, 18}};
	private static final int BODY_COUNT = BODY_SIZES.length;
	private final ModelPart[] bodyParts = new ModelPart[BODY_COUNT];

	public EndermiteModel() {
		float f = -3.5F;

		for (int i = 0; i < this.bodyParts.length; i++) {
			this.bodyParts[i] = new ModelPart(this, BODY_TEXS[i][0], BODY_TEXS[i][1]);
			this.bodyParts[i]
				.addBox((float)BODY_SIZES[i][0] * -0.5F, 0.0F, (float)BODY_SIZES[i][2] * -0.5F, (float)BODY_SIZES[i][0], (float)BODY_SIZES[i][1], (float)BODY_SIZES[i][2]);
			this.bodyParts[i].setPos(0.0F, (float)(24 - BODY_SIZES[i][1]), f);
			if (i < this.bodyParts.length - 1) {
				f += (float)(BODY_SIZES[i][2] + BODY_SIZES[i + 1][2]) * 0.5F;
			}
		}
	}

	@Override
	public Iterable<ModelPart> parts() {
		return Arrays.asList(this.bodyParts);
	}

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j) {
		for (int k = 0; k < this.bodyParts.length; k++) {
			this.bodyParts[k].yRot = Mth.cos(h * 0.9F + (float)k * 0.15F * (float) Math.PI) * (float) Math.PI * 0.01F * (float)(1 + Math.abs(k - 2));
			this.bodyParts[k].x = Mth.sin(h * 0.9F + (float)k * 0.15F * (float) Math.PI) * (float) Math.PI * 0.1F * (float)Math.abs(k - 2);
		}
	}
}
