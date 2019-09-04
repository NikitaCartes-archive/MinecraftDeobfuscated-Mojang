/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.level.Level;

@Environment(value=EnvType.CLIENT)
public class MobAppearanceParticle
extends Particle {
    private LivingEntity displayEntity;

    private MobAppearanceParticle(Level level, double d, double e, double f) {
        super(level, d, e, f);
        this.gravity = 0.0f;
        this.lifetime = 30;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.displayEntity == null) {
            ElderGuardian elderGuardian = EntityType.ELDER_GUARDIAN.create(this.level);
            elderGuardian.setGhost();
            this.displayEntity = elderGuardian;
        }
    }

    @Override
    public void render(BufferBuilder bufferBuilder, Camera camera, float f, float g, float h, float i, float j, float k) {
        if (this.displayEntity == null) {
            return;
        }
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        entityRenderDispatcher.setPosition(Particle.xOff, Particle.yOff, Particle.zOff);
        float l = 1.0f / ElderGuardian.ELDER_SIZE_SCALE;
        float m = ((float)this.age + f) / (float)this.lifetime;
        RenderSystem.depthMask(true);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        float n = 240.0f;
        RenderSystem.glMultiTexCoord2f(33985, 240.0f, 240.0f);
        RenderSystem.pushMatrix();
        float o = 0.05f + 0.5f * Mth.sin(m * (float)Math.PI);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, o);
        RenderSystem.translatef(0.0f, 1.8f, 0.0f);
        RenderSystem.rotatef(180.0f - camera.getYRot(), 0.0f, 1.0f, 0.0f);
        RenderSystem.rotatef(60.0f - 150.0f * m - camera.getXRot(), 1.0f, 0.0f, 0.0f);
        RenderSystem.translatef(0.0f, -0.4f, -1.5f);
        RenderSystem.scalef(l, l, l);
        this.displayEntity.yRot = 0.0f;
        this.displayEntity.yHeadRot = 0.0f;
        this.displayEntity.yRotO = 0.0f;
        this.displayEntity.yHeadRotO = 0.0f;
        entityRenderDispatcher.render(this.displayEntity, 0.0, 0.0, 0.0, 0.0f, f, false);
        RenderSystem.popMatrix();
        RenderSystem.enableDepthTest();
    }

    @Environment(value=EnvType.CLIENT)
    public static class Provider
    implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, Level level, double d, double e, double f, double g, double h, double i) {
            return new MobAppearanceParticle(level, d, e, f);
        }
    }
}

