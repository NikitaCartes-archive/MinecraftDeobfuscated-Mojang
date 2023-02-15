/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class NoteParticle
extends TextureSheetParticle {
    NoteParticle(ClientLevel clientLevel, double d, double e, double f, double g) {
        super(clientLevel, d, e, f, 0.0, 0.0, 0.0);
        this.friction = 0.66f;
        this.speedUpWhenYMotionIsBlocked = true;
        this.xd *= (double)0.01f;
        this.yd *= (double)0.01f;
        this.zd *= (double)0.01f;
        this.yd += 0.2;
        this.rCol = Math.max(0.0f, Mth.sin(((float)g + 0.0f) * ((float)Math.PI * 2)) * 0.65f + 0.35f);
        this.gCol = Math.max(0.0f, Mth.sin(((float)g + 0.33333334f) * ((float)Math.PI * 2)) * 0.65f + 0.35f);
        this.bCol = Math.max(0.0f, Mth.sin(((float)g + 0.6666667f) * ((float)Math.PI * 2)) * 0.65f + 0.35f);
        this.quadSize *= 1.5f;
        this.lifetime = 6;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public float getQuadSize(float f) {
        return this.quadSize * Mth.clamp(((float)this.age + f) / (float)this.lifetime * 32.0f, 0.0f, 1.0f);
    }

    @Environment(value=EnvType.CLIENT)
    public static class Provider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            NoteParticle noteParticle = new NoteParticle(clientLevel, d, e, f, g);
            noteParticle.pickSprite(this.sprite);
            return noteParticle;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, e, f, g, h, i);
        }
    }
}

