/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class BaseAshSmokeParticle
extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected BaseAshSmokeParticle(ClientLevel clientLevel, double d, double e, double f, float g, float h, float i, double j, double k, double l, float m, SpriteSet spriteSet, float n, int o, float p, boolean bl) {
        super(clientLevel, d, e, f, 0.0, 0.0, 0.0);
        float q;
        this.friction = 0.96f;
        this.gravity = p;
        this.speedUpWhenYMotionIsBlocked = true;
        this.sprites = spriteSet;
        this.xd *= (double)g;
        this.yd *= (double)h;
        this.zd *= (double)i;
        this.xd += j;
        this.yd += k;
        this.zd += l;
        this.rCol = q = clientLevel.random.nextFloat() * n;
        this.gCol = q;
        this.bCol = q;
        this.quadSize *= 0.75f * m;
        this.lifetime = (int)((double)o / ((double)clientLevel.random.nextFloat() * 0.8 + 0.2) * (double)m);
        this.lifetime = Math.max(this.lifetime, 1);
        this.setSpriteFromAge(spriteSet);
        this.hasPhysics = bl;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public float getQuadSize(float f) {
        return this.quadSize * Mth.clamp(((float)this.age + f) / (float)this.lifetime * 32.0f, 0.0f, 1.0f);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }
}

