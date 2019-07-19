package net.minecraft.client.model;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;

@Environment(EnvType.CLIENT)
public class Model {
	public final List<ModelPart> cubes = Lists.<ModelPart>newArrayList();
	public int texWidth = 64;
	public int texHeight = 32;

	public ModelPart getRandomModelPart(Random random) {
		return (ModelPart)this.cubes.get(random.nextInt(this.cubes.size()));
	}
}
