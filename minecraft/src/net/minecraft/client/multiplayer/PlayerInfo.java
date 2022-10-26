package net.minecraft.client.multiplayer;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import java.util.Map;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.chat.SignedMessageValidator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;

@Environment(EnvType.CLIENT)
public class PlayerInfo {
	private final GameProfile profile;
	private final Map<Type, ResourceLocation> textureLocations = Maps.newEnumMap(Type.class);
	private GameType gameMode = GameType.DEFAULT_MODE;
	private int latency;
	private boolean pendingTextures;
	@Nullable
	private String skinModel;
	@Nullable
	private Component tabListDisplayName;
	@Nullable
	private RemoteChatSession chatSession;
	private SignedMessageValidator messageValidator;

	public PlayerInfo(GameProfile gameProfile, boolean bl) {
		this.profile = gameProfile;
		this.messageValidator = fallbackMessageValidator(bl);
	}

	public GameProfile getProfile() {
		return this.profile;
	}

	@Nullable
	public RemoteChatSession getChatSession() {
		return this.chatSession;
	}

	public SignedMessageValidator getMessageValidator() {
		return this.messageValidator;
	}

	public boolean hasVerifiableChat() {
		return this.chatSession != null;
	}

	protected void setChatSession(RemoteChatSession remoteChatSession) {
		this.chatSession = remoteChatSession;
		this.messageValidator = remoteChatSession.createMessageValidator();
	}

	protected void clearChatSession(boolean bl) {
		this.chatSession = null;
		this.messageValidator = fallbackMessageValidator(bl);
	}

	private static SignedMessageValidator fallbackMessageValidator(boolean bl) {
		return bl ? SignedMessageValidator.REJECT_ALL : SignedMessageValidator.ACCEPT_UNSIGNED;
	}

	public GameType getGameMode() {
		return this.gameMode;
	}

	protected void setGameMode(GameType gameType) {
		this.gameMode = gameType;
	}

	public int getLatency() {
		return this.latency;
	}

	protected void setLatency(int i) {
		this.latency = i;
	}

	public boolean isCapeLoaded() {
		return this.getCapeLocation() != null;
	}

	public boolean isSkinLoaded() {
		return this.getSkinLocation() != null;
	}

	public String getModelName() {
		return this.skinModel == null ? DefaultPlayerSkin.getSkinModelName(this.profile.getId()) : this.skinModel;
	}

	public ResourceLocation getSkinLocation() {
		this.registerTextures();
		return MoreObjects.firstNonNull((ResourceLocation)this.textureLocations.get(Type.SKIN), DefaultPlayerSkin.getDefaultSkin(this.profile.getId()));
	}

	@Nullable
	public ResourceLocation getCapeLocation() {
		this.registerTextures();
		return (ResourceLocation)this.textureLocations.get(Type.CAPE);
	}

	@Nullable
	public ResourceLocation getElytraLocation() {
		this.registerTextures();
		return (ResourceLocation)this.textureLocations.get(Type.ELYTRA);
	}

	@Nullable
	public PlayerTeam getTeam() {
		return Minecraft.getInstance().level.getScoreboard().getPlayersTeam(this.getProfile().getName());
	}

	protected void registerTextures() {
		synchronized (this) {
			if (!this.pendingTextures) {
				this.pendingTextures = true;
				Minecraft.getInstance().getSkinManager().registerSkins(this.profile, (type, resourceLocation, minecraftProfileTexture) -> {
					this.textureLocations.put(type, resourceLocation);
					if (type == Type.SKIN) {
						this.skinModel = minecraftProfileTexture.getMetadata("model");
						if (this.skinModel == null) {
							this.skinModel = "default";
						}
					}
				}, true);
			}
		}
	}

	public void setTabListDisplayName(@Nullable Component component) {
		this.tabListDisplayName = component;
	}

	@Nullable
	public Component getTabListDisplayName() {
		return this.tabListDisplayName;
	}
}
