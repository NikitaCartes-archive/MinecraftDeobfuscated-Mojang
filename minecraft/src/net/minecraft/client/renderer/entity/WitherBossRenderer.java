package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.WitherBossModel;
import net.minecraft.client.renderer.entity.layers.WitherArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.boss.wither.WitherBoss;

@Environment(EnvType.CLIENT)
public class WitherBossRenderer extends MobRenderer<WitherBoss, WitherBossModel<WitherBoss>> {
	private static final ResourceLocation WITHER_INVULNERABLE_LOCATION = new ResourceLocation("textures/entity/wither/wither_invulnerable.png");
	private static final ResourceLocation WITHER_LOCATION = new ResourceLocation("textures/entity/wither/wither.png");

	public WitherBossRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new WitherBossModel<>(0.0F), 1.0F);
		this.addLayer(new WitherArmorLayer(this));
	}

	protected int getBlockLightLevel(WitherBoss witherBoss, float f) {
		return 15;
	}

	public ResourceLocation getTextureLocation(WitherBoss witherBoss) {
		int i = witherBoss.getInvulnerableTicks();
		return i > 0 && (i > 80 || i / 5 % 2 != 1) ? WITHER_INVULNERABLE_LOCATION : WITHER_LOCATION;
	}

	protected void scale(WitherBoss witherBoss, PoseStack poseStack, float f) {
		float g = 2.0F;
		int i = witherBoss.getInvulnerableTicks();
		if (i > 0) {
			g -= ((float)i - f) / 220.0F * 0.5F;
		}

		poseStack.scale(g, g, g);
	}
}
