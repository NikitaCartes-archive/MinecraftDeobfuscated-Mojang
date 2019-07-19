package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.renderer.entity.layers.CatCollarLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

@Environment(EnvType.CLIENT)
public class CatRenderer extends MobRenderer<Cat, CatModel<Cat>> {
	public CatRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new CatModel<>(0.0F), 0.4F);
		this.addLayer(new CatCollarLayer(this));
	}

	@Nullable
	protected ResourceLocation getTextureLocation(Cat cat) {
		return cat.getResourceLocation();
	}

	protected void scale(Cat cat, float f) {
		super.scale(cat, f);
		GlStateManager.scalef(0.8F, 0.8F, 0.8F);
	}

	protected void setupRotations(Cat cat, float f, float g, float h) {
		super.setupRotations(cat, f, g, h);
		float i = cat.getLieDownAmount(h);
		if (i > 0.0F) {
			GlStateManager.translatef(0.4F * i, 0.15F * i, 0.1F * i);
			GlStateManager.rotatef(Mth.rotLerp(i, 0.0F, 90.0F), 0.0F, 0.0F, 1.0F);
			BlockPos blockPos = new BlockPos(cat);

			for (Player player : cat.level.getEntitiesOfClass(Player.class, new AABB(blockPos).inflate(2.0, 2.0, 2.0))) {
				if (player.isSleeping()) {
					GlStateManager.translatef(0.15F * i, 0.0F, 0.0F);
					break;
				}
			}
		}
	}
}
