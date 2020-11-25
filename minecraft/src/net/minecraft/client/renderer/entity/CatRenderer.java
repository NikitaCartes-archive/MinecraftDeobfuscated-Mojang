package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.CatCollarLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

@Environment(EnvType.CLIENT)
public class CatRenderer extends MobRenderer<Cat, CatModel<Cat>> {
	public CatRenderer(EntityRendererProvider.Context context) {
		super(context, new CatModel<>(context.bakeLayer(ModelLayers.CAT)), 0.4F);
		this.addLayer(new CatCollarLayer(this, context.getModelSet()));
	}

	public ResourceLocation getTextureLocation(Cat cat) {
		return cat.getResourceLocation();
	}

	protected void scale(Cat cat, PoseStack poseStack, float f) {
		super.scale(cat, poseStack, f);
		poseStack.scale(0.8F, 0.8F, 0.8F);
	}

	protected void setupRotations(Cat cat, PoseStack poseStack, float f, float g, float h) {
		super.setupRotations(cat, poseStack, f, g, h);
		float i = cat.getLieDownAmount(h);
		if (i > 0.0F) {
			poseStack.translate((double)(0.4F * i), (double)(0.15F * i), (double)(0.1F * i));
			poseStack.mulPose(Vector3f.ZP.rotationDegrees(Mth.rotLerp(i, 0.0F, 90.0F)));
			BlockPos blockPos = cat.blockPosition();

			for (Player player : cat.level.getEntitiesOfClass(Player.class, new AABB(blockPos).inflate(2.0, 2.0, 2.0))) {
				if (player.isSleeping()) {
					poseStack.translate((double)(0.15F * i), 0.0, 0.0);
					break;
				}
			}
		}
	}
}
