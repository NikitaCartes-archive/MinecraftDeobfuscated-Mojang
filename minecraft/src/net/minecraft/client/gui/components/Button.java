package net.minecraft.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@Environment(EnvType.CLIENT)
public class Button extends AbstractButton {
	public static final Button.OnTooltip NO_TOOLTIP = (button, poseStack, i, j) -> {
	};
	public static final int SMALL_WIDTH = 120;
	public static final int DEFAULT_WIDTH = 150;
	public static final int DEFAULT_HEIGHT = 20;
	protected static final Button.CreateNarration DEFAULT_NARRATION = supplier -> (MutableComponent)supplier.get();
	protected final Button.OnPress onPress;
	protected final Button.OnTooltip onTooltip;
	protected final Button.CreateNarration createNarration;

	public static Button.Builder builder(Component component, Button.OnPress onPress) {
		return new Button.Builder(component, onPress);
	}

	protected Button(int i, int j, int k, int l, Component component, Button.OnPress onPress, Button.OnTooltip onTooltip, Button.CreateNarration createNarration) {
		super(i, j, k, l, component);
		this.onPress = onPress;
		this.onTooltip = onTooltip;
		this.createNarration = createNarration;
	}

	@Override
	public void onPress() {
		this.onPress.onPress(this);
	}

	@Override
	protected MutableComponent createNarrationMessage() {
		return this.createNarration.createNarrationMessage(() -> super.createNarrationMessage());
	}

	@Override
	public void renderButton(PoseStack poseStack, int i, int j, float f) {
		super.renderButton(poseStack, i, j, f);
		if (this.isHoveredOrFocused()) {
			this.renderToolTip(poseStack, i, j);
		}
	}

	@Override
	public void renderToolTip(PoseStack poseStack, int i, int j) {
		this.onTooltip.onTooltip(this, poseStack, i, j);
	}

	@Override
	public void updateNarration(NarrationElementOutput narrationElementOutput) {
		this.defaultButtonNarrationText(narrationElementOutput);
		this.onTooltip.narrateTooltip(component -> narrationElementOutput.add(NarratedElementType.HINT, component));
	}

	@Environment(EnvType.CLIENT)
	public static class Builder {
		private final Component message;
		private final Button.OnPress onPress;
		private Button.OnTooltip onTooltip = Button.NO_TOOLTIP;
		private int x;
		private int y;
		private int width = 150;
		private int height = 20;
		private Button.CreateNarration createNarration = Button.DEFAULT_NARRATION;

		public Builder(Component component, Button.OnPress onPress) {
			this.message = component;
			this.onPress = onPress;
		}

		public Button.Builder pos(int i, int j) {
			this.x = i;
			this.y = j;
			return this;
		}

		public Button.Builder width(int i) {
			this.width = i;
			return this;
		}

		public Button.Builder size(int i, int j) {
			this.width = i;
			this.height = j;
			return this;
		}

		public Button.Builder bounds(int i, int j, int k, int l) {
			return this.pos(i, j).size(k, l);
		}

		public Button.Builder tooltip(Button.OnTooltip onTooltip) {
			this.onTooltip = onTooltip;
			return this;
		}

		public Button.Builder createNarration(Button.CreateNarration createNarration) {
			this.createNarration = createNarration;
			return this;
		}

		public Button build() {
			return new Button(this.x, this.y, this.width, this.height, this.message, this.onPress, this.onTooltip, this.createNarration);
		}
	}

	@Environment(EnvType.CLIENT)
	public interface CreateNarration {
		MutableComponent createNarrationMessage(Supplier<MutableComponent> supplier);
	}

	@Environment(EnvType.CLIENT)
	public interface OnPress {
		void onPress(Button button);
	}

	@Environment(EnvType.CLIENT)
	public interface OnTooltip {
		void onTooltip(Button button, PoseStack poseStack, int i, int j);

		default void narrateTooltip(Consumer<Component> consumer) {
		}
	}
}
