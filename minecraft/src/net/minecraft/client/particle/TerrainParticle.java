package net.minecraft.client.particle;

import javax.annotation.Nullable;
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
	private final BlockState blockState;
	private BlockPos pos;
	private final float uo;
	private final float vo;

	public TerrainParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, BlockState blockState) {
		super(clientLevel, d, e, f, g, h, i);
		this.blockState = blockState;
		this.setSprite(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(blockState));
		this.gravity = 1.0F;
		this.rCol = 0.6F;
		this.gCol = 0.6F;
		this.bCol = 0.6F;
		this.quadSize /= 2.0F;
		this.uo = this.random.nextFloat() * 3.0F;
		this.vo = this.random.nextFloat() * 3.0F;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.TERRAIN_SHEET;
	}

	public TerrainParticle init(BlockPos blockPos) {
		this.pos = blockPos;
		if (this.blockState.is(Blocks.GRASS_BLOCK)) {
			return this;
		} else {
			this.multiplyColor(blockPos);
			return this;
		}
	}

	public TerrainParticle init() {
		this.pos = new BlockPos(this.x, this.y, this.z);
		if (this.blockState.is(Blocks.GRASS_BLOCK)) {
			return this;
		} else {
			this.multiplyColor(this.pos);
			return this;
		}
	}

	protected void multiplyColor(@Nullable BlockPos blockPos) {
		int i = Minecraft.getInstance().getBlockColors().getColor(this.blockState, this.level, blockPos, 0);
		this.rCol *= (float)(i >> 16 & 0xFF) / 255.0F;
		this.gCol *= (float)(i >> 8 & 0xFF) / 255.0F;
		this.bCol *= (float)(i & 0xFF) / 255.0F;
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
		int j = 0;
		if (this.level.hasChunkAt(this.pos)) {
			j = LevelRenderer.getLightColor(this.level, this.pos);
		}

		return i == 0 ? j : i;
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<BlockParticleOption> {
		public Particle createParticle(BlockParticleOption blockParticleOption, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			BlockState blockState = blockParticleOption.getState();
			return !blockState.isAir() && !blockState.is(Blocks.MOVING_PISTON) ? new TerrainParticle(clientLevel, d, e, f, g, h, i, blockState).init() : null;
		}
	}
}
