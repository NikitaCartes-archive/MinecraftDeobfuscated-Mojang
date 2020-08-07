package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;

@Environment(EnvType.CLIENT)
public abstract class RisingParticle extends TextureSheetParticle {
	protected RisingParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
		super(clientLevel, d, e, f, g, h, i);
		this.xd = this.xd * 0.01F + g;
		this.yd = this.yd * 0.01F + h;
		this.zd = this.zd * 0.01F + i;
		this.x = this.x + (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
		this.y = this.y + (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
		this.z = this.z + (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
		this.lifetime = (int)(8.0 / (Math.random() * 0.8 + 0.2)) + 4;
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.age++ >= this.lifetime) {
			this.remove();
		} else {
			this.move(this.xd, this.yd, this.zd);
			this.xd *= 0.96F;
			this.yd *= 0.96F;
			this.zd *= 0.96F;
			if (this.onGround) {
				this.xd *= 0.7F;
				this.zd *= 0.7F;
			}
		}
	}
}
