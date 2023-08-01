package net.minecraft.client.gui.spectator;

import com.mojang.authlib.GameProfile;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;

@Environment(EnvType.CLIENT)
public class PlayerMenuItem implements SpectatorMenuItem {
	private final GameProfile profile;
	private final Supplier<PlayerSkin> skin;
	private final Component name;

	public PlayerMenuItem(GameProfile gameProfile) {
		this.profile = gameProfile;
		this.skin = Minecraft.getInstance().getSkinManager().lookupInsecure(gameProfile);
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
		PlayerFaceRenderer.draw(guiGraphics, (PlayerSkin)this.skin.get(), 2, 2, 12);
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
