package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.renderer.entity.layers.DrownedOuterLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Zombie;

@Environment(EnvType.CLIENT)
public class DrownedRenderer extends AbstractZombieRenderer<Drowned, DrownedModel<Drowned>> {
	private static final ResourceLocation DROWNED_LOCATION = new ResourceLocation("textures/entity/zombie/drowned.png");

	public DrownedRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new DrownedModel<>(0.0F, 0.0F, 64, 64), new DrownedModel<>(0.5F, true), new DrownedModel<>(1.0F, true));
		this.addLayer(new DrownedOuterLayer<>(this));
	}

	@Nullable
	@Override
	protected ResourceLocation getTextureLocation(Zombie zombie) {
		return DROWNED_LOCATION;
	}

	protected void setupRotations(Drowned drowned, float f, float g, float h) {
		float i = drowned.getSwimAmount(h);
		super.setupRotations(drowned, f, g, h);
		if (i > 0.0F) {
			RenderSystem.rotatef(Mth.lerp(i, drowned.xRot, -10.0F - drowned.xRot), 1.0F, 0.0F, 0.0F);
		}
	}
}
