/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.Arrow;

@Environment(value=EnvType.CLIENT)
public class TippableArrowRenderer
extends ArrowRenderer<Arrow> {
    public static final ResourceLocation NORMAL_ARROW_LOCATION = new ResourceLocation("textures/entity/projectiles/arrow.png");
    public static final ResourceLocation TIPPED_ARROW_LOCATION = new ResourceLocation("textures/entity/projectiles/tipped_arrow.png");

    public TippableArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(Arrow arrow) {
        return arrow.getColor() > 0 ? TIPPED_ARROW_LOCATION : NORMAL_ARROW_LOCATION;
    }
}

