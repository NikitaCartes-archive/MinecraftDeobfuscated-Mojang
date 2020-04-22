package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.exception.RealmsDefaultUncaughtExceptionHandler;
import com.mojang.realmsclient.gui.ErrorCallback;
import com.mojang.realmsclient.util.task.LongRunningTask;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsLongRunningMcoTaskScreen extends RealmsScreen implements ErrorCallback {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Screen lastScreen;
	private volatile String title = "";
	private volatile boolean error;
	private volatile Component errorMessage;
	private volatile boolean aborted;
	private int animTicks;
	private final LongRunningTask task;
	private final int buttonLength = 212;
	public static final String[] SYMBOLS = new String[]{
		"▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃",
		"_ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄",
		"_ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅",
		"_ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆",
		"_ _ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇",
		"_ _ _ _ _ ▃ ▄ ▅ ▆ ▇ █",
		"_ _ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇",
		"_ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆",
		"_ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅",
		"_ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄",
		"▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃",
		"▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _",
		"▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _",
		"▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _",
		"▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _ _",
		"█ ▇ ▆ ▅ ▄ ▃ _ _ _ _ _",
		"▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _ _",
		"▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _",
		"▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _",
		"▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _"
	};

	public RealmsLongRunningMcoTaskScreen(Screen screen, LongRunningTask longRunningTask) {
		this.lastScreen = screen;
		this.task = longRunningTask;
		longRunningTask.setScreen(this);
		Thread thread = new Thread(longRunningTask, "Realms-long-running-task");
		thread.setUncaughtExceptionHandler(new RealmsDefaultUncaughtExceptionHandler(LOGGER));
		thread.start();
	}

	@Override
	public void tick() {
		super.tick();
		NarrationHelper.repeatedly(this.title);
		this.animTicks++;
		this.task.tick();
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 256) {
			this.cancelOrBackButtonClicked();
			return true;
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	@Override
	public void init() {
		this.task.init();
		this.addButton(new Button(this.width / 2 - 106, row(12), 212, 20, CommonComponents.GUI_CANCEL, button -> this.cancelOrBackButtonClicked()));
	}

	private void cancelOrBackButtonClicked() {
		this.aborted = true;
		this.task.abortTask();
		this.minecraft.setScreen(this.lastScreen);
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		this.drawCenteredString(poseStack, this.font, this.title, this.width / 2, row(3), 16777215);
		if (!this.error) {
			this.drawCenteredString(poseStack, this.font, SYMBOLS[this.animTicks % SYMBOLS.length], this.width / 2, row(8), 8421504);
		}

		if (this.error) {
			this.drawCenteredString(poseStack, this.font, this.errorMessage, this.width / 2, row(8), 16711680);
		}

		super.render(poseStack, i, j, f);
	}

	@Override
	public void error(Component component) {
		this.error = true;
		this.errorMessage = component;
		NarrationHelper.now(component.getString());
		this.buttonsClear();
		this.addButton(new Button(this.width / 2 - 106, this.height / 4 + 120 + 12, 200, 20, CommonComponents.GUI_BACK, button -> this.cancelOrBackButtonClicked()));
	}

	public void buttonsClear() {
		Set<GuiEventListener> set = Sets.<GuiEventListener>newHashSet(this.buttons);
		this.children.removeIf(set::contains);
		this.buttons.clear();
	}

	public void setTitle(String string) {
		this.title = string;
	}

	public boolean aborted() {
		return this.aborted;
	}
}
