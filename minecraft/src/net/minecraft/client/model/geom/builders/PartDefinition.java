package net.minecraft.client.model.geom.builders;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;

@Environment(EnvType.CLIENT)
public class PartDefinition {
	private final List<CubeDefinition> cubes;
	private final PartPose partPose;
	private final Map<String, PartDefinition> children = Maps.<String, PartDefinition>newHashMap();

	PartDefinition(List<CubeDefinition> list, PartPose partPose) {
		this.cubes = list;
		this.partPose = partPose;
	}

	public PartDefinition addOrReplaceChild(String string, CubeListBuilder cubeListBuilder, PartPose partPose) {
		PartDefinition partDefinition = new PartDefinition(cubeListBuilder.getCubes(), partPose);
		PartDefinition partDefinition2 = (PartDefinition)this.children.put(string, partDefinition);
		if (partDefinition2 != null) {
			partDefinition.children.putAll(partDefinition2.children);
		}

		return partDefinition;
	}

	public ModelPart bake(int i, int j) {
		Object2ObjectArrayMap<String, ModelPart> object2ObjectArrayMap = (Object2ObjectArrayMap<String, ModelPart>)this.children
			.entrySet()
			.stream()
			.collect(
				Collectors.toMap(Entry::getKey, entry -> ((PartDefinition)entry.getValue()).bake(i, j), (modelPartx, modelPart2) -> modelPartx, Object2ObjectArrayMap::new)
			);
		List<ModelPart.Cube> list = (List<ModelPart.Cube>)this.cubes
			.stream()
			.map(cubeDefinition -> cubeDefinition.bake(i, j))
			.collect(ImmutableList.toImmutableList());
		ModelPart modelPart = new ModelPart(list, object2ObjectArrayMap);
		modelPart.loadPose(this.partPose);
		return modelPart;
	}

	public PartDefinition getChild(String string) {
		return (PartDefinition)this.children.get(string);
	}
}
