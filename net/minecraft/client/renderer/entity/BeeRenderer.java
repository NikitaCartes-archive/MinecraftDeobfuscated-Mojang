/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BeeModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Bee;

@Environment(value=EnvType.CLIENT)
public class BeeRenderer
extends MobRenderer<Bee, BeeModel<Bee>> {
    private static final ResourceLocation ANGRY_BEE_TEXTURE = new ResourceLocation("textures/entity/bee/bee_angry.png");
    private static final ResourceLocation ANGRY_NECTAR_BEE_TEXTURE = new ResourceLocation("textures/entity/bee/bee_angry_nectar.png");
    private static final ResourceLocation BEE_TEXTURE = new ResourceLocation("textures/entity/bee/bee.png");
    private static final ResourceLocation NECTAR_BEE_TEXTURE = new ResourceLocation("textures/entity/bee/bee_nectar.png");

    public BeeRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new BeeModel(), 0.4f);
    }

    @Override
    public ResourceLocation getTextureLocation(Bee bee) {
        if (bee.isAngry()) {
            if (bee.hasNectar()) {
                return ANGRY_NECTAR_BEE_TEXTURE;
            }
            return ANGRY_BEE_TEXTURE;
        }
        if (bee.hasNectar()) {
            return NECTAR_BEE_TEXTURE;
        }
        return BEE_TEXTURE;
    }
}

