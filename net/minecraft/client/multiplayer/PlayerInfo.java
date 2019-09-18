/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.multiplayer;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class PlayerInfo {
    private final GameProfile profile;
    private final Map<MinecraftProfileTexture.Type, ResourceLocation> textureLocations = Maps.newEnumMap(MinecraftProfileTexture.Type.class);
    private GameType gameMode;
    private int latency;
    private boolean pendingTextures;
    @Nullable
    private String skinModel;
    @Nullable
    private Component tabListDisplayName;
    private int lastHealth;
    private int displayHealth;
    private long lastHealthTime;
    private long healthBlinkTime;
    private long renderVisibilityId;

    public PlayerInfo(ClientboundPlayerInfoPacket.PlayerUpdate playerUpdate) {
        this.profile = playerUpdate.getProfile();
        this.gameMode = playerUpdate.getGameMode();
        this.latency = playerUpdate.getLatency();
        this.tabListDisplayName = playerUpdate.getDisplayName();
    }

    public GameProfile getProfile() {
        return this.profile;
    }

    @Nullable
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

    public boolean isSkinLoaded() {
        return this.getSkinLocation() != null;
    }

    public String getModelName() {
        if (this.skinModel == null) {
            return DefaultPlayerSkin.getSkinModelName(this.profile.getId());
        }
        return this.skinModel;
    }

    public ResourceLocation getSkinLocation() {
        this.registerTextures();
        return MoreObjects.firstNonNull(this.textureLocations.get((Object)MinecraftProfileTexture.Type.SKIN), DefaultPlayerSkin.getDefaultSkin(this.profile.getId()));
    }

    @Nullable
    public ResourceLocation getCapeLocation() {
        this.registerTextures();
        return this.textureLocations.get((Object)MinecraftProfileTexture.Type.CAPE);
    }

    @Nullable
    public ResourceLocation getElytraLocation() {
        this.registerTextures();
        return this.textureLocations.get((Object)MinecraftProfileTexture.Type.ELYTRA);
    }

    @Nullable
    public PlayerTeam getTeam() {
        return Minecraft.getInstance().level.getScoreboard().getPlayersTeam(this.getProfile().getName());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void registerTextures() {
        PlayerInfo playerInfo = this;
        synchronized (playerInfo) {
            if (!this.pendingTextures) {
                this.pendingTextures = true;
                Minecraft.getInstance().getSkinManager().registerSkins(this.profile, (type, resourceLocation, minecraftProfileTexture) -> {
                    this.textureLocations.put(type, resourceLocation);
                    if (type == MinecraftProfileTexture.Type.SKIN) {
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

    public int getLastHealth() {
        return this.lastHealth;
    }

    public void setLastHealth(int i) {
        this.lastHealth = i;
    }

    public int getDisplayHealth() {
        return this.displayHealth;
    }

    public void setDisplayHealth(int i) {
        this.displayHealth = i;
    }

    public long getLastHealthTime() {
        return this.lastHealthTime;
    }

    public void setLastHealthTime(long l) {
        this.lastHealthTime = l;
    }

    public long getHealthBlinkTime() {
        return this.healthBlinkTime;
    }

    public void setHealthBlinkTime(long l) {
        this.healthBlinkTime = l;
    }

    public long getRenderVisibilityId() {
        return this.renderVisibilityId;
    }

    public void setRenderVisibilityId(long l) {
        this.renderVisibilityId = l;
    }
}

