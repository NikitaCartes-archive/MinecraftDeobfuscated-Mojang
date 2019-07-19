/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;

@Environment(value=EnvType.CLIENT)
public class UndeadHorseRenderer
extends AbstractHorseRenderer<AbstractHorse, HorseModel<AbstractHorse>> {
    private static final Map<Class<?>, ResourceLocation> MAP = Maps.newHashMap(ImmutableMap.of(ZombieHorse.class, new ResourceLocation("textures/entity/horse/horse_zombie.png"), SkeletonHorse.class, new ResourceLocation("textures/entity/horse/horse_skeleton.png")));

    public UndeadHorseRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new HorseModel(0.0f), 1.0f);
    }

    @Override
    protected ResourceLocation getTextureLocation(AbstractHorse abstractHorse) {
        return MAP.get(abstractHorse.getClass());
    }
}

