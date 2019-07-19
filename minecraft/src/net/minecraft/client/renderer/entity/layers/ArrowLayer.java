package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.Cube;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;

@Environment(EnvType.CLIENT)
public class ArrowLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
	private final EntityRenderDispatcher dispatcher;

	public ArrowLayer(LivingEntityRenderer<T, M> livingEntityRenderer) {
		super(livingEntityRenderer);
		this.dispatcher = livingEntityRenderer.getDispatcher();
	}

	public void render(T livingEntity, float f, float g, float h, float i, float j, float k, float l) {
		int m = livingEntity.getArrowCount();
		if (m > 0) {
			Entity entity = new Arrow(livingEntity.level, livingEntity.x, livingEntity.y, livingEntity.z);
			Random random = new Random((long)livingEntity.getId());
			Lighting.turnOff();

			for (int n = 0; n < m; n++) {
				GlStateManager.pushMatrix();
				ModelPart modelPart = this.getParentModel().getRandomModelPart(random);
				Cube cube = (Cube)modelPart.cubes.get(random.nextInt(modelPart.cubes.size()));
				modelPart.translateTo(0.0625F);
				float o = random.nextFloat();
				float p = random.nextFloat();
				float q = random.nextFloat();
				float r = Mth.lerp(o, cube.minX, cube.maxX) / 16.0F;
				float s = Mth.lerp(p, cube.minY, cube.maxY) / 16.0F;
				float t = Mth.lerp(q, cube.minZ, cube.maxZ) / 16.0F;
				GlStateManager.translatef(r, s, t);
				o = o * 2.0F - 1.0F;
				p = p * 2.0F - 1.0F;
				q = q * 2.0F - 1.0F;
				o *= -1.0F;
				p *= -1.0F;
				q *= -1.0F;
				float u = Mth.sqrt(o * o + q * q);
				entity.yRot = (float)(Math.atan2((double)o, (double)q) * 180.0F / (float)Math.PI);
				entity.xRot = (float)(Math.atan2((double)p, (double)u) * 180.0F / (float)Math.PI);
				entity.yRotO = entity.yRot;
				entity.xRotO = entity.xRot;
				double d = 0.0;
				double e = 0.0;
				double v = 0.0;
				this.dispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, h, false);
				GlStateManager.popMatrix();
			}

			Lighting.turnOn();
		}
	}

	@Override
	public boolean colorsOnDamage() {
		return false;
	}
}
