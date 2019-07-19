/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.fishing.FishingHook;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public class FishingHookRenderer
extends EntityRenderer<FishingHook> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/fishing_hook.png");

    public FishingHookRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
    }

    @Override
    public void render(FishingHook fishingHook, double d, double e, double f, float g, float h) {
        double x;
        double w;
        double v;
        double u;
        double t;
        Player player = fishingHook.getOwner();
        if (player == null || this.solidRender) {
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float)d, (float)e, (float)f);
        GlStateManager.enableRescaleNormal();
        GlStateManager.scalef(0.5f, 0.5f, 0.5f);
        this.bindTexture(fishingHook);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        float i = 1.0f;
        float j = 0.5f;
        float k = 0.5f;
        GlStateManager.rotatef(180.0f - this.entityRenderDispatcher.playerRotY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotatef((float)(this.entityRenderDispatcher.options.thirdPersonView == 2 ? -1 : 1) * -this.entityRenderDispatcher.playerRotX, 1.0f, 0.0f, 0.0f);
        if (this.solidRender) {
            GlStateManager.enableColorMaterial();
            GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(fishingHook));
        }
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_NORMAL);
        bufferBuilder.vertex(-0.5, -0.5, 0.0).uv(0.0, 1.0).normal(0.0f, 1.0f, 0.0f).endVertex();
        bufferBuilder.vertex(0.5, -0.5, 0.0).uv(1.0, 1.0).normal(0.0f, 1.0f, 0.0f).endVertex();
        bufferBuilder.vertex(0.5, 0.5, 0.0).uv(1.0, 0.0).normal(0.0f, 1.0f, 0.0f).endVertex();
        bufferBuilder.vertex(-0.5, 0.5, 0.0).uv(0.0, 0.0).normal(0.0f, 1.0f, 0.0f).endVertex();
        tesselator.end();
        if (this.solidRender) {
            GlStateManager.tearDownSolidRenderingTextureCombine();
            GlStateManager.disableColorMaterial();
        }
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        int l = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
        ItemStack itemStack = player.getMainHandItem();
        if (itemStack.getItem() != Items.FISHING_ROD) {
            l = -l;
        }
        float m = player.getAttackAnim(h);
        float n = Mth.sin(Mth.sqrt(m) * (float)Math.PI);
        float o = Mth.lerp(h, player.yBodyRotO, player.yBodyRot) * ((float)Math.PI / 180);
        double p = Mth.sin(o);
        double q = Mth.cos(o);
        double r = (double)l * 0.35;
        double s = 0.8;
        if (this.entityRenderDispatcher.options != null && this.entityRenderDispatcher.options.thirdPersonView > 0 || player != Minecraft.getInstance().player) {
            t = Mth.lerp((double)h, player.xo, player.x) - q * r - p * 0.8;
            u = player.yo + (double)player.getEyeHeight() + (player.y - player.yo) * (double)h - 0.45;
            v = Mth.lerp((double)h, player.zo, player.z) - p * r + q * 0.8;
            w = player.isVisuallySneaking() ? -0.1875 : 0.0;
        } else {
            x = this.entityRenderDispatcher.options.fov;
            Vec3 vec3 = new Vec3((double)l * -0.36 * (x /= 100.0), -0.045 * x, 0.4);
            vec3 = vec3.xRot(-Mth.lerp(h, player.xRotO, player.xRot) * ((float)Math.PI / 180));
            vec3 = vec3.yRot(-Mth.lerp(h, player.yRotO, player.yRot) * ((float)Math.PI / 180));
            vec3 = vec3.yRot(n * 0.5f);
            vec3 = vec3.xRot(-n * 0.7f);
            t = Mth.lerp((double)h, player.xo, player.x) + vec3.x;
            u = Mth.lerp((double)h, player.yo, player.y) + vec3.y;
            v = Mth.lerp((double)h, player.zo, player.z) + vec3.z;
            w = player.getEyeHeight();
        }
        x = Mth.lerp((double)h, fishingHook.xo, fishingHook.x);
        double y = Mth.lerp((double)h, fishingHook.yo, fishingHook.y) + 0.25;
        double z = Mth.lerp((double)h, fishingHook.zo, fishingHook.z);
        double aa = (float)(t - x);
        double ab = (double)((float)(u - y)) + w;
        double ac = (float)(v - z);
        GlStateManager.disableTexture();
        GlStateManager.disableLighting();
        bufferBuilder.begin(3, DefaultVertexFormat.POSITION_COLOR);
        int ad = 16;
        for (int ae = 0; ae <= 16; ++ae) {
            float af = (float)ae / 16.0f;
            bufferBuilder.vertex(d + aa * (double)af, e + ab * (double)(af * af + af) * 0.5 + 0.25, f + ac * (double)af).color(0, 0, 0, 255).endVertex();
        }
        tesselator.end();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture();
        super.render(fishingHook, d, e, f, g, h);
    }

    @Override
    protected ResourceLocation getTextureLocation(FishingHook fishingHook) {
        return TEXTURE_LOCATION;
    }
}

