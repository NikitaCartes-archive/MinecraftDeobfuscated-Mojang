package net.minecraft.client.model;

import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
public abstract class EntityModel<T extends Entity> extends Model {
	public float attackTime;
	public boolean riding;
	public boolean young = true;

	protected EntityModel() {
		this(RenderType::entityCutoutNoCull);
	}

	protected EntityModel(Function<ResourceLocation, RenderType> function) {
		super(function);
	}

	public abstract void setupAnim(T entity, float f, float g, float h, float i, float j);

	public void prepareMobModel(T entity, float f, float g, float h) {
	}

	public void copyPropertiesTo(EntityModel<T> entityModel) {
		entityModel.attackTime = this.attackTime;
		entityModel.riding = this.riding;
		entityModel.young = this.young;
	}
}
