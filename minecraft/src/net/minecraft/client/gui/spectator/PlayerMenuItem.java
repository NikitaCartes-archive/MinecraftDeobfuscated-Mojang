package net.minecraft.client.gui.spectator;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.platform.GlStateManager;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.CLIENT)
public class PlayerMenuItem implements SpectatorMenuItem {
	private final GameProfile profile;
	private final ResourceLocation location;

	public PlayerMenuItem(GameProfile gameProfile) {
		this.profile = gameProfile;
		Minecraft minecraft = Minecraft.getInstance();
		Map<Type, MinecraftProfileTexture> map = minecraft.getSkinManager().getInsecureSkinInformation(gameProfile);
		if (map.containsKey(Type.SKIN)) {
			this.location = minecraft.getSkinManager().registerTexture((MinecraftProfileTexture)map.get(Type.SKIN), Type.SKIN);
		} else {
			this.location = DefaultPlayerSkin.getDefaultSkin(Player.createPlayerUUID(gameProfile));
		}
	}

	@Override
	public void selectItem(SpectatorMenu spectatorMenu) {
		Minecraft.getInstance().getConnection().send(new ServerboundTeleportToEntityPacket(this.profile.getId()));
	}

	@Override
	public Component getName() {
		return new TextComponent(this.profile.getName());
	}

	@Override
	public void renderIcon(float f, int i) {
		Minecraft.getInstance().getTextureManager().bind(this.location);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, (float)i / 255.0F);
		GuiComponent.blit(2, 2, 12, 12, 8.0F, 8.0F, 8, 8, 64, 64);
		GuiComponent.blit(2, 2, 12, 12, 40.0F, 8.0F, 8, 8, 64, 64);
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
