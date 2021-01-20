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
import net.minecraft.core.particles.DustParticleOptionsBase;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class DustParticleBase<T extends DustParticleOptionsBase>
extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected DustParticleBase(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, T dustParticleOptionsBase, SpriteSet spriteSet) {
        super(clientLevel, d, e, f, g, h, i);
        this.sprites = spriteSet;
        this.xd *= (double)0.1f;
        this.yd *= (double)0.1f;
        this.zd *= (double)0.1f;
        float j = this.random.nextFloat() * 0.4f + 0.6f;
        this.rCol = this.randomizeColor(((DustParticleOptionsBase)dustParticleOptionsBase).getColor().x(), j);
        this.gCol = this.randomizeColor(((DustParticleOptionsBase)dustParticleOptionsBase).getColor().y(), j);
        this.bCol = this.randomizeColor(((DustParticleOptionsBase)dustParticleOptionsBase).getColor().z(), j);
        this.quadSize *= 0.75f * ((DustParticleOptionsBase)dustParticleOptionsBase).getScale();
        int k = (int)(8.0 / (this.random.nextDouble() * 0.8 + 0.2));
        this.lifetime = (int)Math.max((float)k * ((DustParticleOptionsBase)dustParticleOptionsBase).getScale(), 1.0f);
        this.setSpriteFromAge(spriteSet);
    }

    protected float randomizeColor(float f, float g) {
        return (this.random.nextFloat() * 0.2f + 0.8f) * f * g;
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
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        this.setSpriteFromAge(this.sprites);
        this.move(this.xd, this.yd, this.zd);
        if (this.y == this.yo) {
            this.xd *= 1.1;
            this.zd *= 1.1;
        }
        this.xd *= (double)0.96f;
        this.yd *= (double)0.96f;
        this.zd *= (double)0.96f;
        if (this.onGround) {
            this.xd *= (double)0.7f;
            this.zd *= (double)0.7f;
        }
    }
}

