/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class TerrainParticle
extends TextureSheetParticle {
    private final BlockState blockState;
    private BlockPos pos;
    private final float uo;
    private final float vo;

    public TerrainParticle(Level level, double d, double e, double f, double g, double h, double i, BlockState blockState) {
        super(level, d, e, f, g, h, i);
        this.blockState = blockState;
        this.setSprite(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(blockState));
        this.gravity = 1.0f;
        this.rCol = 0.6f;
        this.gCol = 0.6f;
        this.bCol = 0.6f;
        this.quadSize /= 2.0f;
        this.uo = this.random.nextFloat() * 3.0f;
        this.vo = this.random.nextFloat() * 3.0f;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    public TerrainParticle init(BlockPos blockPos) {
        this.pos = blockPos;
        if (this.blockState.getBlock() == Blocks.GRASS_BLOCK) {
            return this;
        }
        this.multiplyColor(blockPos);
        return this;
    }

    public TerrainParticle init() {
        this.pos = new BlockPos(this.x, this.y, this.z);
        Block block = this.blockState.getBlock();
        if (block == Blocks.GRASS_BLOCK) {
            return this;
        }
        this.multiplyColor(this.pos);
        return this;
    }

    protected void multiplyColor(@Nullable BlockPos blockPos) {
        int i = Minecraft.getInstance().getBlockColors().getColor(this.blockState, this.level, blockPos, 0);
        this.rCol *= (float)(i >> 16 & 0xFF) / 255.0f;
        this.gCol *= (float)(i >> 8 & 0xFF) / 255.0f;
        this.bCol *= (float)(i & 0xFF) / 255.0f;
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
        int j = 0;
        if (this.level.hasChunkAt(this.pos)) {
            j = LevelRenderer.getLightColor(this.level, this.pos);
        }
        return i == 0 ? j : i;
    }

    @Environment(value=EnvType.CLIENT)
    public static class Provider
    implements ParticleProvider<BlockParticleOption> {
        @Override
        public Particle createParticle(BlockParticleOption blockParticleOption, Level level, double d, double e, double f, double g, double h, double i) {
            BlockState blockState = blockParticleOption.getState();
            if (blockState.isAir() || blockState.getBlock() == Blocks.MOVING_PISTON) {
                return null;
            }
            return new TerrainParticle(level, d, e, f, g, h, i, blockState).init();
        }
    }
}

