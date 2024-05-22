package net.minecraft.client.renderer.entity.layers;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Crackiness;
import net.minecraft.world.entity.animal.IronGolem;

@Environment(EnvType.CLIENT)
public class IronGolemCrackinessLayer extends RenderLayer<IronGolem, IronGolemModel<IronGolem>> {
	private static final Map<Crackiness.Level, ResourceLocation> resourceLocations = ImmutableMap.of(
		Crackiness.Level.LOW,
		ResourceLocation.withDefaultNamespace("textures/entity/iron_golem/iron_golem_crackiness_low.png"),
		Crackiness.Level.MEDIUM,
		ResourceLocation.withDefaultNamespace("textures/entity/iron_golem/iron_golem_crackiness_medium.png"),
		Crackiness.Level.HIGH,
		ResourceLocation.withDefaultNamespace("textures/entity/iron_golem/iron_golem_crackiness_high.png")
	);

	public IronGolemCrackinessLayer(RenderLayerParent<IronGolem, IronGolemModel<IronGolem>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, IronGolem ironGolem, float f, float g, float h, float j, float k, float l) {
		if (!ironGolem.isInvisible()) {
			Crackiness.Level level = ironGolem.getCrackiness();
			if (level != Crackiness.Level.NONE) {
				ResourceLocation resourceLocation = (ResourceLocation)resourceLocations.get(level);
				renderColoredCutoutModel(this.getParentModel(), resourceLocation, poseStack, multiBufferSource, i, ironGolem, -1);
			}
		}
	}
}
