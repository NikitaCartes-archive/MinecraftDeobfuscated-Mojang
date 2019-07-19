/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cow;

@Environment(value=EnvType.CLIENT)
public class CowRenderer
extends MobRenderer<Cow, CowModel<Cow>> {
    private static final ResourceLocation COW_LOCATION = new ResourceLocation("textures/entity/cow/cow.png");

    public CowRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new CowModel(), 0.7f);
    }

    @Override
    protected ResourceLocation getTextureLocation(Cow cow) {
        return COW_LOCATION;
    }
}

