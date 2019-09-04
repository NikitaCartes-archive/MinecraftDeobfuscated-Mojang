package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BellModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BellBlockEntity;

@Environment(EnvType.CLIENT)
public class BellRenderer extends BlockEntityRenderer<BellBlockEntity> {
	private static final ResourceLocation BELL_RESOURCE_LOCATION = new ResourceLocation("textures/entity/bell/bell_body.png");
	private final BellModel bellModel = new BellModel();

	public void render(BellBlockEntity bellBlockEntity, double d, double e, double f, float g, int i) {
		RenderSystem.pushMatrix();
		RenderSystem.enableRescaleNormal();
		this.bindTexture(BELL_RESOURCE_LOCATION);
		RenderSystem.translatef((float)d, (float)e, (float)f);
		float h = (float)bellBlockEntity.ticks + g;
		float j = 0.0F;
		float k = 0.0F;
		if (bellBlockEntity.shaking) {
			float l = Mth.sin(h / (float) Math.PI) / (4.0F + h / 3.0F);
			if (bellBlockEntity.clickDirection == Direction.NORTH) {
				j = -l;
			} else if (bellBlockEntity.clickDirection == Direction.SOUTH) {
				j = l;
			} else if (bellBlockEntity.clickDirection == Direction.EAST) {
				k = -l;
			} else if (bellBlockEntity.clickDirection == Direction.WEST) {
				k = l;
			}
		}

		this.bellModel.render(j, k, 0.0625F);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.popMatrix();
	}
}
