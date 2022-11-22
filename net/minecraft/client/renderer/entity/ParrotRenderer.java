/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Parrot;

@Environment(value=EnvType.CLIENT)
public class ParrotRenderer
extends MobRenderer<Parrot, ParrotModel> {
    private static final ResourceLocation RED_BLUE = new ResourceLocation("textures/entity/parrot/parrot_red_blue.png");
    private static final ResourceLocation BLUE = new ResourceLocation("textures/entity/parrot/parrot_blue.png");
    private static final ResourceLocation GREEN = new ResourceLocation("textures/entity/parrot/parrot_green.png");
    private static final ResourceLocation YELLOW_BLUE = new ResourceLocation("textures/entity/parrot/parrot_yellow_blue.png");
    private static final ResourceLocation GREY = new ResourceLocation("textures/entity/parrot/parrot_grey.png");

    public ParrotRenderer(EntityRendererProvider.Context context) {
        super(context, new ParrotModel(context.bakeLayer(ModelLayers.PARROT)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(Parrot parrot) {
        return ParrotRenderer.getVariantTexture(parrot.getVariant());
    }

    public static ResourceLocation getVariantTexture(Parrot.Variant variant) {
        return switch (variant) {
            default -> throw new IncompatibleClassChangeError();
            case Parrot.Variant.RED_BLUE -> RED_BLUE;
            case Parrot.Variant.BLUE -> BLUE;
            case Parrot.Variant.GREEN -> GREEN;
            case Parrot.Variant.YELLOW_BLUE -> YELLOW_BLUE;
            case Parrot.Variant.GRAY -> GREY;
        };
    }

    @Override
    public float getBob(Parrot parrot, float f) {
        float g = Mth.lerp(f, parrot.oFlap, parrot.flap);
        float h = Mth.lerp(f, parrot.oFlapSpeed, parrot.flapSpeed);
        return (Mth.sin(g) + 1.0f) * h;
    }
}

