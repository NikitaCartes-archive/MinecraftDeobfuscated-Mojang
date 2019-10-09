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
import net.minecraft.world.entity.animal.IronGolem;

@Environment(EnvType.CLIENT)
public class IronGolemCrackinessLayer extends RenderLayer<IronGolem, IronGolemModel<IronGolem>> {
	private static final Map<IronGolem.Crackiness, ResourceLocation> resourceLocations = ImmutableMap.of(
		IronGolem.Crackiness.LOW,
		new ResourceLocation("textures/entity/iron_golem/iron_golem_crackiness_low.png"),
		IronGolem.Crackiness.MEDIUM,
		new ResourceLocation("textures/entity/iron_golem/iron_golem_crackiness_medium.png"),
		IronGolem.Crackiness.HIGH,
		new ResourceLocation("textures/entity/iron_golem/iron_golem_crackiness_high.png")
	);

	public IronGolemCrackinessLayer(RenderLayerParent<IronGolem, IronGolemModel<IronGolem>> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(
		PoseStack poseStack, MultiBufferSource multiBufferSource, int i, IronGolem ironGolem, float f, float g, float h, float j, float k, float l, float m
	) {
		IronGolem.Crackiness crackiness = ironGolem.getCrackiness();
		if (crackiness != IronGolem.Crackiness.NONE) {
			ResourceLocation resourceLocation = (ResourceLocation)resourceLocations.get(crackiness);
			renderColoredCutoutModel(this.getParentModel(), resourceLocation, poseStack, multiBufferSource, i, ironGolem, 1.0F, 1.0F, 1.0F);
		}
	}
}
