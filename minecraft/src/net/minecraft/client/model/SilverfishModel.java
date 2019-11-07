package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class SilverfishModel<T extends Entity> extends ListModel<T> {
	private final ModelPart[] bodyParts;
	private final ModelPart[] bodyLayers;
	private final ImmutableList<ModelPart> parts;
	private final float[] zPlacement = new float[7];
	private static final int[][] BODY_SIZES = new int[][]{{3, 2, 2}, {4, 3, 2}, {6, 4, 3}, {3, 3, 3}, {2, 2, 3}, {2, 1, 2}, {1, 1, 2}};
	private static final int[][] BODY_TEXS = new int[][]{{0, 0}, {0, 4}, {0, 9}, {0, 16}, {0, 22}, {11, 0}, {13, 4}};

	public SilverfishModel() {
		this.bodyParts = new ModelPart[7];
		float f = -3.5F;

		for (int i = 0; i < this.bodyParts.length; i++) {
			this.bodyParts[i] = new ModelPart(this, BODY_TEXS[i][0], BODY_TEXS[i][1]);
			this.bodyParts[i]
				.addBox((float)BODY_SIZES[i][0] * -0.5F, 0.0F, (float)BODY_SIZES[i][2] * -0.5F, (float)BODY_SIZES[i][0], (float)BODY_SIZES[i][1], (float)BODY_SIZES[i][2]);
			this.bodyParts[i].setPos(0.0F, (float)(24 - BODY_SIZES[i][1]), f);
			this.zPlacement[i] = f;
			if (i < this.bodyParts.length - 1) {
				f += (float)(BODY_SIZES[i][2] + BODY_SIZES[i + 1][2]) * 0.5F;
			}
		}

		this.bodyLayers = new ModelPart[3];
		this.bodyLayers[0] = new ModelPart(this, 20, 0);
		this.bodyLayers[0].addBox(-5.0F, 0.0F, (float)BODY_SIZES[2][2] * -0.5F, 10.0F, 8.0F, (float)BODY_SIZES[2][2]);
		this.bodyLayers[0].setPos(0.0F, 16.0F, this.zPlacement[2]);
		this.bodyLayers[1] = new ModelPart(this, 20, 11);
		this.bodyLayers[1].addBox(-3.0F, 0.0F, (float)BODY_SIZES[4][2] * -0.5F, 6.0F, 4.0F, (float)BODY_SIZES[4][2]);
		this.bodyLayers[1].setPos(0.0F, 20.0F, this.zPlacement[4]);
		this.bodyLayers[2] = new ModelPart(this, 20, 18);
		this.bodyLayers[2].addBox(-3.0F, 0.0F, (float)BODY_SIZES[4][2] * -0.5F, 6.0F, 5.0F, (float)BODY_SIZES[1][2]);
		this.bodyLayers[2].setPos(0.0F, 19.0F, this.zPlacement[1]);
		Builder<ModelPart> builder = ImmutableList.builder();
		builder.addAll(Arrays.asList(this.bodyParts));
		builder.addAll(Arrays.asList(this.bodyLayers));
		this.parts = builder.build();
	}

	public ImmutableList<ModelPart> parts() {
		return this.parts;
	}

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j) {
		for (int k = 0; k < this.bodyParts.length; k++) {
			this.bodyParts[k].yRot = Mth.cos(h * 0.9F + (float)k * 0.15F * (float) Math.PI) * (float) Math.PI * 0.05F * (float)(1 + Math.abs(k - 2));
			this.bodyParts[k].x = Mth.sin(h * 0.9F + (float)k * 0.15F * (float) Math.PI) * (float) Math.PI * 0.2F * (float)Math.abs(k - 2);
		}

		this.bodyLayers[0].yRot = this.bodyParts[2].yRot;
		this.bodyLayers[1].yRot = this.bodyParts[4].yRot;
		this.bodyLayers[1].x = this.bodyParts[4].x;
		this.bodyLayers[2].yRot = this.bodyParts[1].yRot;
		this.bodyLayers[2].x = this.bodyParts[1].x;
	}
}
