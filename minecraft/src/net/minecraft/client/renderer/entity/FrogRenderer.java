package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.model.FrogModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.frog.Frog;

@Environment(EnvType.CLIENT)
public class FrogRenderer extends MobRenderer<Frog, FrogModel<Frog>> {
	private static final Map<Frog.Variant, ResourceLocation> TEXTURE_BY_TYPE = Util.make(Maps.<Frog.Variant, ResourceLocation>newHashMap(), hashMap -> {
		for (Frog.Variant variant : Frog.Variant.values()) {
			hashMap.put(variant, new ResourceLocation(String.format("textures/entity/frog/%s_frog.png", variant.getName())));
		}
	});

	public FrogRenderer(EntityRendererProvider.Context context) {
		super(context, new FrogModel<>(context.bakeLayer(ModelLayers.FROG)), 0.3F);
	}

	public ResourceLocation getTextureLocation(Frog frog) {
		return (ResourceLocation)TEXTURE_BY_TYPE.get(frog.getVariant());
	}
}
