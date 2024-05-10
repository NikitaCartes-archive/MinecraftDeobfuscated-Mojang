package net.minecraft.client.gui.screens.options.controls;

import com.mojang.blaze3d.platform.InputConstants;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class KeyBindsScreen extends OptionsSubScreen {
	private static final Component TITLE = Component.translatable("controls.keybinds.title");
	@Nullable
	public KeyMapping selectedKey;
	public long lastKeySelection;
	private KeyBindsList keyBindsList;
	private Button resetButton;

	public KeyBindsScreen(Screen screen, Options options) {
		super(screen, options, TITLE);
	}

	@Override
	protected void addContents() {
		this.keyBindsList = this.layout.addToContents(new KeyBindsList(this, this.minecraft));
	}

	@Override
	protected void addOptions() {
	}

	@Override
	protected void addFooter() {
		this.resetButton = Button.builder(Component.translatable("controls.resetAll"), button -> {
			for (KeyMapping keyMapping : this.options.keyMappings) {
				keyMapping.setKey(keyMapping.getDefaultKey());
			}

			this.keyBindsList.resetMappingAndUpdateButtons();
		}).build();
		LinearLayout linearLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
		linearLayout.addChild(this.resetButton);
		linearLayout.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).build());
	}

	@Override
	protected void repositionElements() {
		this.layout.arrangeElements();
		this.keyBindsList.updateSize(this.width, this.layout);
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		if (this.selectedKey != null) {
			this.options.setKey(this.selectedKey, InputConstants.Type.MOUSE.getOrCreate(i));
			this.selectedKey = null;
			this.keyBindsList.resetMappingAndUpdateButtons();
			return true;
		} else {
			return super.mouseClicked(d, e, i);
		}
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (this.selectedKey != null) {
			if (i == 256) {
				this.options.setKey(this.selectedKey, InputConstants.UNKNOWN);
			} else {
				this.options.setKey(this.selectedKey, InputConstants.getKey(i, j));
			}

			this.selectedKey = null;
			this.lastKeySelection = Util.getMillis();
			this.keyBindsList.resetMappingAndUpdateButtons();
			return true;
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		boolean bl = false;

		for (KeyMapping keyMapping : this.options.keyMappings) {
			if (!keyMapping.isDefault()) {
				bl = true;
				break;
			}
		}

		this.resetButton.active = bl;
	}
}
