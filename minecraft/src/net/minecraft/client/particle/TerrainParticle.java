package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class TerrainParticle extends TextureSheetParticle {
	private final BlockPos pos;
	private final float uo;
	private final float vo;

	public TerrainParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, BlockState blockState) {
		this(clientLevel, d, e, f, g, h, i, blockState, new BlockPos(d, e, f));
	}

	public TerrainParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, BlockState blockState, BlockPos blockPos) {
		super(clientLevel, d, e, f, g, h, i);
		this.pos = blockPos;
		this.setSprite(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(blockState));
		this.gravity = 1.0F;
		this.rCol = 0.6F;
		this.gCol = 0.6F;
		this.bCol = 0.6F;
		if (!blockState.is(Blocks.GRASS_BLOCK)) {
			int j = Minecraft.getInstance().getBlockColors().getColor(blockState, clientLevel, blockPos, 0);
			this.rCol *= (float)(j >> 16 & 0xFF) / 255.0F;
			this.gCol *= (float)(j >> 8 & 0xFF) / 255.0F;
			this.bCol *= (float)(j & 0xFF) / 255.0F;
		}

		this.quadSize /= 2.0F;
		this.uo = this.random.nextFloat() * 3.0F;
		this.vo = this.random.nextFloat() * 3.0F;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.TERRAIN_SHEET;
	}

	@Override
	protected float getU0() {
		return this.sprite.getU((double)((this.uo + 1.0F) / 4.0F * 16.0F));
	}

	@Override
	protected float getU1() {
		return this.sprite.getU((double)(this.uo / 4.0F * 16.0F));
	}

	@Override
	protected float getV0() {
		return this.sprite.getV((double)(this.vo / 4.0F * 16.0F));
	}

	@Override
	protected float getV1() {
		return this.sprite.getV((double)((this.vo + 1.0F) / 4.0F * 16.0F));
	}

	@Override
	public int getLightColor(float f) {
		int i = super.getLightColor(f);
		return i == 0 && this.level.hasChunkAt(this.pos) ? LevelRenderer.getLightColor(this.level, this.pos) : i;
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<BlockParticleOption> {
		public Particle createParticle(BlockParticleOption blockParticleOption, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			BlockState blockState = blockParticleOption.getState();
			return !blockState.isAir() && !blockState.is(Blocks.MOVING_PISTON) ? new TerrainParticle(clientLevel, d, e, f, g, h, i, blockState) : null;
		}
	}
}
