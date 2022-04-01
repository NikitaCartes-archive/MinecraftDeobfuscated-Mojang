package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Environment(EnvType.CLIENT)
public class BreakingItemParticle extends TextureSheetParticle {
	private final float uo;
	private final float vo;

	public BreakingItemParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, ItemStack itemStack) {
		this(clientLevel, d, e, f, itemStack);
		this.xd *= 0.1F;
		this.yd *= 0.1F;
		this.zd *= 0.1F;
		this.xd += g;
		this.yd += h;
		this.zd += i;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.TERRAIN_SHEET;
	}

	protected BreakingItemParticle(ClientLevel clientLevel, double d, double e, double f, ItemStack itemStack) {
		super(clientLevel, d, e, f, 0.0, 0.0, 0.0);
		this.setSprite(Minecraft.getInstance().getItemRenderer().getModel(itemStack, clientLevel, null, 0).getParticleIcon());
		this.gravity = 1.0F;
		this.quadSize /= 2.0F;
		this.uo = this.random.nextFloat() * 3.0F;
		this.vo = this.random.nextFloat() * 3.0F;
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

	@Environment(EnvType.CLIENT)
	public static class Provider implements ParticleProvider<ItemParticleOption> {
		public Particle createParticle(ItemParticleOption itemParticleOption, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			return new BreakingItemParticle(clientLevel, d, e, f, g, h, i, itemParticleOption.getItem());
		}
	}

	@Environment(EnvType.CLIENT)
	public static class SlimeProvider implements ParticleProvider<SimpleParticleType> {
		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			return new BreakingItemParticle(clientLevel, d, e, f, new ItemStack(Items.SLIME_BALL));
		}
	}

	@Environment(EnvType.CLIENT)
	public static class SnowballProvider implements ParticleProvider<SimpleParticleType> {
		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			return new BreakingItemParticle(clientLevel, d, e, f, new ItemStack(Items.SNOWBALL));
		}
	}
}
