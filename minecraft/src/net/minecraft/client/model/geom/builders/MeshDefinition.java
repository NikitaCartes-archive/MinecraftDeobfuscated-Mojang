package net.minecraft.client.model.geom.builders;

import com.google.common.collect.ImmutableList;
import java.util.function.UnaryOperator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.PartPose;

@Environment(EnvType.CLIENT)
public class MeshDefinition {
	private final PartDefinition root;

	public MeshDefinition() {
		this(new PartDefinition(ImmutableList.of(), PartPose.ZERO));
	}

	private MeshDefinition(PartDefinition partDefinition) {
		this.root = partDefinition;
	}

	public PartDefinition getRoot() {
		return this.root;
	}

	public MeshDefinition transformed(UnaryOperator<PartPose> unaryOperator) {
		return new MeshDefinition(this.root.transformed(unaryOperator));
	}
}
