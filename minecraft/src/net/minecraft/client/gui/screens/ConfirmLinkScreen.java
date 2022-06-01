package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class ConfirmLinkScreen extends ConfirmScreen {
	private static final Component COPY_BUTTON_TEXT = Component.translatable("chat.copy");
	private static final Component WARNING_TEXT = Component.translatable("chat.link.warning");
	private final String url;
	private final boolean showWarning;

	public ConfirmLinkScreen(BooleanConsumer booleanConsumer, Component component, String string, boolean bl) {
		super(booleanConsumer, component, Component.translatable(bl ? "chat.link.confirmTrusted" : "chat.link.confirm").append(" ").append(Component.literal(string)));
		this.yesButton = (Component)(bl ? Component.translatable("chat.link.open") : CommonComponents.GUI_YES);
		this.noButton = bl ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_NO;
		this.showWarning = !bl;
		this.url = string;
	}

	public ConfirmLinkScreen(BooleanConsumer booleanConsumer, String string, boolean bl) {
		super(booleanConsumer, Component.translatable(bl ? "chat.link.confirmTrusted" : "chat.link.confirm"), Component.literal(string));
		this.yesButton = (Component)(bl ? Component.translatable("chat.link.open") : CommonComponents.GUI_YES);
		this.noButton = bl ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_NO;
		this.showWarning = !bl;
		this.url = string;
	}

	@Override
	protected void addButtons(int i) {
		this.addRenderableWidget(new Button(this.width / 2 - 50 - 105, this.height / 6 + 96, 100, 20, this.yesButton, button -> this.callback.accept(true)));
		this.addRenderableWidget(new Button(this.width / 2 - 50, this.height / 6 + 96, 100, 20, COPY_BUTTON_TEXT, button -> {
			this.copyToClipboard();
			this.callback.accept(false);
		}));
		this.addRenderableWidget(new Button(this.width / 2 - 50 + 105, this.height / 6 + 96, 100, 20, this.noButton, button -> this.callback.accept(false)));
	}

	public void copyToClipboard() {
		this.minecraft.keyboardHandler.setClipboard(this.url);
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		super.render(poseStack, i, j, f);
		if (this.showWarning) {
			drawCenteredString(poseStack, this.font, WARNING_TEXT, this.width / 2, 110, 16764108);
		}
	}
}
