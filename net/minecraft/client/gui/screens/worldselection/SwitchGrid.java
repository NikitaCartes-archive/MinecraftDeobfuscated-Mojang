/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.worldselection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
        final int width;
        private final List<SwitchBuilder> switchBuilders = new ArrayList<SwitchBuilder>();
        int paddingLeft;
        int rowSpacing = 4;
        int rowCount;
        Optional<InfoUnderneathSettings> infoUnderneath = Optional.empty();

        public Builder(int i) {
            this.width = i;
        }

        void increaseRow() {
            ++this.rowCount;
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

        public Builder withRowSpacing(int i) {
            this.rowSpacing = i;
            return this;
        }

        public SwitchGrid build(Consumer<LayoutElement> consumer) {
            GridLayout gridLayout = new GridLayout().rowSpacing(this.rowSpacing);
            gridLayout.addChild(SpacerElement.width(this.width - 44), 0, 0);
            gridLayout.addChild(SpacerElement.width(44), 0, 1);
            ArrayList<LabeledSwitch> list = new ArrayList<LabeledSwitch>();
            this.rowCount = 0;
            for (SwitchBuilder switchBuilder : this.switchBuilders) {
                list.add(switchBuilder.build(this, gridLayout, 0));
            }
            gridLayout.arrangeElements();
            consumer.accept(gridLayout);
            SwitchGrid switchGrid = new SwitchGrid(list);
            switchGrid.refreshStates();
            return switchGrid;
        }

        public Builder withInfoUnderneath(int i, boolean bl) {
            this.infoUnderneath = Optional.of(new InfoUnderneathSettings(i, bl));
            return this;
        }
    }

    @Environment(value=EnvType.CLIENT)
    record InfoUnderneathSettings(int maxInfoRows, boolean alwaysMaxHeight) {
    }

    @Environment(value=EnvType.CLIENT)
    record LabeledSwitch(CycleButton<Boolean> button, BooleanSupplier stateSupplier, @Nullable BooleanSupplier isActiveCondition) {
        public void refreshState() {
            this.button.setValue(this.stateSupplier.getAsBoolean());
            if (this.isActiveCondition != null) {
                this.button.active = this.isActiveCondition.getAsBoolean();
            }
        }

        @Nullable
        public BooleanSupplier isActiveCondition() {
            return this.isActiveCondition;
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

        LabeledSwitch build(Builder builder, GridLayout gridLayout, int i) {
            boolean bl;
            builder.increaseRow();
            StringWidget stringWidget = new StringWidget(this.label, Minecraft.getInstance().font).alignLeft();
            gridLayout.addChild(stringWidget, builder.rowCount, i, gridLayout.newCellSettings().align(0.0f, 0.5f).paddingLeft(builder.paddingLeft));
            Optional<InfoUnderneathSettings> optional = builder.infoUnderneath;
            CycleButton.Builder<Boolean> builder2 = CycleButton.onOffBuilder(this.stateSupplier.getAsBoolean());
            builder2.displayOnlyValue();
            boolean bl2 = bl = this.info != null && !optional.isPresent();
            if (bl) {
                Tooltip tooltip = Tooltip.create(this.info);
                builder2.withTooltip(boolean_ -> tooltip);
            }
            if (this.info != null && !bl) {
                builder2.withCustomNarration(cycleButton -> CommonComponents.joinForNarration(this.label, cycleButton.createDefaultNarrationMessage(), this.info));
            } else {
                builder2.withCustomNarration(cycleButton -> CommonComponents.joinForNarration(this.label, cycleButton.createDefaultNarrationMessage()));
            }
            CycleButton<Boolean> cycleButton2 = builder2.create(0, 0, this.buttonWidth, 20, Component.empty(), (cycleButton, boolean_) -> this.onClicked.accept((Boolean)boolean_));
            if (this.isActiveCondition != null) {
                cycleButton2.active = this.isActiveCondition.getAsBoolean();
            }
            gridLayout.addChild(cycleButton2, builder.rowCount, i + 1, gridLayout.newCellSettings().alignHorizontallyRight());
            if (this.info != null) {
                optional.ifPresent(infoUnderneathSettings -> {
                    MutableComponent component = this.info.copy().withStyle(ChatFormatting.GRAY);
                    Font font = Minecraft.getInstance().font;
                    MultiLineTextWidget multiLineTextWidget = new MultiLineTextWidget(component, font);
                    multiLineTextWidget.setMaxWidth(builder.width - builder.paddingLeft - this.buttonWidth);
                    multiLineTextWidget.setMaxRows(infoUnderneathSettings.maxInfoRows());
                    builder.increaseRow();
                    int j = infoUnderneathSettings.alwaysMaxHeight ? font.lineHeight * infoUnderneathSettings.maxInfoRows - multiLineTextWidget.getHeight() : 0;
                    gridLayout.addChild(multiLineTextWidget, builder.rowCount, i, gridLayout.newCellSettings().paddingTop(-builder.rowSpacing).paddingBottom(j));
                });
            }
            return new LabeledSwitch(cycleButton2, this.stateSupplier, this.isActiveCondition);
        }
    }
}

