package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.model.AxolotlModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.axolotl.Axolotl;

@Environment(EnvType.CLIENT)
public class AxolotlRenderer extends MobRenderer<Axolotl, AxolotlModel<Axolotl>> {
	private static final Map<Axolotl.Variant, ResourceLocation> TEXTURE_BY_TYPE = Util.make(Maps.<Axolotl.Variant, ResourceLocation>newHashMap(), hashMap -> {
		for (Axolotl.Variant variant : Axolotl.Variant.BY_ID) {
			hashMap.put(variant, new ResourceLocation(String.format("textures/entity/axolotl/axolotl_%s.png", variant.getName())));
		}
	});

	public AxolotlRenderer(EntityRendererProvider.Context context) {
		super(context, new AxolotlModel<>(context.bakeLayer(ModelLayers.AXOLOTL)), 0.5F);
	}

	public ResourceLocation getTextureLocation(Axolotl axolotl) {
		return (ResourceLocation)TEXTURE_BY_TYPE.get(axolotl.getVariant());
	}
}
