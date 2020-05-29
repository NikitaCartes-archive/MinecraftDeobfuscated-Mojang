package net.minecraft.realms;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

@Environment(EnvType.CLIENT)
public class DisconnectedRealmsScreen extends RealmsScreen {
	private final String title;
	private final Component reason;
	@Nullable
	private List<FormattedText> lines;
	private final Screen parent;
	private int textHeight;

	public DisconnectedRealmsScreen(Screen screen, String string, Component component) {
		this.parent = screen;
		this.title = I18n.get(string);
		this.reason = component;
	}

	@Override
	public void init() {
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.setConnectedToRealms(false);
		minecraft.getClientPackSource().clearServerPack();
		NarrationHelper.now(this.title + ": " + this.reason.getString());
		this.lines = this.font.split(this.reason, this.width - 50);
		this.textHeight = this.lines.size() * 9;
		this.addButton(
			new Button(this.width / 2 - 100, this.height / 2 + this.textHeight / 2 + 9, 200, 20, CommonComponents.GUI_BACK, button -> minecraft.setScreen(this.parent))
		);
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 256) {
			Minecraft.getInstance().setScreen(this.parent);
			return true;
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		this.drawCenteredString(poseStack, this.font, this.title, this.width / 2, this.height / 2 - this.textHeight / 2 - 9 * 2, 11184810);
		int k = this.height / 2 - this.textHeight / 2;
		if (this.lines != null) {
			for (FormattedText formattedText : this.lines) {
				this.drawCenteredString(poseStack, this.font, formattedText, this.width / 2, k, 16777215);
				k += 9;
			}
		}

		super.render(poseStack, i, j, f);
	}
}
