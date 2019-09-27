/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Parrot;

@Environment(value=EnvType.CLIENT)
public class ParrotRenderer
extends MobRenderer<Parrot, ParrotModel> {
    public static final ResourceLocation[] PARROT_LOCATIONS = new ResourceLocation[]{new ResourceLocation("textures/entity/parrot/parrot_red_blue.png"), new ResourceLocation("textures/entity/parrot/parrot_blue.png"), new ResourceLocation("textures/entity/parrot/parrot_green.png"), new ResourceLocation("textures/entity/parrot/parrot_yellow_blue.png"), new ResourceLocation("textures/entity/parrot/parrot_grey.png")};

    public ParrotRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new ParrotModel(), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(Parrot parrot) {
        return PARROT_LOCATIONS[parrot.getVariant()];
    }

    @Override
    public float getBob(Parrot parrot, float f) {
        float g = Mth.lerp(f, parrot.oFlap, parrot.flap);
        float h = Mth.lerp(f, parrot.oFlapSpeed, parrot.flapSpeed);
        return (Mth.sin(g) + 1.0f) * h;
    }
}

