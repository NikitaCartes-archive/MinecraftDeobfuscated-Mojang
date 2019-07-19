package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.dragon.DragonModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

@Environment(EnvType.CLIENT)
public class EnderDragonDeathLayer extends RenderLayer<EnderDragon, DragonModel> {
	public EnderDragonDeathLayer(RenderLayerParent<EnderDragon, DragonModel> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(EnderDragon enderDragon, float f, float g, float h, float i, float j, float k, float l) {
		if (enderDragon.dragonDeathTime > 0) {
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder bufferBuilder = tesselator.getBuilder();
			Lighting.turnOff();
			float m = ((float)enderDragon.dragonDeathTime + h) / 200.0F;
			float n = 0.0F;
			if (m > 0.8F) {
				n = (m - 0.8F) / 0.2F;
			}

			Random random = new Random(432L);
			GlStateManager.disableTexture();
			GlStateManager.shadeModel(7425);
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
			GlStateManager.disableAlphaTest();
			GlStateManager.enableCull();
			GlStateManager.depthMask(false);
			GlStateManager.pushMatrix();
			GlStateManager.translatef(0.0F, -1.0F, -2.0F);

			for (int o = 0; (float)o < (m + m * m) / 2.0F * 60.0F; o++) {
				GlStateManager.rotatef(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
				GlStateManager.rotatef(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
				GlStateManager.rotatef(random.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
				GlStateManager.rotatef(random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
				GlStateManager.rotatef(random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
				GlStateManager.rotatef(random.nextFloat() * 360.0F + m * 90.0F, 0.0F, 0.0F, 1.0F);
				float p = random.nextFloat() * 20.0F + 5.0F + n * 10.0F;
				float q = random.nextFloat() * 2.0F + 1.0F + n * 2.0F;
				bufferBuilder.begin(6, DefaultVertexFormat.POSITION_COLOR);
				bufferBuilder.vertex(0.0, 0.0, 0.0).color(255, 255, 255, (int)(255.0F * (1.0F - n))).endVertex();
				bufferBuilder.vertex(-0.866 * (double)q, (double)p, (double)(-0.5F * q)).color(255, 0, 255, 0).endVertex();
				bufferBuilder.vertex(0.866 * (double)q, (double)p, (double)(-0.5F * q)).color(255, 0, 255, 0).endVertex();
				bufferBuilder.vertex(0.0, (double)p, (double)(1.0F * q)).color(255, 0, 255, 0).endVertex();
				bufferBuilder.vertex(-0.866 * (double)q, (double)p, (double)(-0.5F * q)).color(255, 0, 255, 0).endVertex();
				tesselator.end();
			}

			GlStateManager.popMatrix();
			GlStateManager.depthMask(true);
			GlStateManager.disableCull();
			GlStateManager.disableBlend();
			GlStateManager.shadeModel(7424);
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.enableTexture();
			GlStateManager.enableAlphaTest();
			Lighting.turnOn();
		}
	}

	@Override
	public boolean colorsOnDamage() {
		return false;
	}
}
