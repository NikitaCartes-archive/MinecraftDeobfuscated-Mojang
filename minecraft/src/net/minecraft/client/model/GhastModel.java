package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class GhastModel<T extends Entity> extends ListModel<T> {
	private final ModelPart[] tentacles = new ModelPart[9];
	private final ImmutableList<ModelPart> parts;

	public GhastModel() {
		Builder<ModelPart> builder = ImmutableList.builder();
		ModelPart modelPart = new ModelPart(this, 0, 0);
		modelPart.addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F);
		modelPart.y = 17.6F;
		builder.add(modelPart);
		Random random = new Random(1660L);

		for (int i = 0; i < this.tentacles.length; i++) {
			this.tentacles[i] = new ModelPart(this, 0, 0);
			float f = (((float)(i % 3) - (float)(i / 3 % 2) * 0.5F + 0.25F) / 2.0F * 2.0F - 1.0F) * 5.0F;
			float g = ((float)(i / 3) / 2.0F * 2.0F - 1.0F) * 5.0F;
			int j = random.nextInt(7) + 8;
			this.tentacles[i].addBox(-1.0F, 0.0F, -1.0F, 2.0F, (float)j, 2.0F);
			this.tentacles[i].x = f;
			this.tentacles[i].z = g;
			this.tentacles[i].y = 24.6F;
			builder.add(this.tentacles[i]);
		}

		this.parts = builder.build();
	}

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j) {
		for (int k = 0; k < this.tentacles.length; k++) {
			this.tentacles[k].xRot = 0.2F * Mth.sin(h * 0.3F + (float)k) + 0.4F;
		}
	}

	@Override
	public Iterable<ModelPart> parts() {
		return this.parts;
	}
}
