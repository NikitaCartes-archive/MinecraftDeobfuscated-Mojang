/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.block.state.BlockState;

@Environment(value=EnvType.CLIENT)
public class BlockMarker
extends TextureSheetParticle {
    BlockMarker(ClientLevel clientLevel, double d, double e, double f, BlockState blockState) {
        super(clientLevel, d, e, f);
        this.setSprite(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(blockState));
        this.gravity = 0.0f;
        this.lifetime = 80;
        this.hasPhysics = false;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    @Override
    public float getQuadSize(float f) {
        return 0.5f;
    }

    @Environment(value=EnvType.CLIENT)
    public static class Provider
    implements ParticleProvider<BlockParticleOption> {
        @Override
        public Particle createParticle(BlockParticleOption blockParticleOption, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return new BlockMarker(clientLevel, d, e, f, blockParticleOption.getState());
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return this.createParticle((BlockParticleOption)particleOptions, clientLevel, d, e, f, g, h, i);
        }
    }
}

