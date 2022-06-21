package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@Environment(EnvType.CLIENT)
public class ConfirmLinkScreen extends ConfirmScreen {
	private static final Component COPY_BUTTON_TEXT = Component.translatable("chat.copy");
	private static final Component WARNING_TEXT = Component.translatable("chat.link.warning");
	private final String url;
	private final boolean showWarning;

	public ConfirmLinkScreen(BooleanConsumer booleanConsumer, String string, boolean bl) {
		this(booleanConsumer, confirmMessage(bl), Component.literal(string), string, bl ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_NO, bl);
	}

	public ConfirmLinkScreen(BooleanConsumer booleanConsumer, Component component, String string, boolean bl) {
		this(booleanConsumer, component, string, bl ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_NO, bl);
	}

	public ConfirmLinkScreen(BooleanConsumer booleanConsumer, Component component, String string, Component component2, boolean bl) {
		this(booleanConsumer, component, confirmMessage(bl, string), string, component2, bl);
	}

	public ConfirmLinkScreen(BooleanConsumer booleanConsumer, Component component, Component component2, String string, Component component3, boolean bl) {
		super(booleanConsumer, component, component2);
		this.yesButton = (Component)(bl ? Component.translatable("chat.link.open") : CommonComponents.GUI_YES);
		this.noButton = component3;
		this.showWarning = !bl;
		this.url = string;
	}

	protected static MutableComponent confirmMessage(boolean bl, String string) {
		return confirmMessage(bl).append(" ").append(Component.literal(string));
	}

	protected static MutableComponent confirmMessage(boolean bl) {
		return Component.translatable(bl ? "chat.link.confirmTrusted" : "chat.link.confirm");
	}

	@Override
	protected void addButtons(int i) {
		this.addRenderableWidget(new Button(this.width / 2 - 50 - 105, i, 100, 20, this.yesButton, button -> this.callback.accept(true)));
		this.addRenderableWidget(new Button(this.width / 2 - 50, i, 100, 20, COPY_BUTTON_TEXT, button -> {
			this.copyToClipboard();
			this.callback.accept(false);
		}));
		this.addRenderableWidget(new Button(this.width / 2 - 50 + 105, i, 100, 20, this.noButton, button -> this.callback.accept(false)));
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
