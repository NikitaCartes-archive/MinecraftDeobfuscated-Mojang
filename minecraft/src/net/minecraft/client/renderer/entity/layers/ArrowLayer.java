package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.Lighting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;

@Environment(EnvType.CLIENT)
public class ArrowLayer<T extends LivingEntity, M extends EntityModel<T>> extends StuckInBodyLayer<T, M> {
	private final EntityRenderDispatcher dispatcher;
	private Arrow arrow;

	public ArrowLayer(LivingEntityRenderer<T, M> livingEntityRenderer) {
		super(livingEntityRenderer);
		this.dispatcher = livingEntityRenderer.getDispatcher();
	}

	@Override
	protected void preRenderStuckItem(T livingEntity) {
		Lighting.turnOff();
		this.arrow = new Arrow(livingEntity.level, livingEntity.x, livingEntity.y, livingEntity.z);
	}

	@Override
	protected int numStuck(T livingEntity) {
		return livingEntity.getArrowCount();
	}

	@Override
	protected void renderStuckItem(Entity entity, float f, float g, float h, float i) {
		float j = Mth.sqrt(f * f + h * h);
		this.arrow.yRot = (float)(Math.atan2((double)f, (double)h) * 180.0F / (float)Math.PI);
		this.arrow.xRot = (float)(Math.atan2((double)g, (double)j) * 180.0F / (float)Math.PI);
		this.arrow.yRotO = this.arrow.yRot;
		this.arrow.xRotO = this.arrow.xRot;
		this.dispatcher.render(this.arrow, 0.0, 0.0, 0.0, 0.0F, i, false);
	}

	@Override
	public boolean colorsOnDamage() {
		return false;
	}
}
