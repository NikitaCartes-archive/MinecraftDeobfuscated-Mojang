/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ParticleOptions;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface ParticleProvider<T extends ParticleOptions> {
    @Nullable
    public Particle createParticle(T var1, ClientLevel var2, double var3, double var5, double var7, double var9, double var11, double var13);

    @Environment(value=EnvType.CLIENT)
    public static interface Sprite<T extends ParticleOptions> {
        @Nullable
        public TextureSheetParticle createParticle(T var1, ClientLevel var2, double var3, double var5, double var7, double var9, double var11, double var13);
    }
}

