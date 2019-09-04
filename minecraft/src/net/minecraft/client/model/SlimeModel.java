package net.minecraft.client.model;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public class SlimeModel<T extends Entity> extends EntityModel<T> {
	private final ModelPart cube;
	private final ModelPart eye0;
	private final ModelPart eye1;
	private final ModelPart mouth;

	public SlimeModel(int i) {
		if (i > 0) {
			this.cube = new ModelPart(this, 0, i);
			this.cube.addBox(-3.0F, 17.0F, -3.0F, 6, 6, 6);
			this.eye0 = new ModelPart(this, 32, 0);
			this.eye0.addBox(-3.25F, 18.0F, -3.5F, 2, 2, 2);
			this.eye1 = new ModelPart(this, 32, 4);
			this.eye1.addBox(1.25F, 18.0F, -3.5F, 2, 2, 2);
			this.mouth = new ModelPart(this, 32, 8);
			this.mouth.addBox(0.0F, 21.0F, -3.5F, 1, 1, 1);
		} else {
			this.cube = new ModelPart(this, 0, i);
			this.cube.addBox(-4.0F, 16.0F, -4.0F, 8, 8, 8);
			this.eye0 = null;
			this.eye1 = null;
			this.mouth = null;
		}
	}

	@Override
	public void render(T entity, float f, float g, float h, float i, float j, float k) {
		this.setupAnim(entity, f, g, h, i, j, k);
		RenderSystem.translatef(0.0F, 0.001F, 0.0F);
		this.cube.render(k);
		if (this.eye0 != null) {
			this.eye0.render(k);
			this.eye1.render(k);
			this.mouth.render(k);
		}
	}
}
