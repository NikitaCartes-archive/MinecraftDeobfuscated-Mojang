package net.minecraft.client.player;

import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import java.util.Locale;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.voting.rules.Rules;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public abstract class AbstractClientPlayer extends Player {
	private static final String SKIN_URL_TEMPLATE = "http://skins.minecraft.net/MinecraftSkins/%s.png";
	@Nullable
	private PlayerInfo playerInfo;
	@Nullable
	private PlayerInfo visiblePlayerInfo;
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

	public boolean isCapeLoaded() {
		return this.getVisiblePlayerInfo() != null;
	}

	@Nullable
	protected PlayerInfo getVisiblePlayerInfo() {
		GameProfile gameProfile = this.transform.playerSkin();
		if (gameProfile == null) {
			return this.getPlayerInfo();
		} else {
			if (this.visiblePlayerInfo == null || !this.visiblePlayerInfo.getProfile().equals(gameProfile)) {
				this.visiblePlayerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(gameProfile.getId());
				if (this.visiblePlayerInfo == null) {
					this.visiblePlayerInfo = new PlayerInfo(gameProfile, false);
				}
			}

			return this.visiblePlayerInfo;
		}
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

	public boolean isSkinLoaded() {
		PlayerInfo playerInfo = this.getVisiblePlayerInfo();
		return playerInfo != null && playerInfo.isSkinLoaded();
	}

	public ResourceLocation getSkinTextureLocation() {
		if (Rules.ANONYMIZE_SKINS.get()) {
			return DefaultPlayerSkin.getDefaultSkin(this.getUUID());
		} else {
			PlayerInfo playerInfo = this.getVisiblePlayerInfo();
			return playerInfo == null ? DefaultPlayerSkin.getDefaultSkin(this.getUUID()) : playerInfo.getSkinLocation();
		}
	}

	@Nullable
	public ResourceLocation getCloakTextureLocation() {
		PlayerInfo playerInfo = this.getVisiblePlayerInfo();
		return playerInfo == null ? null : playerInfo.getCapeLocation();
	}

	public boolean isElytraLoaded() {
		return this.getVisiblePlayerInfo() != null;
	}

	@Nullable
	public ResourceLocation getElytraTextureLocation() {
		PlayerInfo playerInfo = this.getVisiblePlayerInfo();
		return playerInfo == null ? null : playerInfo.getElytraLocation();
	}

	public static void registerSkinTexture(ResourceLocation resourceLocation, String string) {
		TextureManager textureManager = Minecraft.getInstance().getTextureManager();
		AbstractTexture abstractTexture = textureManager.getTexture(resourceLocation, MissingTextureAtlasSprite.getTexture());
		if (abstractTexture == MissingTextureAtlasSprite.getTexture()) {
			AbstractTexture var4 = new HttpTexture(
				null,
				String.format(Locale.ROOT, "http://skins.minecraft.net/MinecraftSkins/%s.png", StringUtil.stripColor(string)),
				DefaultPlayerSkin.getDefaultSkin(UUIDUtil.createOfflinePlayerUUID(string)),
				true,
				null
			);
			textureManager.register(resourceLocation, var4);
		}
	}

	public static ResourceLocation getSkinLocation(String string) {
		return new ResourceLocation("skins/" + Hashing.sha1().hashUnencodedChars(StringUtil.stripColor(string)));
	}

	public String getModelName() {
		PlayerInfo playerInfo = this.getVisiblePlayerInfo();
		return playerInfo == null ? DefaultPlayerSkin.getSkinModelName(this.getUUID()) : playerInfo.getModelName();
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
