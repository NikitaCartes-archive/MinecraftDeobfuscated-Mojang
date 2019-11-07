package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class SlimeModel<T extends Entity> extends ListModel<T> {
	private final ModelPart cube;
	private final ModelPart eye0;
	private final ModelPart eye1;
	private final ModelPart mouth;

	public SlimeModel(int i) {
		this.cube = new ModelPart(this, 0, i);
		this.eye0 = new ModelPart(this, 32, 0);
		this.eye1 = new ModelPart(this, 32, 4);
		this.mouth = new ModelPart(this, 32, 8);
		if (i > 0) {
			this.cube.addBox(-3.0F, 17.0F, -3.0F, 6.0F, 6.0F, 6.0F);
			this.eye0.addBox(-3.25F, 18.0F, -3.5F, 2.0F, 2.0F, 2.0F);
			this.eye1.addBox(1.25F, 18.0F, -3.5F, 2.0F, 2.0F, 2.0F);
			this.mouth.addBox(0.0F, 21.0F, -3.5F, 1.0F, 1.0F, 1.0F);
		} else {
			this.cube.addBox(-4.0F, 16.0F, -4.0F, 8.0F, 8.0F, 8.0F);
		}
	}

	@Override
	public void setupAnim(T entity, float f, float g, float h, float i, float j) {
	}

	@Override
	public Iterable<ModelPart> parts() {
		return ImmutableList.<ModelPart>of(this.cube, this.eye0, this.eye1, this.mouth);
	}
}
