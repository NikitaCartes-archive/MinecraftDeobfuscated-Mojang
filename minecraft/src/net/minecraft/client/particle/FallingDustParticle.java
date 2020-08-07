package net.minecraft.client.particle;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class FallingDustParticle extends TextureSheetParticle {
	private final float rotSpeed;
	private final SpriteSet sprites;

	private FallingDustParticle(ClientLevel clientLevel, double d, double e, double f, float g, float h, float i, SpriteSet spriteSet) {
		super(clientLevel, d, e, f);
		this.sprites = spriteSet;
		this.rCol = g;
		this.gCol = h;
		this.bCol = i;
		float j = 0.9F;
		this.quadSize *= 0.67499995F;
		int k = (int)(32.0 / (Math.random() * 0.8 + 0.2));
		this.lifetime = (int)Math.max((float)k * 0.9F, 1.0F);
		this.setSpriteFromAge(spriteSet);
		this.rotSpeed = ((float)Math.random() - 0.5F) * 0.1F;
		this.roll = (float)Math.random() * (float) (Math.PI * 2);
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	@Override
	public float getQuadSize(float f) {
		return this.quadSize * Mth.clamp(((float)this.age + f) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.age++ >= this.lifetime) {
			this.remove();
		} else {
			this.setSpriteFromAge(this.sprites);
			this.oRoll = this.roll;
			this.roll = this.roll + (float) Math.PI * this.rotSpeed * 2.0F;
			if (this.onGround) {
				this.oRoll = this.roll = 0.0F;
			}

			this.move(this.xd, this.yd, this.zd);
			this.yd -= 0.003F;
			this.yd = Math.max(this.yd, -0.14F);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<BlockParticleOption> {
		private final SpriteSet sprite;

		public Provider(SpriteSet spriteSet) {
			this.sprite = spriteSet;
		}

		@Nullable
		public Particle createParticle(BlockParticleOption blockParticleOption, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			BlockState blockState = blockParticleOption.getState();
			if (!blockState.isAir() && blockState.getRenderShape() == RenderShape.INVISIBLE) {
				return null;
			} else {
				BlockPos blockPos = new BlockPos(d, e, f);
				int j = Minecraft.getInstance().getBlockColors().getColor(blockState, clientLevel, blockPos);
				if (blockState.getBlock() instanceof FallingBlock) {
					j = ((FallingBlock)blockState.getBlock()).getDustColor(blockState, clientLevel, blockPos);
				}

				float k = (float)(j >> 16 & 0xFF) / 255.0F;
				float l = (float)(j >> 8 & 0xFF) / 255.0F;
				float m = (float)(j & 0xFF) / 255.0F;
				return new FallingDustParticle(clientLevel, d, e, f, k, l, m, this.sprite);
			}
		}
	}
}
