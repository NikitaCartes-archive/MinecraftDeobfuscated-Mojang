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
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@Environment(value=EnvType.CLIENT)
public class TerrainParticle
extends TextureSheetParticle {
    private final BlockPos pos;
    private final float uo;
    private final float vo;

    public TerrainParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, BlockState blockState) {
        this(clientLevel, d, e, f, g, h, i, blockState, BlockPos.containing(d, e, f));
    }

    public TerrainParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, BlockState blockState, BlockPos blockPos) {
        super(clientLevel, d, e, f, g, h, i);
        this.pos = blockPos;
        this.setSprite(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(blockState));
        this.gravity = 1.0f;
        this.rCol = 0.6f;
        this.gCol = 0.6f;
        this.bCol = 0.6f;
        if (!blockState.is(Blocks.GRASS_BLOCK)) {
            int j = Minecraft.getInstance().getBlockColors().getColor(blockState, clientLevel, blockPos, 0);
            this.rCol *= (float)(j >> 16 & 0xFF) / 255.0f;
            this.gCol *= (float)(j >> 8 & 0xFF) / 255.0f;
            this.bCol *= (float)(j & 0xFF) / 255.0f;
        }
        this.quadSize /= 2.0f;
        this.uo = this.random.nextFloat() * 3.0f;
        this.vo = this.random.nextFloat() * 3.0f;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    @Override
    protected float getU0() {
        return this.sprite.getU((this.uo + 1.0f) / 4.0f * 16.0f);
    }

    @Override
    protected float getU1() {
        return this.sprite.getU(this.uo / 4.0f * 16.0f);
    }

    @Override
    protected float getV0() {
        return this.sprite.getV(this.vo / 4.0f * 16.0f);
    }

    @Override
    protected float getV1() {
        return this.sprite.getV((this.vo + 1.0f) / 4.0f * 16.0f);
    }

    @Override
    public int getLightColor(float f) {
        int i = super.getLightColor(f);
        if (i == 0 && this.level.hasChunkAt(this.pos)) {
            return LevelRenderer.getLightColor(this.level, this.pos);
        }
        return i;
    }

    @Environment(value=EnvType.CLIENT)
    public static class Provider
    implements ParticleProvider<BlockParticleOption> {
        @Override
        public Particle createParticle(BlockParticleOption blockParticleOption, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            BlockState blockState = blockParticleOption.getState();
            if (blockState.isAir() || blockState.is(Blocks.MOVING_PISTON)) {
                return null;
            }
            return new TerrainParticle(clientLevel, d, e, f, g, h, i, blockState);
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
            return this.createParticle((BlockParticleOption)particleOptions, clientLevel, d, e, f, g, h, i);
        }
    }
}

