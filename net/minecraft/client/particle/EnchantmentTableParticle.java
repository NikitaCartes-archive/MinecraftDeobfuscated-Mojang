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
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.Level;

@Environment(value=EnvType.CLIENT)
public class EnchantmentTableParticle
extends TextureSheetParticle {
    private final double xStart;
    private final double yStart;
    private final double zStart;

    private EnchantmentTableParticle(Level level, double d, double e, double f, double g, double h, double i) {
        super(level, d, e, f);
        this.xd = g;
        this.yd = h;
        this.zd = i;
        this.xStart = d;
        this.yStart = e;
        this.zStart = f;
        this.xo = d + g;
        this.yo = e + h;
        this.zo = f + i;
        this.x = this.xo;
        this.y = this.yo;
        this.z = this.zo;
        this.quadSize = 0.1f * (this.random.nextFloat() * 0.5f + 0.2f);
        float j = this.random.nextFloat() * 0.6f + 0.4f;
        this.rCol = 0.9f * j;
        this.gCol = 0.9f * j;
        this.bCol = j;
        this.hasPhysics = false;
        this.lifetime = (int)(Math.random() * 10.0) + 30;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void move(double d, double e, double f) {
        this.setBoundingBox(this.getBoundingBox().move(d, e, f));
        this.setLocationFromBoundingbox();
    }

    @Override
    public int getLightColor(float f) {
        int i = super.getLightColor(f);
        float g = (float)this.age / (float)this.lifetime;
        g *= g;
        g *= g;
        int j = i & 0xFF;
        int k = i >> 16 & 0xFF;
        if ((k += (int)(g * 15.0f * 16.0f)) > 240) {
            k = 240;
        }
        return j | k << 16;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        float f = (float)this.age / (float)this.lifetime;
        f = 1.0f - f;
        float g = 1.0f - f;
        g *= g;
        g *= g;
        this.x = this.xStart + this.xd * (double)f;
        this.y = this.yStart + this.yd * (double)f - (double)(g * 1.2f);
        this.z = this.zStart + this.zd * (double)f;
    }

    @Environment(value=EnvType.CLIENT)
    public static class NautilusProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public NautilusProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, Level level, double d, double e, double f, double g, double h, double i) {
            EnchantmentTableParticle enchantmentTableParticle = new EnchantmentTableParticle(level, d, e, f, g, h, i);
            enchantmentTableParticle.pickSprite(this.sprite);
            return enchantmentTableParticle;
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
            EnchantmentTableParticle enchantmentTableParticle = new EnchantmentTableParticle(level, d, e, f, g, h, i);
            enchantmentTableParticle.pickSprite(this.sprite);
            return enchantmentTableParticle;
        }
    }
}

