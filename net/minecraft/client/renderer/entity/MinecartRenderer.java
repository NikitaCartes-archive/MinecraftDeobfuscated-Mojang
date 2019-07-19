/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.MinecartModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public class MinecartRenderer<T extends AbstractMinecart>
extends EntityRenderer<T> {
    private static final ResourceLocation MINECART_LOCATION = new ResourceLocation("textures/entity/minecart.png");
    protected final EntityModel<T> model = new MinecartModel();

    public MinecartRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
        this.shadowRadius = 0.7f;
    }

    @Override
    public void render(T abstractMinecart, double d, double e, double f, float g, float h) {
        BlockState blockState;
        GlStateManager.pushMatrix();
        this.bindTexture(abstractMinecart);
        long l = (long)((Entity)abstractMinecart).getId() * 493286711L;
        l = l * l * 4392167121L + l * 98761L;
        float i = (((float)(l >> 16 & 7L) + 0.5f) / 8.0f - 0.5f) * 0.004f;
        float j = (((float)(l >> 20 & 7L) + 0.5f) / 8.0f - 0.5f) * 0.004f;
        float k = (((float)(l >> 24 & 7L) + 0.5f) / 8.0f - 0.5f) * 0.004f;
        GlStateManager.translatef(i, j, k);
        double m = Mth.lerp((double)h, ((AbstractMinecart)abstractMinecart).xOld, ((AbstractMinecart)abstractMinecart).x);
        double n = Mth.lerp((double)h, ((AbstractMinecart)abstractMinecart).yOld, ((AbstractMinecart)abstractMinecart).y);
        double o = Mth.lerp((double)h, ((AbstractMinecart)abstractMinecart).zOld, ((AbstractMinecart)abstractMinecart).z);
        double p = 0.3f;
        Vec3 vec3 = ((AbstractMinecart)abstractMinecart).getPos(m, n, o);
        float q = Mth.lerp(h, ((AbstractMinecart)abstractMinecart).xRotO, ((AbstractMinecart)abstractMinecart).xRot);
        if (vec3 != null) {
            Vec3 vec32 = ((AbstractMinecart)abstractMinecart).getPosOffs(m, n, o, 0.3f);
            Vec3 vec33 = ((AbstractMinecart)abstractMinecart).getPosOffs(m, n, o, -0.3f);
            if (vec32 == null) {
                vec32 = vec3;
            }
            if (vec33 == null) {
                vec33 = vec3;
            }
            d += vec3.x - m;
            e += (vec32.y + vec33.y) / 2.0 - n;
            f += vec3.z - o;
            Vec3 vec34 = vec33.add(-vec32.x, -vec32.y, -vec32.z);
            if (vec34.length() != 0.0) {
                vec34 = vec34.normalize();
                g = (float)(Math.atan2(vec34.z, vec34.x) * 180.0 / Math.PI);
                q = (float)(Math.atan(vec34.y) * 73.0);
            }
        }
        GlStateManager.translatef((float)d, (float)e + 0.375f, (float)f);
        GlStateManager.rotatef(180.0f - g, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotatef(-q, 0.0f, 0.0f, 1.0f);
        float r = (float)((AbstractMinecart)abstractMinecart).getHurtTime() - h;
        float s = ((AbstractMinecart)abstractMinecart).getDamage() - h;
        if (s < 0.0f) {
            s = 0.0f;
        }
        if (r > 0.0f) {
            GlStateManager.rotatef(Mth.sin(r) * r * s / 10.0f * (float)((AbstractMinecart)abstractMinecart).getHurtDir(), 1.0f, 0.0f, 0.0f);
        }
        int t = ((AbstractMinecart)abstractMinecart).getDisplayOffset();
        if (this.solidRender) {
            GlStateManager.enableColorMaterial();
            GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(abstractMinecart));
        }
        if ((blockState = ((AbstractMinecart)abstractMinecart).getDisplayBlockState()).getRenderShape() != RenderShape.INVISIBLE) {
            GlStateManager.pushMatrix();
            this.bindTexture(TextureAtlas.LOCATION_BLOCKS);
            float u = 0.75f;
            GlStateManager.scalef(0.75f, 0.75f, 0.75f);
            GlStateManager.translatef(-0.5f, (float)(t - 8) / 16.0f, 0.5f);
            this.renderMinecartContents(abstractMinecart, h, blockState);
            GlStateManager.popMatrix();
            GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            this.bindTexture(abstractMinecart);
        }
        GlStateManager.scalef(-1.0f, -1.0f, 1.0f);
        this.model.render(abstractMinecart, 0.0f, 0.0f, -0.1f, 0.0f, 0.0f, 0.0625f);
        GlStateManager.popMatrix();
        if (this.solidRender) {
            GlStateManager.tearDownSolidRenderingTextureCombine();
            GlStateManager.disableColorMaterial();
        }
        super.render(abstractMinecart, d, e, f, g, h);
    }

    @Override
    protected ResourceLocation getTextureLocation(T abstractMinecart) {
        return MINECART_LOCATION;
    }

    protected void renderMinecartContents(T abstractMinecart, float f, BlockState blockState) {
        GlStateManager.pushMatrix();
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(blockState, ((Entity)abstractMinecart).getBrightness());
        GlStateManager.popMatrix();
    }
}

