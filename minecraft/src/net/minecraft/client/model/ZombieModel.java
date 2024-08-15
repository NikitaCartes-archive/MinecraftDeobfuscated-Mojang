package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;

@Environment(EnvType.CLIENT)
public class ZombieModel<S extends ZombieRenderState> extends AbstractZombieModel<S> {
	public ZombieModel(ModelPart modelPart) {
		super(modelPart);
	}
}
