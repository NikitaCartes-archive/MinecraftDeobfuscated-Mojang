package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

@Environment(EnvType.CLIENT)
public class StationaryItemParticle extends TextureSheetParticle {
	private StationaryItemParticle(ClientLevel clientLevel, double d, double e, double f, ItemLike itemLike) {
		super(clientLevel, d, e, f);
		this.setSprite(Minecraft.getInstance().getItemRenderer().getItemModelShaper().getParticleIcon(itemLike));
		this.gravity = 0.0F;
		this.lifetime = 80;
		this.hasPhysics = false;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.TERRAIN_SHEET;
	}

	@Override
	public float getQuadSize(float f) {
		return 0.5F;
	}

	@Environment(EnvType.CLIENT)
	public static class BarrierProvider implements ParticleProvider<SimpleParticleType> {
		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			return new StationaryItemParticle(clientLevel, d, e, f, Blocks.BARRIER.asItem());
		}
	}

	@Environment(EnvType.CLIENT)
	public static class LightProvider implements ParticleProvider<SimpleParticleType> {
		public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
			return new StationaryItemParticle(clientLevel, d, e, f, Items.LIGHT);
		}
	}
}