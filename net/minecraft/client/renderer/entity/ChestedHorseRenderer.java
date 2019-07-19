/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ChestedHorseModel;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Mule;

@Environment(value=EnvType.CLIENT)
public class ChestedHorseRenderer<T extends AbstractChestedHorse>
extends AbstractHorseRenderer<T, ChestedHorseModel<T>> {
    private static final Map<Class<?>, ResourceLocation> MAP = Maps.newHashMap(ImmutableMap.of(Donkey.class, new ResourceLocation("textures/entity/horse/donkey.png"), Mule.class, new ResourceLocation("textures/entity/horse/mule.png")));

    public ChestedHorseRenderer(EntityRenderDispatcher entityRenderDispatcher, float f) {
        super(entityRenderDispatcher, new ChestedHorseModel(0.0f), f);
    }

    @Override
    protected ResourceLocation getTextureLocation(T abstractChestedHorse) {
        return MAP.get(abstractChestedHorse.getClass());
    }
}

