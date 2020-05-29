package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public class BackupConfirmScreen extends Screen {
	private final Screen lastScreen;
	protected final BackupConfirmScreen.Listener listener;
	private final Component description;
	private final boolean promptForCacheErase;
	private final List<FormattedText> lines = Lists.<FormattedText>newArrayList();
	private Checkbox eraseCache;

	public BackupConfirmScreen(Screen screen, BackupConfirmScreen.Listener listener, Component component, Component component2, boolean bl) {
		super(component);
		this.lastScreen = screen;
		this.listener = listener;
		this.description = component2;
		this.promptForCacheErase = bl;
	}

	@Override
	protected void init() {
		super.init();
		this.lines.clear();
		this.lines.addAll(this.font.split(this.description, this.width - 50));
		int i = (this.lines.size() + 1) * 9;
		this.addButton(
			new Button(
				this.width / 2 - 155,
				100 + i,
				150,
				20,
				new TranslatableComponent("selectWorld.backupJoinConfirmButton"),
				button -> this.listener.proceed(true, this.eraseCache.selected())
			)
		);
		this.addButton(
			new Button(
				this.width / 2 - 155 + 160,
				100 + i,
				150,
				20,
				new TranslatableComponent("selectWorld.backupJoinSkipButton"),
				button -> this.listener.proceed(false, this.eraseCache.selected())
			)
		);
		this.addButton(new Button(this.width / 2 - 155 + 80, 124 + i, 150, 20, CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.lastScreen)));
		this.eraseCache = new Checkbox(this.width / 2 - 155 + 80, 76 + i, 150, 20, new TranslatableComponent("selectWorld.backupEraseCache"), false);
		if (this.promptForCacheErase) {
			this.addButton(this.eraseCache);
		}
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		this.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 50, 16777215);
		int k = 70;

		for (FormattedText formattedText : this.lines) {
			this.drawCenteredString(poseStack, this.font, formattedText, this.width / 2, k, 16777215);
			k += 9;
		}

		super.render(poseStack, i, j, f);
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
