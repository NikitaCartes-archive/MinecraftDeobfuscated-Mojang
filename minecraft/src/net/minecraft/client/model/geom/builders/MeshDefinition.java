package net.minecraft.client.model.geom.builders;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.PartPose;

@Environment(EnvType.CLIENT)
public class MeshDefinition {
	private PartDefinition root = new PartDefinition(ImmutableList.of(), PartPose.ZERO);

	public PartDefinition getRoot() {
		return this.root;
	}
}
