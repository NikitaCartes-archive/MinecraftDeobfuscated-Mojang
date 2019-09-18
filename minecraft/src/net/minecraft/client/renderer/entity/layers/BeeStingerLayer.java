package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public class BeeStingerLayer<T extends LivingEntity, M extends PlayerModel<T>> extends StuckInBodyLayer<T, M> {
	private static final ResourceLocation BEE_STINGER_LOCATION = new ResourceLocation("textures/entity/bee/bee_stinger.png");

	public BeeStingerLayer(LivingEntityRenderer<T, M> livingEntityRenderer) {
		super(livingEntityRenderer);
	}

	@Override
	protected int numStuck(T livingEntity) {
		return livingEntity.getStingerCount();
	}

	@Override
	protected void preRenderStuckItem(T livingEntity) {
		Lighting.turnOff();
		RenderSystem.pushMatrix();
		this.bindTexture(BEE_STINGER_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableLighting();
		RenderSystem.enableRescaleNormal();
	}

	@Override
	protected void postRenderStuckItem() {
		RenderSystem.disableRescaleNormal();
		RenderSystem.enableLighting();
		RenderSystem.popMatrix();
		Lighting.turnOn();
	}

	@Override
	protected void renderStuckItem(Entity entity, float f, float g, float h, float i) {
		RenderSystem.pushMatrix();
		float j = Mth.sqrt(f * f + h * h);
		float k = (float)(Math.atan2((double)f, (double)h) * 180.0F / (float)Math.PI);
		float l = (float)(Math.atan2((double)g, (double)j) * 180.0F / (float)Math.PI);
		RenderSystem.translatef(0.0F, 0.0F, 0.0F);
		RenderSystem.rotatef(k - 90.0F, 0.0F, 1.0F, 0.0F);
		RenderSystem.rotatef(l, 0.0F, 0.0F, 1.0F);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		float m = 0.0F;
		float n = 0.125F;
		float o = 0.0F;
		float p = 0.0625F;
		float q = 0.03125F;
		RenderSystem.rotatef(45.0F, 1.0F, 0.0F, 0.0F);
		RenderSystem.scalef(0.03125F, 0.03125F, 0.03125F);
		RenderSystem.translatef(2.5F, 0.0F, 0.0F);

		for (int r = 0; r < 4; r++) {
			RenderSystem.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
			bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
			bufferBuilder.vertex(-4.5, -1.0, 0.0).uv(0.0, 0.0).endVertex();
			bufferBuilder.vertex(4.5, -1.0, 0.0).uv(0.125, 0.0).endVertex();
			bufferBuilder.vertex(4.5, 1.0, 0.0).uv(0.125, 0.0625).endVertex();
			bufferBuilder.vertex(-4.5, 1.0, 0.0).uv(0.0, 0.0625).endVertex();
			tesselator.end();
		}

		RenderSystem.popMatrix();
	}

	@Override
	public boolean colorsOnDamage() {
		return false;
	}
}
