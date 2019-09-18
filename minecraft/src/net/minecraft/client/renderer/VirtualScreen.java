package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.platform.Window;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public final class VirtualScreen implements AutoCloseable {
	private final Minecraft minecraft;
	private final ScreenManager screenManager;

	public VirtualScreen(Minecraft minecraft) {
		this.minecraft = minecraft;
		this.screenManager = new ScreenManager(Monitor::new);
	}

	public Window newWindow(DisplayData displayData, @Nullable String string, String string2) {
		return new Window(this.minecraft, this.screenManager, displayData, string, string2);
	}

	public void close() {
		this.screenManager.shutdown();
	}
}
