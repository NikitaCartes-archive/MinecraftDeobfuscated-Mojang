/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.spectator;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.resources.ResourceLocation;

@Environment(value=EnvType.CLIENT)
public class PlayerMenuItem
implements SpectatorMenuItem {
    private final GameProfile profile;
    private final ResourceLocation location;
    private final Component name;

    public PlayerMenuItem(GameProfile gameProfile) {
        this.profile = gameProfile;
        Minecraft minecraft = Minecraft.getInstance();
        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager().getInsecureSkinInformation(gameProfile);
        this.location = map.containsKey((Object)MinecraftProfileTexture.Type.SKIN) ? minecraft.getSkinManager().registerTexture(map.get((Object)MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN) : DefaultPlayerSkin.getDefaultSkin(UUIDUtil.getOrCreatePlayerUUID(gameProfile));
        this.name = Component.literal(gameProfile.getName());
    }

    @Override
    public void selectItem(SpectatorMenu spectatorMenu) {
        Minecraft.getInstance().getConnection().send(new ServerboundTeleportToEntityPacket(this.profile.getId()));
    }

    @Override
    public Component getName() {
        return this.name;
    }

    @Override
    public void renderIcon(PoseStack poseStack, float f, int i) {
        RenderSystem.setShaderTexture(0, this.location);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, (float)i / 255.0f);
        PlayerFaceRenderer.draw(poseStack, 2, 2, 12);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

