package net.minecraft.client.gui.screens.controls;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class KeyBindsScreen extends OptionsSubScreen {
	@Nullable
	public KeyMapping selectedKey;
	public long lastKeySelection;
	private KeyBindsList keyBindsList;
	private Button resetButton;

	public KeyBindsScreen(Screen screen, Options options) {
		super(screen, options, Component.translatable("controls.keybinds.title"));
	}

	@Override
	protected void init() {
		this.keyBindsList = new KeyBindsList(this, this.minecraft);
		this.addWidget(this.keyBindsList);
		this.resetButton = this.addRenderableWidget(Button.builder(Component.translatable("controls.resetAll"), button -> {
			for (KeyMapping keyMapping : this.options.keyMappings) {
				keyMapping.setKey(keyMapping.getDefaultKey());
			}

			KeyMapping.resetMapping();
		}).bounds(this.width / 2 - 155, this.height - 29, 150, 20).build());
		this.addRenderableWidget(
			Button.builder(CommonComponents.GUI_DONE, button -> this.minecraft.setScreen(this.lastScreen))
				.bounds(this.width / 2 - 155 + 160, this.height - 29, 150, 20)
				.build()
		);
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		if (this.selectedKey != null) {
			this.options.setKey(this.selectedKey, InputConstants.Type.MOUSE.getOrCreate(i));
			this.selectedKey = null;
			KeyMapping.resetMapping();
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
			KeyMapping.resetMapping();
			return true;
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		this.keyBindsList.render(poseStack, i, j, f);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 8, 16777215);
		boolean bl = false;

		for (KeyMapping keyMapping : this.options.keyMappings) {
			if (!keyMapping.isDefault()) {
				bl = true;
				break;
			}
		}

		this.resetButton.active = bl;
		super.render(poseStack, i, j, f);
	}
}
