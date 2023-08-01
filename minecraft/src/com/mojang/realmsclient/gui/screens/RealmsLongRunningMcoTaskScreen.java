package com.mojang.realmsclient.gui.screens;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.exception.RealmsDefaultUncaughtExceptionHandler;
import com.mojang.realmsclient.gui.ErrorCallback;
import com.mojang.realmsclient.util.task.LongRunningTask;
import java.time.Duration;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.RepeatedNarrator;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsLongRunningMcoTaskScreen extends RealmsScreen implements ErrorCallback {
	private static final RepeatedNarrator REPEATED_NARRATOR = new RepeatedNarrator(Duration.ofSeconds(5L));
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Screen lastScreen;
	private volatile Component title = CommonComponents.EMPTY;
	@Nullable
	private volatile Component errorMessage;
	private volatile boolean aborted;
	private int animTicks;
	private final LongRunningTask task;
	private final int buttonLength = 212;
	private Button cancelOrBackButton;
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
		super(GameNarrator.NO_TITLE);
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
		REPEATED_NARRATOR.narrate(this.minecraft.getNarrator(), this.title);
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
		this.cancelOrBackButton = this.addRenderableWidget(
			Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancelOrBackButtonClicked()).bounds(this.width / 2 - 106, row(12), 212, 20).build()
		);
	}

	private void cancelOrBackButtonClicked() {
		this.aborted = true;
		this.task.abortTask();
		this.minecraft.setScreen(this.lastScreen);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, row(3), 16777215);
		Component component = this.errorMessage;
		if (component == null) {
			guiGraphics.drawCenteredString(this.font, SYMBOLS[this.animTicks % SYMBOLS.length], this.width / 2, row(8), -8355712);
		} else {
			guiGraphics.drawCenteredString(this.font, component, this.width / 2, row(8), 16711680);
		}
	}

	@Override
	public void error(Component component) {
		this.errorMessage = component;
		this.minecraft.getNarrator().sayNow(component);
		this.minecraft
			.execute(
				() -> {
					this.removeWidget(this.cancelOrBackButton);
					this.cancelOrBackButton = this.addRenderableWidget(
						Button.builder(CommonComponents.GUI_BACK, button -> this.cancelOrBackButtonClicked())
							.bounds(this.width / 2 - 106, this.height / 4 + 120 + 12, 200, 20)
							.build()
					);
				}
			);
	}

	public void setTitle(Component component) {
		this.title = component;
	}

	public boolean aborted() {
		return this.aborted;
	}
}
