/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.VillagerHeadModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.metadata.animation.VillagerMetaDataSection;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;

@Environment(value=EnvType.CLIENT)
public class VillagerProfessionLayer<T extends LivingEntity, M extends EntityModel<T>>
extends RenderLayer<T, M>
implements ResourceManagerReloadListener {
    private static final Int2ObjectMap<ResourceLocation> LEVEL_LOCATIONS = Util.make(new Int2ObjectOpenHashMap(), int2ObjectOpenHashMap -> {
        int2ObjectOpenHashMap.put(1, new ResourceLocation("stone"));
        int2ObjectOpenHashMap.put(2, new ResourceLocation("iron"));
        int2ObjectOpenHashMap.put(3, new ResourceLocation("gold"));
        int2ObjectOpenHashMap.put(4, new ResourceLocation("emerald"));
        int2ObjectOpenHashMap.put(5, new ResourceLocation("diamond"));
    });
    private final Object2ObjectMap<VillagerType, VillagerMetaDataSection.Hat> typeHatCache = new Object2ObjectOpenHashMap<VillagerType, VillagerMetaDataSection.Hat>();
    private final Object2ObjectMap<VillagerProfession, VillagerMetaDataSection.Hat> professionHatCache = new Object2ObjectOpenHashMap<VillagerProfession, VillagerMetaDataSection.Hat>();
    private final ReloadableResourceManager resourceManager;
    private final String path;

    public VillagerProfessionLayer(RenderLayerParent<T, M> renderLayerParent, ReloadableResourceManager reloadableResourceManager, String string) {
        super(renderLayerParent);
        this.resourceManager = reloadableResourceManager;
        this.path = string;
        reloadableResourceManager.registerReloadListener(this);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l, float m) {
        if (((Entity)livingEntity).isInvisible()) {
            return;
        }
        VillagerData villagerData = ((VillagerDataHolder)livingEntity).getVillagerData();
        VillagerType villagerType = villagerData.getType();
        VillagerProfession villagerProfession = villagerData.getProfession();
        VillagerMetaDataSection.Hat hat = this.getHatData(this.typeHatCache, "type", Registry.VILLAGER_TYPE, villagerType);
        VillagerMetaDataSection.Hat hat2 = this.getHatData(this.professionHatCache, "profession", Registry.VILLAGER_PROFESSION, villagerProfession);
        Object entityModel = this.getParentModel();
        ((VillagerHeadModel)entityModel).hatVisible(hat2 == VillagerMetaDataSection.Hat.NONE || hat2 == VillagerMetaDataSection.Hat.PARTIAL && hat != VillagerMetaDataSection.Hat.FULL);
        ResourceLocation resourceLocation = this.getResourceLocation("type", Registry.VILLAGER_TYPE.getKey(villagerType));
        VillagerProfessionLayer.renderColoredModel(entityModel, resourceLocation, poseStack, multiBufferSource, i, livingEntity);
        ((VillagerHeadModel)entityModel).hatVisible(true);
        if (villagerProfession != VillagerProfession.NONE && !((LivingEntity)livingEntity).isBaby()) {
            ResourceLocation resourceLocation2 = this.getResourceLocation("profession", Registry.VILLAGER_PROFESSION.getKey(villagerProfession));
            VillagerProfessionLayer.renderColoredModel(entityModel, resourceLocation2, poseStack, multiBufferSource, i, livingEntity);
            if (villagerProfession != VillagerProfession.NITWIT) {
                ResourceLocation resourceLocation3 = this.getResourceLocation("profession_level", (ResourceLocation)LEVEL_LOCATIONS.get(Mth.clamp(villagerData.getLevel(), 1, LEVEL_LOCATIONS.size())));
                VillagerProfessionLayer.renderColoredModel(entityModel, resourceLocation3, poseStack, multiBufferSource, i, livingEntity);
            }
        }
    }

    private ResourceLocation getResourceLocation(String string, ResourceLocation resourceLocation) {
        return new ResourceLocation(resourceLocation.getNamespace(), "textures/entity/" + this.path + "/" + string + "/" + resourceLocation.getPath() + ".png");
    }

    public <K> VillagerMetaDataSection.Hat getHatData(Object2ObjectMap<K, VillagerMetaDataSection.Hat> object2ObjectMap, String string, DefaultedRegistry<K> defaultedRegistry, K object) {
        return object2ObjectMap.computeIfAbsent(object, object2 -> {
            try (Resource resource = this.resourceManager.getResource(this.getResourceLocation(string, defaultedRegistry.getKey(object)));){
                VillagerMetaDataSection villagerMetaDataSection = resource.getMetadata(VillagerMetaDataSection.SERIALIZER);
                if (villagerMetaDataSection == null) return VillagerMetaDataSection.Hat.NONE;
                VillagerMetaDataSection.Hat hat = villagerMetaDataSection.getHat();
                return hat;
            } catch (IOException iOException) {
                // empty catch block
            }
            return VillagerMetaDataSection.Hat.NONE;
        });
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.professionHatCache.clear();
        this.typeHatCache.clear();
    }
}

