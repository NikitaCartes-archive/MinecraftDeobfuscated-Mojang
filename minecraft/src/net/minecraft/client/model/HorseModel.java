package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.EquineRenderState;

@Environment(EnvType.CLIENT)
public class HorseModel extends AbstractEquineModel<EquineRenderState> {
	public HorseModel(ModelPart modelPart) {
		super(modelPart);
	}
}
