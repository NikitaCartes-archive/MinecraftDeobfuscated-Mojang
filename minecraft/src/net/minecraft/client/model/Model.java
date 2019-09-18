package net.minecraft.client.model;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;

@Environment(EnvType.CLIENT)
public class Model implements Consumer<ModelPart> {
	public int texWidth = 64;
	public int texHeight = 32;

	public void accept(ModelPart modelPart) {
	}
}
