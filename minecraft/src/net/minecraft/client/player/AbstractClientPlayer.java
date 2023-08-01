package net.minecraft.client.player;

import com.mojang.authlib.GameProfile;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public abstract class AbstractClientPlayer extends Player {
	@Nullable
	private PlayerInfo playerInfo;
	protected Vec3 deltaMovementOnPreviousTick = Vec3.ZERO;
	public float elytraRotX;
	public float elytraRotY;
	public float elytraRotZ;
	public final ClientLevel clientLevel;

	public AbstractClientPlayer(ClientLevel clientLevel, GameProfile gameProfile) {
		super(clientLevel, clientLevel.getSharedSpawnPos(), clientLevel.getSharedSpawnAngle(), gameProfile);
		this.clientLevel = clientLevel;
	}

	@Override
	public boolean isSpectator() {
		PlayerInfo playerInfo = this.getPlayerInfo();
		return playerInfo != null && playerInfo.getGameMode() == GameType.SPECTATOR;
	}

	@Override
	public boolean isCreative() {
		PlayerInfo playerInfo = this.getPlayerInfo();
		return playerInfo != null && playerInfo.getGameMode() == GameType.CREATIVE;
	}

	@Nullable
	protected PlayerInfo getPlayerInfo() {
		if (this.playerInfo == null) {
			this.playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(this.getUUID());
		}

		return this.playerInfo;
	}

	@Override
	public void tick() {
		this.deltaMovementOnPreviousTick = this.getDeltaMovement();
		super.tick();
	}

	public Vec3 getDeltaMovementLerped(float f) {
		return this.deltaMovementOnPreviousTick.lerp(this.getDeltaMovement(), (double)f);
	}

	public PlayerSkin getSkin() {
		PlayerInfo playerInfo = this.getPlayerInfo();
		return playerInfo == null ? DefaultPlayerSkin.get(this.getUUID()) : playerInfo.getSkin();
	}

	public float getFieldOfViewModifier() {
		float f = 1.0F;
		if (this.getAbilities().flying) {
			f *= 1.1F;
		}

		f *= ((float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) / this.getAbilities().getWalkingSpeed() + 1.0F) / 2.0F;
		if (this.getAbilities().getWalkingSpeed() == 0.0F || Float.isNaN(f) || Float.isInfinite(f)) {
			f = 1.0F;
		}

		ItemStack itemStack = this.getUseItem();
		if (this.isUsingItem()) {
			if (itemStack.is(Items.BOW)) {
				int i = this.getTicksUsingItem();
				float g = (float)i / 20.0F;
				if (g > 1.0F) {
					g = 1.0F;
				} else {
					g *= g;
				}

				f *= 1.0F - g * 0.15F;
			} else if (Minecraft.getInstance().options.getCameraType().isFirstPerson() && this.isScoping()) {
				return 0.1F;
			}
		}

		return Mth.lerp(Minecraft.getInstance().options.fovEffectScale().get().floatValue(), 1.0F, f);
	}
}
