/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.world.level.Level;

@Environment(value=EnvType.CLIENT)
public class NoRenderParticle
extends Particle {
    protected NoRenderParticle(Level level, double d, double e, double f) {
        super(level, d, e, f);
    }

    protected NoRenderParticle(Level level, double d, double e, double f, double g, double h, double i) {
        super(level, d, e, f, g, h, i);
    }

    @Override
    public final void render(VertexConsumer vertexConsumer, Camera camera, float f) {
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.NO_RENDER;
    }
}

