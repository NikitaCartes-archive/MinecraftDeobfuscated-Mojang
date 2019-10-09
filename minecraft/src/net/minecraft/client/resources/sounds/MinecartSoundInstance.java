package net.minecraft.client.resources.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;

@Environment(EnvType.CLIENT)
public class MinecartSoundInstance extends AbstractTickableSoundInstance {
	private final AbstractMinecart minecart;
	private float pitch = 0.0F;

	public MinecartSoundInstance(AbstractMinecart abstractMinecart) {
		super(SoundEvents.MINECART_RIDING, SoundSource.NEUTRAL);
		this.minecart = abstractMinecart;
		this.looping = true;
		this.delay = 0;
		this.volume = 0.0F;
		this.x = (float)abstractMinecart.getX();
		this.y = (float)abstractMinecart.getY();
		this.z = (float)abstractMinecart.getZ();
	}

	@Override
	public boolean canStartSilent() {
		return true;
	}

	@Override
	public void tick() {
		if (this.minecart.removed) {
			this.stopped = true;
		} else {
			this.x = (float)this.minecart.getX();
			this.y = (float)this.minecart.getY();
			this.z = (float)this.minecart.getZ();
			float f = Mth.sqrt(Entity.getHorizontalDistanceSqr(this.minecart.getDeltaMovement()));
			if ((double)f >= 0.01) {
				this.pitch = Mth.clamp(this.pitch + 0.0025F, 0.0F, 1.0F);
				this.volume = Mth.lerp(Mth.clamp(f, 0.0F, 0.5F), 0.0F, 0.7F);
			} else {
				this.pitch = 0.0F;
				this.volume = 0.0F;
			}
		}
	}
}
