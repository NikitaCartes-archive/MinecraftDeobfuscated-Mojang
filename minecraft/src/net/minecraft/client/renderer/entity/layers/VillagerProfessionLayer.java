package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.VillagerHeadModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.resources.metadata.animation.VillagerMetaDataSection;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;

@Environment(EnvType.CLIENT)
public class VillagerProfessionLayer<T extends LivingEntity & VillagerDataHolder, M extends EntityModel<T> & VillagerHeadModel> extends RenderLayer<T, M> {
	private static final Int2ObjectMap<ResourceLocation> LEVEL_LOCATIONS = Util.make(new Int2ObjectOpenHashMap<>(), int2ObjectOpenHashMap -> {
		int2ObjectOpenHashMap.put(1, new ResourceLocation("stone"));
		int2ObjectOpenHashMap.put(2, new ResourceLocation("iron"));
		int2ObjectOpenHashMap.put(3, new ResourceLocation("gold"));
		int2ObjectOpenHashMap.put(4, new ResourceLocation("emerald"));
		int2ObjectOpenHashMap.put(5, new ResourceLocation("diamond"));
	});
	private final Object2ObjectMap<VillagerType, VillagerMetaDataSection.Hat> typeHatCache = new Object2ObjectOpenHashMap<>();
	private final Object2ObjectMap<VillagerProfession, VillagerMetaDataSection.Hat> professionHatCache = new Object2ObjectOpenHashMap();
	private final ResourceManager resourceManager;
	private final String path;

	public VillagerProfessionLayer(RenderLayerParent<T, M> renderLayerParent, ResourceManager resourceManager, String string) {
		super(renderLayerParent);
		this.resourceManager = resourceManager;
		this.path = string;
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
		if (!livingEntity.isInvisible()) {
			VillagerData villagerData = livingEntity.getVillagerData();
			VillagerType villagerType = villagerData.getType();
			VillagerProfession villagerProfession = villagerData.getProfession();
			VillagerMetaDataSection.Hat hat = this.getHatData(this.typeHatCache, "type", BuiltInRegistries.VILLAGER_TYPE, villagerType);
			VillagerMetaDataSection.Hat hat2 = this.getHatData(this.professionHatCache, "profession", BuiltInRegistries.VILLAGER_PROFESSION, villagerProfession);
			M entityModel = this.getParentModel();
			entityModel.hatVisible(hat2 == VillagerMetaDataSection.Hat.NONE || hat2 == VillagerMetaDataSection.Hat.PARTIAL && hat != VillagerMetaDataSection.Hat.FULL);
			ResourceLocation resourceLocation = this.getResourceLocation("type", BuiltInRegistries.VILLAGER_TYPE.getKey(villagerType));
			renderColoredCutoutModel(entityModel, resourceLocation, poseStack, multiBufferSource, i, livingEntity, 1.0F, 1.0F, 1.0F);
			entityModel.hatVisible(true);
			if (villagerProfession != VillagerProfession.NONE && !livingEntity.isBaby()) {
				ResourceLocation resourceLocation2 = this.getResourceLocation("profession", BuiltInRegistries.VILLAGER_PROFESSION.getKey((T)villagerProfession));
				renderColoredCutoutModel(entityModel, resourceLocation2, poseStack, multiBufferSource, i, livingEntity, 1.0F, 1.0F, 1.0F);
				if (villagerProfession != VillagerProfession.NITWIT) {
					ResourceLocation resourceLocation3 = this.getResourceLocation(
						"profession_level", LEVEL_LOCATIONS.get(Mth.clamp(villagerData.getLevel(), 1, LEVEL_LOCATIONS.size()))
					);
					renderColoredCutoutModel(entityModel, resourceLocation3, poseStack, multiBufferSource, i, livingEntity, 1.0F, 1.0F, 1.0F);
				}
			}
		}
	}

	private ResourceLocation getResourceLocation(String string, ResourceLocation resourceLocation) {
		return resourceLocation.withPath((UnaryOperator<String>)(string2 -> "textures/entity/" + this.path + "/" + string + "/" + string2 + ".png"));
	}

	public <K> VillagerMetaDataSection.Hat getHatData(
		Object2ObjectMap<K, VillagerMetaDataSection.Hat> object2ObjectMap, String string, DefaultedRegistry<K> defaultedRegistry, K object
	) {
		return (VillagerMetaDataSection.Hat)object2ObjectMap.computeIfAbsent(
			object,
			object2 -> (VillagerMetaDataSection.Hat)this.resourceManager
					.getResource(this.getResourceLocation(string, defaultedRegistry.getKey(object)))
					.flatMap(resource -> {
						try {
							return resource.metadata().getSection(VillagerMetaDataSection.SERIALIZER).map(VillagerMetaDataSection::getHat);
						} catch (IOException var2xx) {
							return Optional.empty();
						}
					})
					.orElse(VillagerMetaDataSection.Hat.NONE)
		);
	}
}
