package net.minecraft.realms;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

@Environment(EnvType.CLIENT)
public class RealmsBridge extends RealmsScreen {
	private Screen previousScreen;

	public void switchToRealms(Screen screen) {
		this.previousScreen = screen;
		Realms.setScreen(new RealmsMainScreen(this));
	}

	@Nullable
	public RealmsScreenProxy getNotificationScreen(Screen screen) {
		this.previousScreen = screen;
		return new RealmsNotificationsScreen(this).getProxy();
	}

	@Override
	public void init() {
		Minecraft.getInstance().setScreen(this.previousScreen);
	}
}
