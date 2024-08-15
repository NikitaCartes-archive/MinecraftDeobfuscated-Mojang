package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.FelineRenderState;

@Environment(EnvType.CLIENT)
public class OcelotModel extends FelineModel<FelineRenderState> {
	public OcelotModel(ModelPart modelPart) {
		super(modelPart);
	}
}
