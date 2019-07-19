/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;

@Environment(value=EnvType.CLIENT)
public class BubbleColumnUpParticle
extends TextureSheetParticle {
    private BubbleColumnUpParticle(Level level, double d, double e, double f, double g, double h, double i) {
        super(level, d, e, f);
        this.setSize(0.02f, 0.02f);
        this.quadSize *= this.random.nextFloat() * 0.6f + 0.2f;
        this.xd = g * (double)0.2f + (Math.random() * 2.0 - 1.0) * (double)0.02f;
        this.yd = h * (double)0.2f + (Math.random() * 2.0 - 1.0) * (double)0.02f;
        this.zd = i * (double)0.2f + (Math.random() * 2.0 - 1.0) * (double)0.02f;
        this.lifetime = (int)(40.0 / (Math.random() * 0.8 + 0.2));
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.yd += 0.005;
        if (this.lifetime-- <= 0) {
            this.remove();
            return;
        }
        this.move(this.xd, this.yd, this.zd);
        this.xd *= (double)0.85f;
        this.yd *= (double)0.85f;
        this.zd *= (double)0.85f;
        if (!this.level.getFluidState(new BlockPos(this.x, this.y, this.z)).is(FluidTags.WATER)) {
            this.remove();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Environment(value=EnvType.CLIENT)
    public static class Provider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, Level level, double d, double e, double f, double g, double h, double i) {
            BubbleColumnUpParticle bubbleColumnUpParticle = new BubbleColumnUpParticle(level, d, e, f, g, h, i);
            bubbleColumnUpParticle.pickSprite(this.sprite);
            return bubbleColumnUpParticle;
        }
    }
}

