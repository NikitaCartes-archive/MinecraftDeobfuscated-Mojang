package net.minecraft.client.model;

import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public abstract class EntityModel<T extends EntityRenderState> extends Model {
	public static final float MODEL_Y_OFFSET = -1.501F;

	protected EntityModel(ModelPart modelPart) {
		this(modelPart, RenderType::entityCutoutNoCull);
	}

	protected EntityModel(ModelPart modelPart, Function<ResourceLocation, RenderType> function) {
		super(modelPart, function);
	}

	public void setupAnim(T entityRenderState) {
		this.resetPose();
	}
}
