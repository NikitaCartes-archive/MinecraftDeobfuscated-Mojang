/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.world.level.Level;

@Environment(value=EnvType.CLIENT)
public class SimpleAnimatedParticle
extends TextureSheetParticle {
    protected final SpriteSet sprites;
    private final float baseGravity;
    private float baseAirFriction = 0.91f;
    private float fadeR;
    private float fadeG;
    private float fadeB;
    private boolean hasFade;

    protected SimpleAnimatedParticle(Level level, double d, double e, double f, SpriteSet spriteSet, float g) {
        super(level, d, e, f);
        this.sprites = spriteSet;
        this.baseGravity = g;
    }

    public void setColor(int i) {
        float f = (float)((i & 0xFF0000) >> 16) / 255.0f;
        float g = (float)((i & 0xFF00) >> 8) / 255.0f;
        float h = (float)((i & 0xFF) >> 0) / 255.0f;
        float j = 1.0f;
        this.setColor(f * 1.0f, g * 1.0f, h * 1.0f);
    }

    public void setFadeColor(int i) {
        this.fadeR = (float)((i & 0xFF0000) >> 16) / 255.0f;
        this.fadeG = (float)((i & 0xFF00) >> 8) / 255.0f;
        this.fadeB = (float)((i & 0xFF) >> 0) / 255.0f;
        this.hasFade = true;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
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
        this.setSpriteFromAge(this.sprites);
        if (this.age > this.lifetime / 2) {
            this.setAlpha(1.0f - ((float)this.age - (float)(this.lifetime / 2)) / (float)this.lifetime);
            if (this.hasFade) {
                this.rCol += (this.fadeR - this.rCol) * 0.2f;
                this.gCol += (this.fadeG - this.gCol) * 0.2f;
                this.bCol += (this.fadeB - this.bCol) * 0.2f;
            }
        }
        this.yd += (double)this.baseGravity;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= (double)this.baseAirFriction;
        this.yd *= (double)this.baseAirFriction;
        this.zd *= (double)this.baseAirFriction;
        if (this.onGround) {
            this.xd *= (double)0.7f;
            this.zd *= (double)0.7f;
        }
    }

    @Override
    public int getLightColor(float f) {
        return 0xF000F0;
    }

    protected void setBaseAirFriction(float f) {
        this.baseAirFriction = f;
    }
}

