package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class BackupConfirmScreen extends Screen {
	private final Screen lastScreen;
	protected final BackupConfirmScreen.Listener listener;
	private final Component description;
	private final boolean promptForCacheErase;
	private final List<String> lines = Lists.<String>newArrayList();
	private final String eraseCacheText;
	private final String backupButton;
	private final String continueButton;
	private final String cancelButton;
	private Checkbox eraseCache;

	public BackupConfirmScreen(Screen screen, BackupConfirmScreen.Listener listener, Component component, Component component2, boolean bl) {
		super(component);
		this.lastScreen = screen;
		this.listener = listener;
		this.description = component2;
		this.promptForCacheErase = bl;
		this.eraseCacheText = I18n.get("selectWorld.backupEraseCache");
		this.backupButton = I18n.get("selectWorld.backupJoinConfirmButton");
		this.continueButton = I18n.get("selectWorld.backupJoinSkipButton");
		this.cancelButton = I18n.get("gui.cancel");
	}

	@Override
	protected void init() {
		super.init();
		this.lines.clear();
		this.lines.addAll(this.font.split(this.description.getColoredString(), this.width - 50));
		int i = (this.lines.size() + 1) * 9;
		this.addButton(new Button(this.width / 2 - 155, 100 + i, 150, 20, this.backupButton, button -> this.listener.proceed(true, this.eraseCache.selected())));
		this.addButton(
			new Button(this.width / 2 - 155 + 160, 100 + i, 150, 20, this.continueButton, button -> this.listener.proceed(false, this.eraseCache.selected()))
		);
		this.addButton(new Button(this.width / 2 - 155 + 80, 124 + i, 150, 20, this.cancelButton, button -> this.minecraft.setScreen(this.lastScreen)));
		this.eraseCache = new Checkbox(this.width / 2 - 155 + 80, 76 + i, 150, 20, this.eraseCacheText, false);
		if (this.promptForCacheErase) {
			this.addButton(this.eraseCache);
		}
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 50, 16777215);
		int k = 70;

		for (String string : this.lines) {
			this.drawCenteredString(this.font, string, this.width / 2, k, 16777215);
			k += 9;
		}

		super.render(i, j, f);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 256) {
			this.minecraft.setScreen(this.lastScreen);
			return true;
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	@Environment(EnvType.CLIENT)
	public interface Listener {
		void proceed(boolean bl, boolean bl2);
	}
}
