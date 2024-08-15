package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.model.AxolotlModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.AxolotlRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.axolotl.Axolotl;

@Environment(EnvType.CLIENT)
public class AxolotlRenderer extends AgeableMobRenderer<Axolotl, AxolotlRenderState, AxolotlModel> {
	private static final Map<Axolotl.Variant, ResourceLocation> TEXTURE_BY_TYPE = Util.make(Maps.<Axolotl.Variant, ResourceLocation>newHashMap(), hashMap -> {
		for (Axolotl.Variant variant : Axolotl.Variant.values()) {
			hashMap.put(variant, ResourceLocation.withDefaultNamespace(String.format(Locale.ROOT, "textures/entity/axolotl/axolotl_%s.png", variant.getName())));
		}
	});

	public AxolotlRenderer(EntityRendererProvider.Context context) {
		super(context, new AxolotlModel(context.bakeLayer(ModelLayers.AXOLOTL)), new AxolotlModel(context.bakeLayer(ModelLayers.AXOLOTL_BABY)), 0.5F);
	}

	public ResourceLocation getTextureLocation(AxolotlRenderState axolotlRenderState) {
		return (ResourceLocation)TEXTURE_BY_TYPE.get(axolotlRenderState.variant);
	}

	public AxolotlRenderState createRenderState() {
		return new AxolotlRenderState();
	}

	public void extractRenderState(Axolotl axolotl, AxolotlRenderState axolotlRenderState, float f) {
		super.extractRenderState(axolotl, axolotlRenderState, f);
		axolotlRenderState.variant = axolotl.getVariant();
		axolotlRenderState.playingDeadFactor = axolotl.playingDeadAnimator.getFactor(f);
		axolotlRenderState.inWaterFactor = axolotl.inWaterAnimator.getFactor(f);
		axolotlRenderState.onGroundFactor = axolotl.onGroundAnimator.getFactor(f);
		axolotlRenderState.movingFactor = axolotl.movingAnimator.getFactor(f);
	}
}
