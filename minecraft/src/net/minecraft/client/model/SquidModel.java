package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class SquidModel<T extends Entity> extends ListModel<T> {
	private final ModelPart body;
	private final ModelPart[] tentacles = new ModelPart[8];
	private final ImmutableList<ModelPart> parts;

	public SquidModel() {
		int i = -16;
		this.body = new ModelPart(this, 0, 0);
		this.body.addBox(-6.0F, -8.0F, -6.0F, 12.0F, 16.0F, 12.0F);
		this.body.y += 8.0F;

		for (int j = 0; j < this.tentacles.length; j++) {
			this.tentacles[j] = new ModelPart(this, 48, 0);
			double d = (double)j * Math.PI * 2.0 / (double)this.tentacles.length;
			float f = (float)Math.cos(d) * 5.0F;
			float g = (float)Math.sin(d) * 5.0F;
			this.tentacles[j].addBox(-1.0F, 0.0F, -1.0F, 2.0F, 18.0F, 2.0F);
			this.tentacles[j].x = f;
			this.tentacles[j].z = g;
			this.tentacles[j].y = 15.0F;
			d = (double)j * Math.PI * -2.0 / (double)this.tentacles.length + (Math.PI / 2);
			this.tentacles[j].yRot = (float)d;
		}

		Builder<ModelPart> builder = ImmutableList.builder();
		builder.add(this.body);
		builder.addAll(Arrays.asList(this.tentacles));
		this.parts = builder.build();
	}

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j) {
		for (ModelPart modelPart : this.tentacles) {
			modelPart.xRot = h;
		}
	}

	@Override
	public Iterable<ModelPart> parts() {
		return this.parts;
	}
}
