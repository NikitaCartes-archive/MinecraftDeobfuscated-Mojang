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
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

@Environment(value=EnvType.CLIENT)
public class BarrierParticle
extends TextureSheetParticle {
    private BarrierParticle(ClientLevel clientLevel, double d, double e, double f, ItemLike itemLike) {
        super(clientLevel, d, e, f);
        this.setSprite(Minecraft.getInstance().getItemRenderer().getItemModelShaper().getParticleIcon(itemLike));
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
    implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return new BarrierParticle(clientLevel, d, e, f, Blocks.BARRIER.asItem());
        }
    }
}

