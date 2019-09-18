/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.renderer.culling.Culler;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ShulkerHeadLayer;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public class ShulkerRenderer
extends MobRenderer<Shulker, ShulkerModel<Shulker>> {
    public static final ResourceLocation DEFAULT_TEXTURE_LOCATION = new ResourceLocation("textures/" + ModelBakery.DEFAULT_SHULKER_TEXTURE_LOCATION.getPath() + ".png");
    public static final ResourceLocation[] TEXTURE_LOCATION = (ResourceLocation[])ModelBakery.SHULKER_TEXTURE_LOCATION.stream().map(resourceLocation -> new ResourceLocation("textures/" + resourceLocation.getPath() + ".png")).toArray(ResourceLocation[]::new);

    public ShulkerRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new ShulkerModel(), 0.0f);
        this.addLayer(new ShulkerHeadLayer(this));
    }

    @Override
    public void render(Shulker shulker, double d, double e, double f, float g, float h) {
        int i = shulker.getClientSideTeleportInterpolation();
        if (i > 0 && shulker.hasValidInterpolationPositions()) {
            BlockPos blockPos = shulker.getAttachPosition();
            BlockPos blockPos2 = shulker.getOldAttachPosition();
            double j = (double)((float)i - h) / 6.0;
            j *= j;
            double k = (double)(blockPos.getX() - blockPos2.getX()) * j;
            double l = (double)(blockPos.getY() - blockPos2.getY()) * j;
            double m = (double)(blockPos.getZ() - blockPos2.getZ()) * j;
            super.render(shulker, d - k, e - l, f - m, g, h);
        } else {
            super.render(shulker, d, e, f, g, h);
        }
    }

    @Override
    public boolean shouldRender(Shulker shulker, Culler culler, double d, double e, double f) {
        if (super.shouldRender(shulker, culler, d, e, f)) {
            return true;
        }
        if (shulker.getClientSideTeleportInterpolation() > 0 && shulker.hasValidInterpolationPositions()) {
            BlockPos blockPos = shulker.getOldAttachPosition();
            BlockPos blockPos2 = shulker.getAttachPosition();
            Vec3 vec3 = new Vec3(blockPos2.getX(), blockPos2.getY(), blockPos2.getZ());
            Vec3 vec32 = new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            if (culler.isVisible(new AABB(vec32.x, vec32.y, vec32.z, vec3.x, vec3.y, vec3.z))) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected ResourceLocation getTextureLocation(Shulker shulker) {
        if (shulker.getColor() == null) {
            return DEFAULT_TEXTURE_LOCATION;
        }
        return TEXTURE_LOCATION[shulker.getColor().getId()];
    }

    @Override
    protected void setupRotations(Shulker shulker, float f, float g, float h) {
        super.setupRotations(shulker, f, g, h);
        switch (shulker.getAttachFace()) {
            case DOWN: {
                break;
            }
            case EAST: {
                RenderSystem.translatef(0.5f, 0.5f, 0.0f);
                RenderSystem.rotatef(90.0f, 1.0f, 0.0f, 0.0f);
                RenderSystem.rotatef(90.0f, 0.0f, 0.0f, 1.0f);
                break;
            }
            case WEST: {
                RenderSystem.translatef(-0.5f, 0.5f, 0.0f);
                RenderSystem.rotatef(90.0f, 1.0f, 0.0f, 0.0f);
                RenderSystem.rotatef(-90.0f, 0.0f, 0.0f, 1.0f);
                break;
            }
            case NORTH: {
                RenderSystem.translatef(0.0f, 0.5f, -0.5f);
                RenderSystem.rotatef(90.0f, 1.0f, 0.0f, 0.0f);
                break;
            }
            case SOUTH: {
                RenderSystem.translatef(0.0f, 0.5f, 0.5f);
                RenderSystem.rotatef(90.0f, 1.0f, 0.0f, 0.0f);
                RenderSystem.rotatef(180.0f, 0.0f, 0.0f, 1.0f);
                break;
            }
            case UP: {
                RenderSystem.translatef(0.0f, 1.0f, 0.0f);
                RenderSystem.rotatef(180.0f, 1.0f, 0.0f, 0.0f);
            }
        }
    }

    @Override
    protected void scale(Shulker shulker, float f) {
        float g = 0.999f;
        RenderSystem.scalef(0.999f, 0.999f, 0.999f);
    }
}

