package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.ShulkerRenderer;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.DyeColor;

@Environment(EnvType.CLIENT)
public class ShulkerHeadLayer extends RenderLayer<Shulker, ShulkerModel<Shulker>> {
	public ShulkerHeadLayer(RenderLayerParent<Shulker, ShulkerModel<Shulker>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(Shulker shulker, float f, float g, float h, float i, float j, float k, float l) {
		RenderSystem.pushMatrix();
		switch (shulker.getAttachFace()) {
			case DOWN:
			default:
				break;
			case EAST:
				RenderSystem.rotatef(90.0F, 0.0F, 0.0F, 1.0F);
				RenderSystem.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
				RenderSystem.translatef(1.0F, -1.0F, 0.0F);
				RenderSystem.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
				break;
			case WEST:
				RenderSystem.rotatef(-90.0F, 0.0F, 0.0F, 1.0F);
				RenderSystem.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
				RenderSystem.translatef(-1.0F, -1.0F, 0.0F);
				RenderSystem.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
				break;
			case NORTH:
				RenderSystem.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
				RenderSystem.translatef(0.0F, -1.0F, -1.0F);
				break;
			case SOUTH:
				RenderSystem.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
				RenderSystem.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
				RenderSystem.translatef(0.0F, -1.0F, 1.0F);
				break;
			case UP:
				RenderSystem.rotatef(180.0F, 1.0F, 0.0F, 0.0F);
				RenderSystem.translatef(0.0F, -2.0F, 0.0F);
		}

		ModelPart modelPart = this.getParentModel().getHead();
		modelPart.yRot = j * (float) (Math.PI / 180.0);
		modelPart.xRot = k * (float) (Math.PI / 180.0);
		DyeColor dyeColor = shulker.getColor();
		if (dyeColor == null) {
			this.bindTexture(ShulkerRenderer.DEFAULT_TEXTURE_LOCATION);
		} else {
			this.bindTexture(ShulkerRenderer.TEXTURE_LOCATION[dyeColor.getId()]);
		}

		modelPart.render(l);
		RenderSystem.popMatrix();
	}

	@Override
	public boolean colorsOnDamage() {
		return false;
	}
}
