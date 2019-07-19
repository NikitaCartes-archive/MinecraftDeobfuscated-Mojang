package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;

@Environment(EnvType.CLIENT)
public class BannerModel extends Model {
	private final ModelPart flag;
	private final ModelPart pole;
	private final ModelPart bar;

	public BannerModel() {
		this.texWidth = 64;
		this.texHeight = 64;
		this.flag = new ModelPart(this, 0, 0);
		this.flag.addBox(-10.0F, 0.0F, -2.0F, 20, 40, 1, 0.0F);
		this.pole = new ModelPart(this, 44, 0);
		this.pole.addBox(-1.0F, -30.0F, -1.0F, 2, 42, 2, 0.0F);
		this.bar = new ModelPart(this, 0, 42);
		this.bar.addBox(-10.0F, -32.0F, -1.0F, 20, 2, 2, 0.0F);
	}

	public void render() {
		this.flag.y = -32.0F;
		this.flag.render(0.0625F);
		this.pole.render(0.0625F);
		this.bar.render(0.0625F);
	}

	public ModelPart getPole() {
		return this.pole;
	}

	public ModelPart getFlag() {
		return this.flag;
	}
}
