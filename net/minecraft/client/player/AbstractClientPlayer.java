/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.player;

import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import java.util.Locale;
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
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractClientPlayer
extends Player {
    private static final String SKIN_URL_TEMPLATE = "http://skins.minecraft.net/MinecraftSkins/%s.png";
    @Nullable
    private PlayerInfo playerInfo;
    public float elytraRotX;
    public float elytraRotY;
    public float elytraRotZ;
    public final ClientLevel clientLevel;

    public AbstractClientPlayer(ClientLevel clientLevel, GameProfile gameProfile, @Nullable ProfilePublicKey profilePublicKey) {
        super(clientLevel, clientLevel.getSharedSpawnPos(), clientLevel.getSharedSpawnAngle(), gameProfile, profilePublicKey);
        this.clientLevel = clientLevel;
    }

    @Override
    public boolean isSpectator() {
        PlayerInfo playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(this.getGameProfile().getId());
        return playerInfo != null && playerInfo.getGameMode() == GameType.SPECTATOR;
    }

    @Override
    public boolean isCreative() {
        PlayerInfo playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(this.getGameProfile().getId());
        return playerInfo != null && playerInfo.getGameMode() == GameType.CREATIVE;
    }

    public boolean isCapeLoaded() {
        return this.getPlayerInfo() != null;
    }

    @Nullable
    protected PlayerInfo getPlayerInfo() {
        if (this.playerInfo == null) {
            this.playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(this.getUUID());
        }
        return this.playerInfo;
    }

    public boolean isSkinLoaded() {
        PlayerInfo playerInfo = this.getPlayerInfo();
        return playerInfo != null && playerInfo.isSkinLoaded();
    }

    public ResourceLocation getSkinTextureLocation() {
        PlayerInfo playerInfo = this.getPlayerInfo();
        return playerInfo == null ? DefaultPlayerSkin.getDefaultSkin(this.getUUID()) : playerInfo.getSkinLocation();
    }

    @Nullable
    public ResourceLocation getCloakTextureLocation() {
        PlayerInfo playerInfo = this.getPlayerInfo();
        return playerInfo == null ? null : playerInfo.getCapeLocation();
    }

    public boolean isElytraLoaded() {
        return this.getPlayerInfo() != null;
    }

    @Nullable
    public ResourceLocation getElytraTextureLocation() {
        PlayerInfo playerInfo = this.getPlayerInfo();
        return playerInfo == null ? null : playerInfo.getElytraLocation();
    }

    public static void registerSkinTexture(ResourceLocation resourceLocation, String string) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        AbstractTexture abstractTexture = textureManager.getTexture(resourceLocation, MissingTextureAtlasSprite.getTexture());
        if (abstractTexture == MissingTextureAtlasSprite.getTexture()) {
            abstractTexture = new HttpTexture(null, String.format(Locale.ROOT, SKIN_URL_TEMPLATE, StringUtil.stripColor(string)), DefaultPlayerSkin.getDefaultSkin(UUIDUtil.createOfflinePlayerUUID(string)), true, null);
            textureManager.register(resourceLocation, abstractTexture);
        }
    }

    public static ResourceLocation getSkinLocation(String string) {
        return new ResourceLocation("skins/" + Hashing.sha1().hashUnencodedChars(StringUtil.stripColor(string)));
    }

    public String getModelName() {
        PlayerInfo playerInfo = this.getPlayerInfo();
        return playerInfo == null ? DefaultPlayerSkin.getSkinModelName(this.getUUID()) : playerInfo.getModelName();
    }

    public float getFieldOfViewModifier() {
        float f = 1.0f;
        if (this.getAbilities().flying) {
            f *= 1.1f;
        }
        if (this.getAbilities().getWalkingSpeed() == 0.0f || Float.isNaN(f *= ((float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) / this.getAbilities().getWalkingSpeed() + 1.0f) / 2.0f) || Float.isInfinite(f)) {
            f = 1.0f;
        }
        ItemStack itemStack = this.getUseItem();
        if (this.isUsingItem()) {
            if (itemStack.is(Items.BOW)) {
                int i = this.getTicksUsingItem();
                float g = (float)i / 20.0f;
                g = g > 1.0f ? 1.0f : (g *= g);
                f *= 1.0f - g * 0.15f;
            } else if (Minecraft.getInstance().options.getCameraType().isFirstPerson() && this.isScoping()) {
                return 0.1f;
            }
        }
        return Mth.lerp(Minecraft.getInstance().options.fovEffectScale().get().floatValue(), 1.0f, f);
    }
}

