package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.renderer.entity.state.CatRenderState;

@Environment(EnvType.CLIENT)
public class CatModel extends FelineModel<CatRenderState> {
	public static final MeshTransformer CAT_TRANSFORMER = MeshTransformer.scaling(0.8F);

	public CatModel(ModelPart modelPart) {
		super(modelPart);
	}
}
