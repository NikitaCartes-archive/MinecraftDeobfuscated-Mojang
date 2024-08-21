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
	public float walkDistO;
	public float walkDist;

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
		this.walkDistO = this.walkDist;
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

	public float getFieldOfViewModifier(boolean bl, float f) {
		float g = 1.0F;
		if (this.getAbilities().flying) {
			g *= 1.1F;
		}

		float h = this.getAbilities().getWalkingSpeed();
		if (h != 0.0F) {
			float i = (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) / h;
			g *= (i + 1.0F) / 2.0F;
		}

		if (this.isUsingItem()) {
			if (this.getUseItem().is(Items.BOW)) {
				float i = Math.min((float)this.getTicksUsingItem() / 20.0F, 1.0F);
				g *= 1.0F - Mth.square(i) * 0.15F;
			} else if (bl && this.isScoping()) {
				return 0.1F;
			}
		}

		return Mth.lerp(f, 1.0F, g);
	}
}
