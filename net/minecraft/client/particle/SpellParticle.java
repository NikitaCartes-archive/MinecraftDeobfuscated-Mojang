/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.particle;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class SpellParticle
extends TextureSheetParticle {
    private static final Random RANDOM = new Random();
    private final SpriteSet sprites;

    SpellParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, SpriteSet spriteSet) {
        super(clientLevel, d, e, f, 0.5 - RANDOM.nextDouble(), h, 0.5 - RANDOM.nextDouble());
        this.friction = 0.96f;
        this.gravity = -0.1f;
        this.speedUpWhenYMotionIsBlocked = true;
        this.sprites = spriteSet;
        this.yd *= (double)0.2f;
        if (g == 0.0 && i == 0.0) {
            this.xd *= (double)0.1f;
            this.zd *= (double)0.1f;
        }
        this.quadSize *= 0.75f;
        this.lifetime = (int)(8.0 / (Math.random() * 0.8 + 0.2));
        this.hasPhysics = false;
        this.setSpriteFromAge(spriteSet);
        if (this.isCloseToScopingPlayer()) {
            this.setAlpha(0.0f);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        if (this.isCloseToScopingPlayer()) {
            this.setAlpha(0.0f);
        } else {
            this.setAlpha(Mth.lerp(0.05f, this.alpha, 1.0f));
        }
    }

    private boolean isCloseToScopingPlayer() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer localPlayer = minecraft.player;
        return localPlayer != null && localPlayer.getEyePosition().distanceToSqr(this.x, this.y, this.z) <= 9.0 && minecraft.options.getCameraType().isFirstPerson() && localPlayer.isScoping();
    }

    @Environment(value=EnvType.CLIENT)
    public static class InstantProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public InstantProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return new SpellParticle(clientLevel, d, e, f, g, h, i, this.sprite);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class WitchProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public WitchProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            SpellParticle spellParticle = new SpellParticle(clientLevel, d, e, f, g, h, i, this.sprite);
            float j = clientLevel.random.nextFloat() * 0.5f + 0.35f;
            spellParticle.setColor(1.0f * j, 0.0f * j, 1.0f * j);
            return spellParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class AmbientMobProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public AmbientMobProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            SpellParticle particle = new SpellParticle(clientLevel, d, e, f, g, h, i, this.sprite);
            particle.setAlpha(0.15f);
            particle.setColor((float)g, (float)h, (float)i);
            return particle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class MobProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public MobProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            SpellParticle particle = new SpellParticle(clientLevel, d, e, f, g, h, i, this.sprite);
            particle.setColor((float)g, (float)h, (float)i);
            return particle;
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
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return new SpellParticle(clientLevel, d, e, f, g, h, i, this.sprite);
        }
    }
}

