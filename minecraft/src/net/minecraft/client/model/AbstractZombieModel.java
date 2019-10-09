package net.minecraft.client.model;

import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Monster;

@Environment(EnvType.CLIENT)
public abstract class AbstractZombieModel<T extends Monster> extends HumanoidModel<T> {
	protected AbstractZombieModel(Function<ResourceLocation, RenderType> function, float f, float g, int i, int j) {
		super(function, f, g, i, j);
	}

	public void setupAnim(T monster, float f, float g, float h, float i, float j, float k) {
		super.setupAnim(monster, f, g, h, i, j, k);
		boolean bl = this.isAggressive(monster);
		float l = Mth.sin(this.attackTime * (float) Math.PI);
		float m = Mth.sin((1.0F - (1.0F - this.attackTime) * (1.0F - this.attackTime)) * (float) Math.PI);
		this.rightArm.zRot = 0.0F;
		this.leftArm.zRot = 0.0F;
		this.rightArm.yRot = -(0.1F - l * 0.6F);
		this.leftArm.yRot = 0.1F - l * 0.6F;
		float n = (float) -Math.PI / (bl ? 1.5F : 2.25F);
		this.rightArm.xRot = n;
		this.leftArm.xRot = n;
		this.rightArm.xRot += l * 1.2F - m * 0.4F;
		this.leftArm.xRot += l * 1.2F - m * 0.4F;
		this.rightArm.zRot = this.rightArm.zRot + Mth.cos(h * 0.09F) * 0.05F + 0.05F;
		this.leftArm.zRot = this.leftArm.zRot - (Mth.cos(h * 0.09F) * 0.05F + 0.05F);
		this.rightArm.xRot = this.rightArm.xRot + Mth.sin(h * 0.067F) * 0.05F;
		this.leftArm.xRot = this.leftArm.xRot - Mth.sin(h * 0.067F) * 0.05F;
	}

	public abstract boolean isAggressive(T monster);
}
