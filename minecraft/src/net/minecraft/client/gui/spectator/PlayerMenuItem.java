package net.minecraft.client.gui.spectator;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class PlayerMenuItem implements SpectatorMenuItem {
	private final GameProfile profile;
	private final ResourceLocation location;
	private final Component name;

	public PlayerMenuItem(GameProfile gameProfile) {
		this.profile = gameProfile;
		Minecraft minecraft = Minecraft.getInstance();
		this.location = minecraft.getSkinManager().getInsecureSkinLocation(gameProfile);
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
	public void renderIcon(GuiGraphics guiGraphics, float f, int i) {
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, (float)i / 255.0F);
		PlayerFaceRenderer.draw(guiGraphics, this.location, 2, 2, 12);
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
