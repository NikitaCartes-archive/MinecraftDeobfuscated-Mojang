package net.minecraft.client.gui.screens.worldselection;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
class SwitchGrid {
	private static final int DEFAULT_SWITCH_BUTTON_WIDTH = 44;
	private final List<SwitchGrid.LabeledSwitch> switches;

	SwitchGrid(List<SwitchGrid.LabeledSwitch> list) {
		this.switches = list;
	}

	public void refreshStates() {
		this.switches.forEach(SwitchGrid.LabeledSwitch::refreshState);
	}

	public static SwitchGrid.Builder builder(int i) {
		return new SwitchGrid.Builder(i);
	}

	@Environment(EnvType.CLIENT)
	public static class Builder {
		private final int width;
		private final List<SwitchGrid.SwitchBuilder> switchBuilders = new ArrayList();
		int paddingLeft;

		public Builder(int i) {
			this.width = i;
		}

		public SwitchGrid.SwitchBuilder addSwitch(Component component, BooleanSupplier booleanSupplier, Consumer<Boolean> consumer) {
			SwitchGrid.SwitchBuilder switchBuilder = new SwitchGrid.SwitchBuilder(component, booleanSupplier, consumer, 44);
			this.switchBuilders.add(switchBuilder);
			return switchBuilder;
		}

		public SwitchGrid.Builder withPaddingLeft(int i) {
			this.paddingLeft = i;
			return this;
		}

		public SwitchGrid build(Consumer<LayoutElement> consumer) {
			GridLayout gridLayout = new GridLayout().rowSpacing(4);
			gridLayout.addChild(SpacerElement.width(this.width - 44), 0, 0);
			gridLayout.addChild(SpacerElement.width(44), 0, 1);
			List<SwitchGrid.LabeledSwitch> list = new ArrayList();
			int i = 0;

			for (SwitchGrid.SwitchBuilder switchBuilder : this.switchBuilders) {
				list.add(switchBuilder.build(this, gridLayout, i++, 0));
			}

			gridLayout.arrangeElements();
			consumer.accept(gridLayout);
			return new SwitchGrid(list);
		}
	}

	@Environment(EnvType.CLIENT)
	static class LabeledSwitch {
		private final CycleButton<Boolean> button;
		private final BooleanSupplier stateSupplier;
		@Nullable
		private final BooleanSupplier isActiveCondition;

		public LabeledSwitch(CycleButton<Boolean> cycleButton, BooleanSupplier booleanSupplier, @Nullable BooleanSupplier booleanSupplier2) {
			this.button = cycleButton;
			this.stateSupplier = booleanSupplier;
			this.isActiveCondition = booleanSupplier2;
		}

		public void refreshState() {
			this.button.setValue(this.stateSupplier.getAsBoolean());
			if (this.isActiveCondition != null) {
				this.button.active = this.isActiveCondition.getAsBoolean();
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public static class SwitchBuilder {
		private final Component label;
		private final BooleanSupplier stateSupplier;
		private final Consumer<Boolean> onClicked;
		@Nullable
		private Component info;
		@Nullable
		private BooleanSupplier isActiveCondition;
		private final int buttonWidth;

		SwitchBuilder(Component component, BooleanSupplier booleanSupplier, Consumer<Boolean> consumer, int i) {
			this.label = component;
			this.stateSupplier = booleanSupplier;
			this.onClicked = consumer;
			this.buttonWidth = i;
		}

		public SwitchGrid.SwitchBuilder withIsActiveCondition(BooleanSupplier booleanSupplier) {
			this.isActiveCondition = booleanSupplier;
			return this;
		}

		public SwitchGrid.SwitchBuilder withInfo(Component component) {
			this.info = component;
			return this;
		}

		SwitchGrid.LabeledSwitch build(SwitchGrid.Builder builder, GridLayout gridLayout, int i, int j) {
			StringWidget stringWidget = new StringWidget(this.label, Minecraft.getInstance().font).alignLeft();
			gridLayout.addChild(stringWidget, i, j, gridLayout.newCellSettings().align(0.0F, 0.5F).paddingLeft(builder.paddingLeft));
			CycleButton.Builder<Boolean> builder2 = CycleButton.onOffBuilder(this.stateSupplier.getAsBoolean());
			builder2.displayOnlyValue();
			builder2.withCustomNarration(cycleButtonx -> CommonComponents.joinForNarration(this.label, cycleButtonx.createDefaultNarrationMessage()));
			if (this.info != null) {
				builder2.withTooltip(boolean_ -> Tooltip.create(this.info));
			}

			CycleButton<Boolean> cycleButton = builder2.create(
				0, 0, this.buttonWidth, 20, Component.empty(), (cycleButtonx, boolean_) -> this.onClicked.accept(boolean_)
			);
			if (this.isActiveCondition != null) {
				cycleButton.active = this.isActiveCondition.getAsBoolean();
			}

			gridLayout.addChild(cycleButton, i, j + 1, gridLayout.newCellSettings().alignHorizontallyRight());
			return new SwitchGrid.LabeledSwitch(cycleButton, this.stateSupplier, this.isActiveCondition);
		}
	}
}
