package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ChestedHorseModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;

@Environment(EnvType.CLIENT)
public class ChestedHorseRenderer<T extends AbstractChestedHorse> extends AbstractHorseRenderer<T, ChestedHorseModel<T>> {
	private static final Map<EntityType<?>, ResourceLocation> MAP = Maps.<EntityType<?>, ResourceLocation>newHashMap(
		ImmutableMap.of(
			EntityType.DONKEY,
			ResourceLocation.withDefaultNamespace("textures/entity/horse/donkey.png"),
			EntityType.MULE,
			ResourceLocation.withDefaultNamespace("textures/entity/horse/mule.png")
		)
	);

	public ChestedHorseRenderer(EntityRendererProvider.Context context, float f, ModelLayerLocation modelLayerLocation) {
		super(context, new ChestedHorseModel<>(context.bakeLayer(modelLayerLocation)), f);
	}

	public ResourceLocation getTextureLocation(T abstractChestedHorse) {
		return (ResourceLocation)MAP.get(abstractChestedHorse.getType());
	}
}
