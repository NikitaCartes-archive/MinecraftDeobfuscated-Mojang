package net.minecraft.client.gui.screens.controls;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.MouseSettingsScreen;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public class ControlsScreen extends OptionsSubScreen {
	public KeyMapping selectedKey;
	public long lastKeySelection;
	private ControlList controlList;
	private Button resetButton;

	public ControlsScreen(Screen screen, Options options) {
		super(screen, options, new TranslatableComponent("controls.title"));
	}

	@Override
	protected void init() {
		this.addRenderableWidget(
			new Button(
				this.width / 2 - 155,
				18,
				150,
				20,
				new TranslatableComponent("options.mouse_settings"),
				button -> this.minecraft.setScreen(new MouseSettingsScreen(this, this.options))
			)
		);
		this.addRenderableWidget(Option.AUTO_JUMP.createButton(this.options, this.width / 2 - 155 + 160, 18, 150));
		this.controlList = new ControlList(this, this.minecraft);
		this.addWidget(this.controlList);
		this.resetButton = this.addRenderableWidget(
			new Button(this.width / 2 - 155, this.height - 29, 150, 20, new TranslatableComponent("controls.resetAll"), button -> {
				for (KeyMapping keyMapping : this.options.keyMappings) {
					keyMapping.setKey(keyMapping.getDefaultKey());
				}

				KeyMapping.resetMapping();
			})
		);
		this.addRenderableWidget(
			new Button(this.width / 2 - 155 + 160, this.height - 29, 150, 20, CommonComponents.GUI_DONE, button -> this.minecraft.setScreen(this.lastScreen))
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
		this.controlList.render(poseStack, i, j, f);
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
