package net.minecraft.client.model;

import java.util.Set;
import java.util.Map.Entry;
import java.util.function.UnaryOperator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;

@Environment(EnvType.CLIENT)
public record BabyModelTransform(
	boolean scaleHead, float babyYHeadOffset, float babyZHeadOffset, float babyHeadScale, float babyBodyScale, float bodyYOffset, Set<String> headParts
) implements MeshTransformer {
	public BabyModelTransform(Set<String> set) {
		this(false, 5.0F, 2.0F, set);
	}

	public BabyModelTransform(boolean bl, float f, float g, Set<String> set) {
		this(bl, f, g, 2.0F, 2.0F, 24.0F, set);
	}

	@Override
	public MeshDefinition apply(MeshDefinition meshDefinition) {
		float f = this.scaleHead ? 1.5F / this.babyHeadScale : 1.0F;
		float g = 1.0F / this.babyBodyScale;
		UnaryOperator<PartPose> unaryOperator = partPose -> partPose.translated(0.0F, this.babyYHeadOffset, this.babyZHeadOffset).scaled(f);
		UnaryOperator<PartPose> unaryOperator2 = partPose -> partPose.translated(0.0F, this.bodyYOffset, 0.0F).scaled(g);
		MeshDefinition meshDefinition2 = new MeshDefinition();

		for (Entry<String, PartDefinition> entry : meshDefinition.getRoot().getChildren()) {
			String string = (String)entry.getKey();
			PartDefinition partDefinition = (PartDefinition)entry.getValue();
			meshDefinition2.getRoot().addOrReplaceChild(string, partDefinition.transformed(this.headParts.contains(string) ? unaryOperator : unaryOperator2));
		}

		return meshDefinition2;
	}
}
