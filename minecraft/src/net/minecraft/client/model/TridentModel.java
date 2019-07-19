package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class TridentModel extends Model {
	public static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/trident.png");
	private final ModelPart pole;

	public TridentModel() {
		this.texWidth = 32;
		this.texHeight = 32;
		this.pole = new ModelPart(this, 0, 0);
		this.pole.addBox(-0.5F, -4.0F, -0.5F, 1, 31, 1, 0.0F);
		ModelPart modelPart = new ModelPart(this, 4, 0);
		modelPart.addBox(-1.5F, 0.0F, -0.5F, 3, 2, 1);
		this.pole.addChild(modelPart);
		ModelPart modelPart2 = new ModelPart(this, 4, 3);
		modelPart2.addBox(-2.5F, -3.0F, -0.5F, 1, 4, 1);
		this.pole.addChild(modelPart2);
		ModelPart modelPart3 = new ModelPart(this, 4, 3);
		modelPart3.mirror = true;
		modelPart3.addBox(1.5F, -3.0F, -0.5F, 1, 4, 1);
		this.pole.addChild(modelPart3);
	}

	public void render() {
		this.pole.render(0.0625F);
	}
}
