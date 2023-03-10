/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class EditGameRulesScreen
extends Screen {
    private final Consumer<Optional<GameRules>> exitCallback;
    private RuleList rules;
    private final Set<RuleEntry> invalidEntries = Sets.newHashSet();
    private Button doneButton;
    @Nullable
    private List<FormattedCharSequence> tooltip;
    private final GameRules gameRules;

    public EditGameRulesScreen(GameRules gameRules, Consumer<Optional<GameRules>> consumer) {
        super(Component.translatable("editGamerule.title"));
        this.gameRules = gameRules;
        this.exitCallback = consumer;
    }

    @Override
    protected void init() {
        this.rules = new RuleList(this.gameRules);
        this.addWidget(this.rules);
        GridLayout.RowHelper rowHelper = new GridLayout().columnSpacing(10).createRowHelper(2);
        this.doneButton = rowHelper.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.exitCallback.accept(Optional.of(this.gameRules))).build());
        rowHelper.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> this.exitCallback.accept(Optional.empty())).build());
        rowHelper.getGrid().visitWidgets(guiEventListener -> {
            AbstractWidget cfr_ignored_0 = (AbstractWidget)this.addRenderableWidget(guiEventListener);
        });
        rowHelper.getGrid().setPosition(this.width / 2 - 155, this.height - 28);
        rowHelper.getGrid().arrangeElements();
    }

    @Override
    public void onClose() {
        this.exitCallback.accept(Optional.empty());
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.tooltip = null;
        this.rules.render(poseStack, i, j, f);
        EditGameRulesScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(poseStack, i, j, f);
    }

    private void updateDoneButton() {
        this.doneButton.active = this.invalidEntries.isEmpty();
    }

    void markInvalid(RuleEntry ruleEntry) {
        this.invalidEntries.add(ruleEntry);
        this.updateDoneButton();
    }

    void clearInvalid(RuleEntry ruleEntry) {
        this.invalidEntries.remove(ruleEntry);
        this.updateDoneButton();
    }

    @Environment(value=EnvType.CLIENT)
    public class RuleList
    extends ContainerObjectSelectionList<RuleEntry> {
        public RuleList(final GameRules gameRules) {
            super(EditGameRulesScreen.this.minecraft, EditGameRulesScreen.this.width, EditGameRulesScreen.this.height, 43, EditGameRulesScreen.this.height - 32, 24);
            final HashMap map = Maps.newHashMap();
            GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor(){

                @Override
                public void visitBoolean(GameRules.Key<GameRules.BooleanValue> key, GameRules.Type<GameRules.BooleanValue> type) {
                    this.addEntry(key, (component, list, string, booleanValue) -> new BooleanRuleEntry(component, list, string, (GameRules.BooleanValue)booleanValue));
                }

                @Override
                public void visitInteger(GameRules.Key<GameRules.IntegerValue> key, GameRules.Type<GameRules.IntegerValue> type) {
                    this.addEntry(key, (component, list, string, integerValue) -> new IntegerRuleEntry(component, list, string, (GameRules.IntegerValue)integerValue));
                }

                private <T extends GameRules.Value<T>> void addEntry(GameRules.Key<T> key, EntryFactory<T> entryFactory) {
                    Object string3;
                    ImmutableCollection list;
                    MutableComponent component = Component.translatable(key.getDescriptionId());
                    MutableComponent component2 = Component.literal(key.getId()).withStyle(ChatFormatting.YELLOW);
                    T value = gameRules.getRule(key);
                    String string = ((GameRules.Value)value).serialize();
                    MutableComponent component3 = Component.translatable("editGamerule.default", Component.literal(string)).withStyle(ChatFormatting.GRAY);
                    String string2 = key.getDescriptionId() + ".description";
                    if (I18n.exists(string2)) {
                        ImmutableCollection.Builder builder = ImmutableList.builder().add(component2.getVisualOrderText());
                        MutableComponent component4 = Component.translatable(string2);
                        EditGameRulesScreen.this.font.split(component4, 150).forEach(((ImmutableList.Builder)builder)::add);
                        list = ((ImmutableList.Builder)((ImmutableList.Builder)builder).add(component3.getVisualOrderText())).build();
                        string3 = component4.getString() + "\n" + component3.getString();
                    } else {
                        list = ImmutableList.of(component2.getVisualOrderText(), component3.getVisualOrderText());
                        string3 = component3.getString();
                    }
                    map.computeIfAbsent(key.getCategory(), category -> Maps.newHashMap()).put(key, entryFactory.create(component, (List<FormattedCharSequence>)((Object)list), (String)string3, value));
                }
            });
            map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry2 -> {
                this.addEntry(new CategoryRuleEntry(Component.translatable(((GameRules.Category)((Object)((Object)entry2.getKey()))).getDescriptionId()).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW)));
                ((Map)entry2.getValue()).entrySet().stream().sorted(Map.Entry.comparingByKey(Comparator.comparing(GameRules.Key::getId))).forEach(entry -> this.addEntry((RuleEntry)entry.getValue()));
            });
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, float f) {
            super.render(poseStack, i, j, f);
            RuleEntry ruleEntry = (RuleEntry)this.getHovered();
            if (ruleEntry != null && ruleEntry.tooltip != null) {
                EditGameRulesScreen.this.setTooltipForNextRenderPass(ruleEntry.tooltip);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public class IntegerRuleEntry
    extends GameRuleEntry {
        private final EditBox input;

        public IntegerRuleEntry(Component component, List<FormattedCharSequence> list, String string2, GameRules.IntegerValue integerValue) {
            super(list, component);
            this.input = new EditBox(((EditGameRulesScreen)EditGameRulesScreen.this).minecraft.font, 10, 5, 42, 20, component.copy().append("\n").append(string2).append("\n"));
            this.input.setValue(Integer.toString(integerValue.get()));
            this.input.setResponder(string -> {
                if (integerValue.tryDeserialize((String)string)) {
                    this.input.setTextColor(0xE0E0E0);
                    EditGameRulesScreen.this.clearInvalid(this);
                } else {
                    this.input.setTextColor(0xFF0000);
                    EditGameRulesScreen.this.markInvalid(this);
                }
            });
            this.children.add(this.input);
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            this.renderLabel(poseStack, j, k);
            this.input.setX(k + l - 44);
            this.input.setY(j);
            this.input.render(poseStack, n, o, f);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public class BooleanRuleEntry
    extends GameRuleEntry {
        private final CycleButton<Boolean> checkbox;

        public BooleanRuleEntry(Component component, List<FormattedCharSequence> list, String string, GameRules.BooleanValue booleanValue) {
            super(list, component);
            this.checkbox = CycleButton.onOffBuilder(booleanValue.get()).displayOnlyValue().withCustomNarration(cycleButton -> cycleButton.createDefaultNarrationMessage().append("\n").append(string)).create(10, 5, 44, 20, component, (cycleButton, boolean_) -> booleanValue.set((boolean)boolean_, null));
            this.children.add(this.checkbox);
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            this.renderLabel(poseStack, j, k);
            this.checkbox.setX(k + l - 45);
            this.checkbox.setY(j);
            this.checkbox.render(poseStack, n, o, f);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public abstract class GameRuleEntry
    extends RuleEntry {
        private final List<FormattedCharSequence> label;
        protected final List<AbstractWidget> children;

        public GameRuleEntry(List<FormattedCharSequence> list, Component component) {
            super(list);
            this.children = Lists.newArrayList();
            this.label = ((EditGameRulesScreen)EditGameRulesScreen.this).minecraft.font.split(component, 175);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.children;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return this.children;
        }

        protected void renderLabel(PoseStack poseStack, int i, int j) {
            if (this.label.size() == 1) {
                ((EditGameRulesScreen)EditGameRulesScreen.this).minecraft.font.draw(poseStack, this.label.get(0), (float)j, (float)(i + 5), 0xFFFFFF);
            } else if (this.label.size() >= 2) {
                ((EditGameRulesScreen)EditGameRulesScreen.this).minecraft.font.draw(poseStack, this.label.get(0), (float)j, (float)i, 0xFFFFFF);
                ((EditGameRulesScreen)EditGameRulesScreen.this).minecraft.font.draw(poseStack, this.label.get(1), (float)j, (float)(i + 10), 0xFFFFFF);
            }
        }
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    static interface EntryFactory<T extends GameRules.Value<T>> {
        public RuleEntry create(Component var1, List<FormattedCharSequence> var2, String var3, T var4);
    }

    @Environment(value=EnvType.CLIENT)
    public class CategoryRuleEntry
    extends RuleEntry {
        final Component label;

        public CategoryRuleEntry(Component component) {
            super(null);
            this.label = component;
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            GuiComponent.drawCenteredString(poseStack, ((EditGameRulesScreen)EditGameRulesScreen.this).minecraft.font, this.label, k + l / 2, j + 5, 0xFFFFFF);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(new NarratableEntry(){

                @Override
                public NarratableEntry.NarrationPriority narrationPriority() {
                    return NarratableEntry.NarrationPriority.HOVERED;
                }

                @Override
                public void updateNarration(NarrationElementOutput narrationElementOutput) {
                    narrationElementOutput.add(NarratedElementType.TITLE, CategoryRuleEntry.this.label);
                }
            });
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static abstract class RuleEntry
    extends ContainerObjectSelectionList.Entry<RuleEntry> {
        @Nullable
        final List<FormattedCharSequence> tooltip;

        public RuleEntry(@Nullable List<FormattedCharSequence> list) {
            this.tooltip = list;
        }
    }
}

