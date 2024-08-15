package net.minecraft.client.renderer.entity.layers;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Crackiness;

@Environment(EnvType.CLIENT)
public class IronGolemCrackinessLayer extends RenderLayer<IronGolemRenderState, IronGolemModel> {
	private static final Map<Crackiness.Level, ResourceLocation> resourceLocations = ImmutableMap.of(
		Crackiness.Level.LOW,
		ResourceLocation.withDefaultNamespace("textures/entity/iron_golem/iron_golem_crackiness_low.png"),
		Crackiness.Level.MEDIUM,
		ResourceLocation.withDefaultNamespace("textures/entity/iron_golem/iron_golem_crackiness_medium.png"),
		Crackiness.Level.HIGH,
		ResourceLocation.withDefaultNamespace("textures/entity/iron_golem/iron_golem_crackiness_high.png")
	);

	public IronGolemCrackinessLayer(RenderLayerParent<IronGolemRenderState, IronGolemModel> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, IronGolemRenderState ironGolemRenderState, float f, float g) {
		if (!ironGolemRenderState.isInvisible) {
			Crackiness.Level level = ironGolemRenderState.crackiness;
			if (level != Crackiness.Level.NONE) {
				ResourceLocation resourceLocation = (ResourceLocation)resourceLocations.get(level);
				renderColoredCutoutModel(this.getParentModel(), resourceLocation, poseStack, multiBufferSource, i, ironGolemRenderState, -1);
			}
		}
	}
}
