package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.WitherBossModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.WitherArmorLayer;
import net.minecraft.client.renderer.entity.state.WitherRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.wither.WitherBoss;

@Environment(EnvType.CLIENT)
public class WitherBossRenderer extends MobRenderer<WitherBoss, WitherRenderState, WitherBossModel> {
	private static final ResourceLocation WITHER_INVULNERABLE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/wither/wither_invulnerable.png");
	private static final ResourceLocation WITHER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/wither/wither.png");

	public WitherBossRenderer(EntityRendererProvider.Context context) {
		super(context, new WitherBossModel(context.bakeLayer(ModelLayers.WITHER)), 1.0F);
		this.addLayer(new WitherArmorLayer(this, context.getModelSet()));
	}

	protected int getBlockLightLevel(WitherBoss witherBoss, BlockPos blockPos) {
		return 15;
	}

	public ResourceLocation getTextureLocation(WitherRenderState witherRenderState) {
		int i = Mth.floor(witherRenderState.invulnerableTicks);
		return i > 0 && (i > 80 || i / 5 % 2 != 1) ? WITHER_INVULNERABLE_LOCATION : WITHER_LOCATION;
	}

	public WitherRenderState createRenderState() {
		return new WitherRenderState();
	}

	protected void scale(WitherRenderState witherRenderState, PoseStack poseStack) {
		float f = 2.0F;
		if (witherRenderState.invulnerableTicks > 0.0F) {
			f -= witherRenderState.invulnerableTicks / 220.0F * 0.5F;
		}

		poseStack.scale(f, f, f);
	}

	public void extractRenderState(WitherBoss witherBoss, WitherRenderState witherRenderState, float f) {
		super.extractRenderState(witherBoss, witherRenderState, f);
		int i = witherBoss.getInvulnerableTicks();
		witherRenderState.invulnerableTicks = i > 0 ? (float)i - f : 0.0F;
		System.arraycopy(witherBoss.getHeadXRots(), 0, witherRenderState.xHeadRots, 0, witherRenderState.xHeadRots.length);
		System.arraycopy(witherBoss.getHeadYRots(), 0, witherRenderState.yHeadRots, 0, witherRenderState.yHeadRots.length);
		witherRenderState.isPowered = witherBoss.isPowered();
	}
}
