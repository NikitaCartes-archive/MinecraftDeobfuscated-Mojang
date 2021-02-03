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

@Environment(value=EnvType.CLIENT)
public class SimpleAnimatedParticle
extends TextureSheetParticle {
    protected final SpriteSet sprites;
    private float fadeR;
    private float fadeG;
    private float fadeB;
    private boolean hasFade;

    protected SimpleAnimatedParticle(ClientLevel clientLevel, double d, double e, double f, SpriteSet spriteSet, float g) {
        super(clientLevel, d, e, f);
        this.friction = 0.91f;
        this.gravity = g;
        this.sprites = spriteSet;
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
        super.tick();
        this.setSpriteFromAge(this.sprites);
        if (this.age > this.lifetime / 2) {
            this.setAlpha(1.0f - ((float)this.age - (float)(this.lifetime / 2)) / (float)this.lifetime);
            if (this.hasFade) {
                this.rCol += (this.fadeR - this.rCol) * 0.2f;
                this.gCol += (this.fadeG - this.gCol) * 0.2f;
                this.bCol += (this.fadeB - this.bCol) * 0.2f;
            }
        }
    }

    @Override
    public int getLightColor(float f) {
        return 0xF000F0;
    }
}

