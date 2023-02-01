/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.worldselection;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
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
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
class SwitchGrid {
    private static final int DEFAULT_SWITCH_BUTTON_WIDTH = 44;
    private final List<LabeledSwitch> switches;

    SwitchGrid(List<LabeledSwitch> list) {
        this.switches = list;
    }

    public void refreshStates() {
        this.switches.forEach(LabeledSwitch::refreshState);
    }

    public static Builder builder(int i) {
        return new Builder(i);
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder {
        private final int width;
        private final List<SwitchBuilder> switchBuilders = new ArrayList<SwitchBuilder>();
        int paddingLeft;

        public Builder(int i) {
            this.width = i;
        }

        public SwitchBuilder addSwitch(Component component, BooleanSupplier booleanSupplier, Consumer<Boolean> consumer) {
            SwitchBuilder switchBuilder = new SwitchBuilder(component, booleanSupplier, consumer, 44);
            this.switchBuilders.add(switchBuilder);
            return switchBuilder;
        }

        public Builder withPaddingLeft(int i) {
            this.paddingLeft = i;
            return this;
        }

        public SwitchGrid build(Consumer<LayoutElement> consumer) {
            GridLayout gridLayout = new GridLayout().rowSpacing(4);
            gridLayout.addChild(SpacerElement.width(this.width - 44), 0, 0);
            gridLayout.addChild(SpacerElement.width(44), 0, 1);
            ArrayList<LabeledSwitch> list = new ArrayList<LabeledSwitch>();
            int i = 0;
            for (SwitchBuilder switchBuilder : this.switchBuilders) {
                list.add(switchBuilder.build(this, gridLayout, i++, 0));
            }
            gridLayout.arrangeElements();
            consumer.accept(gridLayout);
            return new SwitchGrid(list);
        }
    }

    @Environment(value=EnvType.CLIENT)
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

    @Environment(value=EnvType.CLIENT)
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

        public SwitchBuilder withIsActiveCondition(BooleanSupplier booleanSupplier) {
            this.isActiveCondition = booleanSupplier;
            return this;
        }

        public SwitchBuilder withInfo(Component component) {
            this.info = component;
            return this;
        }

        LabeledSwitch build(Builder builder, GridLayout gridLayout, int i, int j) {
            StringWidget stringWidget = new StringWidget(this.label, Minecraft.getInstance().font).alignLeft();
            gridLayout.addChild(stringWidget, i, j, gridLayout.newCellSettings().align(0.0f, 0.5f).paddingLeft(builder.paddingLeft));
            CycleButton.Builder<Boolean> builder2 = CycleButton.onOffBuilder(this.stateSupplier.getAsBoolean());
            builder2.displayOnlyValue();
            builder2.withCustomNarration(cycleButton -> CommonComponents.joinForNarration(this.label, cycleButton.createDefaultNarrationMessage()));
            if (this.info != null) {
                builder2.withTooltip(boolean_ -> Tooltip.create(this.info));
            }
            CycleButton<Boolean> cycleButton2 = builder2.create(0, 0, this.buttonWidth, 20, Component.empty(), (cycleButton, boolean_) -> this.onClicked.accept((Boolean)boolean_));
            if (this.isActiveCondition != null) {
                cycleButton2.active = this.isActiveCondition.getAsBoolean();
            }
            gridLayout.addChild(cycleButton2, i, j + 1, gridLayout.newCellSettings().alignHorizontallyRight());
            return new LabeledSwitch(cycleButton2, this.stateSupplier, this.isActiveCondition);
        }
    }
}

