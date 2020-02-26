package net.minecraft.client.resources.sounds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;

@Environment(EnvType.CLIENT)
public class RidingMinecartSoundInstance extends AbstractTickableSoundInstance {
	private final Player player;
	private final AbstractMinecart minecart;

	public RidingMinecartSoundInstance(Player player, AbstractMinecart abstractMinecart) {
		super(SoundEvents.MINECART_INSIDE, SoundSource.NEUTRAL);
		this.player = player;
		this.minecart = abstractMinecart;
		this.attenuation = SoundInstance.Attenuation.NONE;
		this.looping = true;
		this.delay = 0;
		this.volume = 0.0F;
	}

	@Override
	public boolean canStartSilent() {
		return true;
	}

	@Override
	public void tick() {
		if (!this.minecart.removed && this.player.isPassenger() && this.player.getVehicle() == this.minecart) {
			float f = Mth.sqrt(Entity.getHorizontalDistanceSqr(this.minecart.getDeltaMovement()));
			if ((double)f >= 0.01) {
				this.volume = 0.0F + Mth.clamp(f, 0.0F, 1.0F) * 0.75F;
			} else {
				this.volume = 0.0F;
			}
		} else {
			this.stop();
		}
	}
}
