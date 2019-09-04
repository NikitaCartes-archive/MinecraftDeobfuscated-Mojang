/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.GuardianRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Guardian;

@Environment(value=EnvType.CLIENT)
public class ElderGuardianRenderer
extends GuardianRenderer {
    private static final ResourceLocation GUARDIAN_ELDER_LOCATION = new ResourceLocation("textures/entity/guardian_elder.png");

    public ElderGuardianRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, 1.2f);
    }

    @Override
    protected void scale(Guardian guardian, float f) {
        RenderSystem.scalef(ElderGuardian.ELDER_SIZE_SCALE, ElderGuardian.ELDER_SIZE_SCALE, ElderGuardian.ELDER_SIZE_SCALE);
    }

    @Override
    protected ResourceLocation getTextureLocation(Guardian guardian) {
        return GUARDIAN_ELDER_LOCATION;
    }
}

