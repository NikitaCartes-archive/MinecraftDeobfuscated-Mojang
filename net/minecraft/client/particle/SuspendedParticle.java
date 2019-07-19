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
public class SuspendedParticle
extends TextureSheetParticle {
    private SuspendedParticle(Level level, double d, double e, double f) {
        super(level, d, e - 0.125, f);
        this.rCol = 0.4f;
        this.gCol = 0.4f;
        this.bCol = 0.7f;
        this.setSize(0.01f, 0.01f);
        this.quadSize *= this.random.nextFloat() * 0.6f + 0.2f;
        this.lifetime = (int)(16.0 / (Math.random() * 0.8 + 0.2));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.lifetime-- <= 0) {
            this.remove();
            return;
        }
        this.move(this.xd, this.yd, this.zd);
        if (!this.level.getFluidState(new BlockPos(this.x, this.y, this.z)).is(FluidTags.WATER)) {
            this.remove();
        }
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
            SuspendedParticle suspendedParticle = new SuspendedParticle(level, d, e, f);
            suspendedParticle.pickSprite(this.sprite);
            return suspendedParticle;
        }
    }
}

