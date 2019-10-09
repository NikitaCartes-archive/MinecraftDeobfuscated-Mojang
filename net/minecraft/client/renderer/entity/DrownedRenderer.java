/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.layers.DrownedOuterLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Zombie;

@Environment(value=EnvType.CLIENT)
public class DrownedRenderer
extends AbstractZombieRenderer<Drowned, DrownedModel<Drowned>> {
    private static final ResourceLocation DROWNED_LOCATION = new ResourceLocation("textures/entity/zombie/drowned.png");

    public DrownedRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new DrownedModel(0.0f, 0.0f, 64, 64), new DrownedModel(0.5f, true), new DrownedModel(1.0f, true));
        this.addLayer(new DrownedOuterLayer<Drowned>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(Zombie zombie) {
        return DROWNED_LOCATION;
    }

    @Override
    protected void setupRotations(Drowned drowned, PoseStack poseStack, float f, float g, float h) {
        super.setupRotations(drowned, poseStack, f, g, h);
        float i = drowned.getSwimAmount(h);
        if (i > 0.0f) {
            poseStack.mulPose(Vector3f.XP.rotationDegrees(Mth.lerp(i, drowned.xRot, -10.0f - drowned.xRot)));
        }
    }
}

