/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.achievement;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.achievement.StatsUpdateListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class StatsScreen
extends Screen
implements StatsUpdateListener {
    private static final Component PENDING_TEXT = Component.translatable("multiplayer.downloadingStats");
    protected final Screen lastScreen;
    private GeneralStatisticsList statsList;
    ItemStatisticsList itemStatsList;
    private MobsStatisticsList mobsStatsList;
    final StatsCounter stats;
    @Nullable
    private ObjectSelectionList<?> activeList;
    private boolean isLoading = true;
    private static final int SLOT_TEX_SIZE = 128;
    private static final int SLOT_BG_SIZE = 18;
    private static final int SLOT_STAT_HEIGHT = 20;
    private static final int SLOT_BG_X = 1;
    private static final int SLOT_BG_Y = 1;
    private static final int SLOT_FG_X = 2;
    private static final int SLOT_FG_Y = 2;
    private static final int SLOT_LEFT_INSERT = 40;
    private static final int SLOT_TEXT_OFFSET = 5;
    private static final int SORT_NONE = 0;
    private static final int SORT_DOWN = -1;
    private static final int SORT_UP = 1;

    public StatsScreen(Screen screen, StatsCounter statsCounter) {
        super(Component.translatable("gui.stats"));
        this.lastScreen = screen;
        this.stats = statsCounter;
    }

    @Override
    protected void init() {
        this.isLoading = true;
        this.minecraft.getConnection().send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS));
    }

    public void initLists() {
        this.statsList = new GeneralStatisticsList(this.minecraft);
        this.itemStatsList = new ItemStatisticsList(this.minecraft);
        this.mobsStatsList = new MobsStatisticsList(this.minecraft);
    }

    public void initButtons() {
        this.addRenderableWidget(new Button(this.width / 2 - 120, this.height - 52, 80, 20, Component.translatable("stat.generalButton"), button -> this.setActiveList(this.statsList)));
        Button button2 = this.addRenderableWidget(new Button(this.width / 2 - 40, this.height - 52, 80, 20, Component.translatable("stat.itemsButton"), button -> this.setActiveList(this.itemStatsList)));
        Button button22 = this.addRenderableWidget(new Button(this.width / 2 + 40, this.height - 52, 80, 20, Component.translatable("stat.mobsButton"), button -> this.setActiveList(this.mobsStatsList)));
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 28, 200, 20, CommonComponents.GUI_DONE, button -> this.minecraft.setScreen(this.lastScreen)));
        if (this.itemStatsList.children().isEmpty()) {
            button2.active = false;
        }
        if (this.mobsStatsList.children().isEmpty()) {
            button22.active = false;
        }
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        if (this.isLoading) {
            this.renderBackground(poseStack);
            StatsScreen.drawCenteredString(poseStack, this.font, PENDING_TEXT, this.width / 2, this.height / 2, 0xFFFFFF);
            StatsScreen.drawCenteredString(poseStack, this.font, LOADING_SYMBOLS[(int)(Util.getMillis() / 150L % (long)LOADING_SYMBOLS.length)], this.width / 2, this.height / 2 + this.font.lineHeight * 2, 0xFFFFFF);
        } else {
            this.getActiveList().render(poseStack, i, j, f);
            StatsScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, 0xFFFFFF);
            super.render(poseStack, i, j, f);
        }
    }

    @Override
    public void onStatsUpdated() {
        if (this.isLoading) {
            this.initLists();
            this.initButtons();
            this.setActiveList(this.statsList);
            this.isLoading = false;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return !this.isLoading;
    }

    @Nullable
    public ObjectSelectionList<?> getActiveList() {
        return this.activeList;
    }

    public void setActiveList(@Nullable ObjectSelectionList<?> objectSelectionList) {
        if (this.activeList != null) {
            this.removeWidget(this.activeList);
        }
        if (objectSelectionList != null) {
            this.addWidget(objectSelectionList);
            this.activeList = objectSelectionList;
        }
    }

    static String getTranslationKey(Stat<ResourceLocation> stat) {
        return "stat." + stat.getValue().toString().replace(':', '.');
    }

    int getColumnX(int i) {
        return 115 + 40 * i;
    }

    void blitSlot(PoseStack poseStack, int i, int j, Item item) {
        this.blitSlotIcon(poseStack, i + 1, j + 1, 0, 0);
        this.itemRenderer.renderGuiItem(item.getDefaultInstance(), i + 2, j + 2);
    }

    void blitSlotIcon(PoseStack poseStack, int i, int j, int k, int l) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, STATS_ICON_LOCATION);
        StatsScreen.blit(poseStack, i, j, this.getBlitOffset(), k, l, 18, 18, 128, 128);
    }

    @Environment(value=EnvType.CLIENT)
    class GeneralStatisticsList
    extends ObjectSelectionList<Entry> {
        public GeneralStatisticsList(Minecraft minecraft) {
            super(minecraft, StatsScreen.this.width, StatsScreen.this.height, 32, StatsScreen.this.height - 64, 10);
            ObjectArrayList<Stat<ResourceLocation>> objectArrayList = new ObjectArrayList<Stat<ResourceLocation>>(Stats.CUSTOM.iterator());
            objectArrayList.sort((Comparator<Stat<ResourceLocation>>)Comparator.comparing(stat -> I18n.get(StatsScreen.getTranslationKey(stat), new Object[0])));
            for (Stat stat2 : objectArrayList) {
                this.addEntry(new Entry(stat2));
            }
        }

        @Override
        protected void renderBackground(PoseStack poseStack) {
            StatsScreen.this.renderBackground(poseStack);
        }

        @Environment(value=EnvType.CLIENT)
        class Entry
        extends ObjectSelectionList.Entry<Entry> {
            private final Stat<ResourceLocation> stat;
            private final Component statDisplay;

            Entry(Stat<ResourceLocation> stat) {
                this.stat = stat;
                this.statDisplay = Component.translatable(StatsScreen.getTranslationKey(stat));
            }

            private String getValueText() {
                return this.stat.format(StatsScreen.this.stats.getValue(this.stat));
            }

            @Override
            public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
                GuiComponent.drawString(poseStack, StatsScreen.this.font, this.statDisplay, k + 2, j + 1, i % 2 == 0 ? 0xFFFFFF : 0x909090);
                String string = this.getValueText();
                GuiComponent.drawString(poseStack, StatsScreen.this.font, string, k + 2 + 213 - StatsScreen.this.font.width(string), j + 1, i % 2 == 0 ? 0xFFFFFF : 0x909090);
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", Component.empty().append(this.statDisplay).append(" ").append(this.getValueText()));
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    class ItemStatisticsList
    extends ObjectSelectionList<ItemRow> {
        protected final List<StatType<Block>> blockColumns;
        protected final List<StatType<Item>> itemColumns;
        private final int[] iconOffsets;
        protected int headerPressed;
        protected final Comparator<ItemRow> itemStatSorter;
        @Nullable
        protected StatType<?> sortColumn;
        protected int sortOrder;

        public ItemStatisticsList(Minecraft minecraft) {
            boolean bl;
            super(minecraft, StatsScreen.this.width, StatsScreen.this.height, 32, StatsScreen.this.height - 64, 20);
            this.iconOffsets = new int[]{3, 4, 1, 2, 5, 6};
            this.headerPressed = -1;
            this.itemStatSorter = new ItemRowComparator();
            this.blockColumns = Lists.newArrayList();
            this.blockColumns.add(Stats.BLOCK_MINED);
            this.itemColumns = Lists.newArrayList(Stats.ITEM_BROKEN, Stats.ITEM_CRAFTED, Stats.ITEM_USED, Stats.ITEM_PICKED_UP, Stats.ITEM_DROPPED);
            this.setRenderHeader(true, 20);
            Set<Item> set = Sets.newIdentityHashSet();
            for (Item item : Registry.ITEM) {
                bl = false;
                for (StatType<Item> statType : this.itemColumns) {
                    if (!statType.contains(item) || StatsScreen.this.stats.getValue(statType.get(item)) <= 0) continue;
                    bl = true;
                }
                if (!bl) continue;
                set.add(item);
            }
            for (Block block : Registry.BLOCK) {
                bl = false;
                for (StatType<FeatureElement> statType : this.blockColumns) {
                    if (!statType.contains(block) || StatsScreen.this.stats.getValue(statType.get(block)) <= 0) continue;
                    bl = true;
                }
                if (!bl) continue;
                set.add(block.asItem());
            }
            set.remove(Items.AIR);
            for (Item item : set) {
                this.addEntry(new ItemRow(item));
            }
        }

        @Override
        protected void renderHeader(PoseStack poseStack, int i, int j, Tesselator tesselator) {
            int l;
            int k;
            if (!this.minecraft.mouseHandler.isLeftPressed()) {
                this.headerPressed = -1;
            }
            for (k = 0; k < this.iconOffsets.length; ++k) {
                StatsScreen.this.blitSlotIcon(poseStack, i + StatsScreen.this.getColumnX(k) - 18, j + 1, 0, this.headerPressed == k ? 0 : 18);
            }
            if (this.sortColumn != null) {
                k = StatsScreen.this.getColumnX(this.getColumnIndex(this.sortColumn)) - 36;
                l = this.sortOrder == 1 ? 2 : 1;
                StatsScreen.this.blitSlotIcon(poseStack, i + k, j + 1, 18 * l, 0);
            }
            for (k = 0; k < this.iconOffsets.length; ++k) {
                l = this.headerPressed == k ? 1 : 0;
                StatsScreen.this.blitSlotIcon(poseStack, i + StatsScreen.this.getColumnX(k) - 18 + l, j + 1 + l, 18 * this.iconOffsets[k], 18);
            }
        }

        @Override
        public int getRowWidth() {
            return 375;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width / 2 + 140;
        }

        @Override
        protected void renderBackground(PoseStack poseStack) {
            StatsScreen.this.renderBackground(poseStack);
        }

        @Override
        protected void clickedHeader(int i, int j) {
            this.headerPressed = -1;
            for (int k = 0; k < this.iconOffsets.length; ++k) {
                int l = i - StatsScreen.this.getColumnX(k);
                if (l < -36 || l > 0) continue;
                this.headerPressed = k;
                break;
            }
            if (this.headerPressed >= 0) {
                this.sortByColumn(this.getColumn(this.headerPressed));
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            }
        }

        private StatType<?> getColumn(int i) {
            return i < this.blockColumns.size() ? this.blockColumns.get(i) : this.itemColumns.get(i - this.blockColumns.size());
        }

        private int getColumnIndex(StatType<?> statType) {
            int i = this.blockColumns.indexOf(statType);
            if (i >= 0) {
                return i;
            }
            int j = this.itemColumns.indexOf(statType);
            if (j >= 0) {
                return j + this.blockColumns.size();
            }
            return -1;
        }

        @Override
        protected void renderDecorations(PoseStack poseStack, int i, int j) {
            if (j < this.y0 || j > this.y1) {
                return;
            }
            ItemRow itemRow = (ItemRow)this.getHovered();
            int k = (this.width - this.getRowWidth()) / 2;
            if (itemRow != null) {
                if (i < k + 40 || i > k + 40 + 20) {
                    return;
                }
                Item item = itemRow.getItem();
                this.renderMousehoverTooltip(poseStack, this.getString(item), i, j);
            } else {
                Component component = null;
                int l = i - k;
                for (int m = 0; m < this.iconOffsets.length; ++m) {
                    int n = StatsScreen.this.getColumnX(m);
                    if (l < n - 18 || l > n) continue;
                    component = this.getColumn(m).getDisplayName();
                    break;
                }
                this.renderMousehoverTooltip(poseStack, component, i, j);
            }
        }

        protected void renderMousehoverTooltip(PoseStack poseStack, @Nullable Component component, int i, int j) {
            if (component == null) {
                return;
            }
            int k = i + 12;
            int l = j - 12;
            int m = StatsScreen.this.font.width(component);
            this.fillGradient(poseStack, k - 3, l - 3, k + m + 3, l + 8 + 3, -1073741824, -1073741824);
            poseStack.pushPose();
            poseStack.translate(0.0, 0.0, 400.0);
            StatsScreen.this.font.drawShadow(poseStack, component, (float)k, (float)l, -1);
            poseStack.popPose();
        }

        protected Component getString(Item item) {
            return item.getDescription();
        }

        protected void sortByColumn(StatType<?> statType) {
            if (statType != this.sortColumn) {
                this.sortColumn = statType;
                this.sortOrder = -1;
            } else if (this.sortOrder == -1) {
                this.sortOrder = 1;
            } else {
                this.sortColumn = null;
                this.sortOrder = 0;
            }
            this.children().sort(this.itemStatSorter);
        }

        @Environment(value=EnvType.CLIENT)
        class ItemRowComparator
        implements Comparator<ItemRow> {
            ItemRowComparator() {
            }

            @Override
            public int compare(ItemRow itemRow, ItemRow itemRow2) {
                int j;
                int i;
                Item item = itemRow.getItem();
                Item item2 = itemRow2.getItem();
                if (ItemStatisticsList.this.sortColumn == null) {
                    i = 0;
                    j = 0;
                } else if (ItemStatisticsList.this.blockColumns.contains(ItemStatisticsList.this.sortColumn)) {
                    StatType<?> statType = ItemStatisticsList.this.sortColumn;
                    i = item instanceof BlockItem ? StatsScreen.this.stats.getValue(statType, ((BlockItem)item).getBlock()) : -1;
                    j = item2 instanceof BlockItem ? StatsScreen.this.stats.getValue(statType, ((BlockItem)item2).getBlock()) : -1;
                } else {
                    StatType<?> statType = ItemStatisticsList.this.sortColumn;
                    i = StatsScreen.this.stats.getValue(statType, item);
                    j = StatsScreen.this.stats.getValue(statType, item2);
                }
                if (i == j) {
                    return ItemStatisticsList.this.sortOrder * Integer.compare(Item.getId(item), Item.getId(item2));
                }
                return ItemStatisticsList.this.sortOrder * Integer.compare(i, j);
            }

            @Override
            public /* synthetic */ int compare(Object object, Object object2) {
                return this.compare((ItemRow)object, (ItemRow)object2);
            }
        }

        @Environment(value=EnvType.CLIENT)
        class ItemRow
        extends ObjectSelectionList.Entry<ItemRow> {
            private final Item item;

            ItemRow(Item item) {
                this.item = item;
            }

            public Item getItem() {
                return this.item;
            }

            @Override
            public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
                int p;
                StatsScreen.this.blitSlot(poseStack, k + 40, j, this.item);
                for (p = 0; p < StatsScreen.this.itemStatsList.blockColumns.size(); ++p) {
                    Stat<Block> stat = this.item instanceof BlockItem ? StatsScreen.this.itemStatsList.blockColumns.get(p).get(((BlockItem)this.item).getBlock()) : null;
                    this.renderStat(poseStack, stat, k + StatsScreen.this.getColumnX(p), j, i % 2 == 0);
                }
                for (p = 0; p < StatsScreen.this.itemStatsList.itemColumns.size(); ++p) {
                    this.renderStat(poseStack, StatsScreen.this.itemStatsList.itemColumns.get(p).get(this.item), k + StatsScreen.this.getColumnX(p + StatsScreen.this.itemStatsList.blockColumns.size()), j, i % 2 == 0);
                }
            }

            protected void renderStat(PoseStack poseStack, @Nullable Stat<?> stat, int i, int j, boolean bl) {
                String string = stat == null ? "-" : stat.format(StatsScreen.this.stats.getValue(stat));
                GuiComponent.drawString(poseStack, StatsScreen.this.font, string, i - StatsScreen.this.font.width(string), j + 5, bl ? 0xFFFFFF : 0x909090);
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", this.item.getDescription());
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    class MobsStatisticsList
    extends ObjectSelectionList<MobRow> {
        public MobsStatisticsList(Minecraft minecraft) {
            super(minecraft, StatsScreen.this.width, StatsScreen.this.height, 32, StatsScreen.this.height - 64, ((StatsScreen)StatsScreen.this).font.lineHeight * 4);
            for (EntityType entityType : Registry.ENTITY_TYPE) {
                if (StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED.get(entityType)) <= 0 && StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED_BY.get(entityType)) <= 0) continue;
                this.addEntry(new MobRow(entityType));
            }
        }

        @Override
        protected void renderBackground(PoseStack poseStack) {
            StatsScreen.this.renderBackground(poseStack);
        }

        @Environment(value=EnvType.CLIENT)
        class MobRow
        extends ObjectSelectionList.Entry<MobRow> {
            private final Component mobName;
            private final Component kills;
            private final boolean hasKills;
            private final Component killedBy;
            private final boolean wasKilledBy;

            public MobRow(EntityType<?> entityType) {
                this.mobName = entityType.getDescription();
                int i = StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED.get(entityType));
                if (i == 0) {
                    this.kills = Component.translatable("stat_type.minecraft.killed.none", this.mobName);
                    this.hasKills = false;
                } else {
                    this.kills = Component.translatable("stat_type.minecraft.killed", i, this.mobName);
                    this.hasKills = true;
                }
                int j = StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED_BY.get(entityType));
                if (j == 0) {
                    this.killedBy = Component.translatable("stat_type.minecraft.killed_by.none", this.mobName);
                    this.wasKilledBy = false;
                } else {
                    this.killedBy = Component.translatable("stat_type.minecraft.killed_by", this.mobName, j);
                    this.wasKilledBy = true;
                }
            }

            @Override
            public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
                GuiComponent.drawString(poseStack, StatsScreen.this.font, this.mobName, k + 2, j + 1, 0xFFFFFF);
                GuiComponent.drawString(poseStack, StatsScreen.this.font, this.kills, k + 2 + 10, j + 1 + ((StatsScreen)StatsScreen.this).font.lineHeight, this.hasKills ? 0x909090 : 0x606060);
                GuiComponent.drawString(poseStack, StatsScreen.this.font, this.killedBy, k + 2 + 10, j + 1 + ((StatsScreen)StatsScreen.this).font.lineHeight * 2, this.wasKilledBy ? 0x909090 : 0x606060);
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", CommonComponents.joinForNarration(this.kills, this.killedBy));
            }
        }
    }
}

