/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.achievement;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.achievement.StatsUpdateListener;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class StatsScreen
extends Screen
implements StatsUpdateListener {
    protected final Screen lastScreen;
    private GeneralStatisticsList statsList;
    private ItemStatisticsList itemStatsList;
    private MobsStatisticsList mobsStatsList;
    private final StatsCounter stats;
    @Nullable
    private ObjectSelectionList<?> activeList;
    private boolean isLoading = true;

    public StatsScreen(Screen screen, StatsCounter statsCounter) {
        super(new TranslatableComponent("gui.stats", new Object[0]));
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
        this.addButton(new Button(this.width / 2 - 120, this.height - 52, 80, 20, I18n.get("stat.generalButton", new Object[0]), button -> this.setActiveList(this.statsList)));
        Button button2 = this.addButton(new Button(this.width / 2 - 40, this.height - 52, 80, 20, I18n.get("stat.itemsButton", new Object[0]), button -> this.setActiveList(this.itemStatsList)));
        Button button22 = this.addButton(new Button(this.width / 2 + 40, this.height - 52, 80, 20, I18n.get("stat.mobsButton", new Object[0]), button -> this.setActiveList(this.mobsStatsList)));
        this.addButton(new Button(this.width / 2 - 100, this.height - 28, 200, 20, I18n.get("gui.done", new Object[0]), button -> this.minecraft.setScreen(this.lastScreen)));
        if (this.itemStatsList.children().isEmpty()) {
            button2.active = false;
        }
        if (this.mobsStatsList.children().isEmpty()) {
            button22.active = false;
        }
    }

    @Override
    public void render(int i, int j, float f) {
        if (this.isLoading) {
            this.renderBackground();
            this.drawCenteredString(this.font, I18n.get("multiplayer.downloadingStats", new Object[0]), this.width / 2, this.height / 2, 0xFFFFFF);
            this.drawCenteredString(this.font, LOADING_SYMBOLS[(int)(Util.getMillis() / 150L % (long)LOADING_SYMBOLS.length)], this.width / 2, this.height / 2 + this.font.lineHeight * 2, 0xFFFFFF);
        } else {
            this.getActiveList().render(i, j, f);
            this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 20, 0xFFFFFF);
            super.render(i, j, f);
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
        this.children.remove(this.statsList);
        this.children.remove(this.itemStatsList);
        this.children.remove(this.mobsStatsList);
        if (objectSelectionList != null) {
            this.children.add(0, objectSelectionList);
            this.activeList = objectSelectionList;
        }
    }

    private int getColumnX(int i) {
        return 115 + 40 * i;
    }

    private void blitSlot(int i, int j, Item item) {
        this.blitSlotIcon(i + 1, j + 1, 0, 0);
        RenderSystem.enableRescaleNormal();
        Lighting.turnOnGui();
        this.itemRenderer.renderGuiItem(item.getDefaultInstance(), i + 2, j + 2);
        Lighting.turnOff();
        RenderSystem.disableRescaleNormal();
    }

    private void blitSlotIcon(int i, int j, int k, int l) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.minecraft.getTextureManager().bind(STATS_ICON_LOCATION);
        StatsScreen.blit(i, j, this.getBlitOffset(), k, l, 18, 18, 128, 128);
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
        protected void renderBackground() {
            StatsScreen.this.renderBackground();
        }

        @Environment(value=EnvType.CLIENT)
        class MobRow
        extends ObjectSelectionList.Entry<MobRow> {
            private final EntityType<?> type;

            public MobRow(EntityType<?> entityType) {
                this.type = entityType;
            }

            @Override
            public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
                String string = I18n.get(Util.makeDescriptionId("entity", EntityType.getKey(this.type)), new Object[0]);
                int p = StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED.get(this.type));
                int q = StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED_BY.get(this.type));
                MobsStatisticsList.this.drawString(StatsScreen.this.font, string, k + 2, j + 1, 0xFFFFFF);
                MobsStatisticsList.this.drawString(StatsScreen.this.font, this.killsMessage(string, p), k + 2 + 10, j + 1 + ((StatsScreen)StatsScreen.this).font.lineHeight, p == 0 ? 0x606060 : 0x909090);
                MobsStatisticsList.this.drawString(StatsScreen.this.font, this.killedByMessage(string, q), k + 2 + 10, j + 1 + ((StatsScreen)StatsScreen.this).font.lineHeight * 2, q == 0 ? 0x606060 : 0x909090);
            }

            private String killsMessage(String string, int i) {
                String string2 = Stats.ENTITY_KILLED.getTranslationKey();
                if (i == 0) {
                    return I18n.get(string2 + ".none", string);
                }
                return I18n.get(string2, i, string);
            }

            private String killedByMessage(String string, int i) {
                String string2 = Stats.ENTITY_KILLED_BY.getTranslationKey();
                if (i == 0) {
                    return I18n.get(string2 + ".none", string);
                }
                return I18n.get(string2, string, i);
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
        protected final List<Item> statItemList;
        protected final Comparator<Item> itemStatSorter;
        @Nullable
        protected StatType<?> sortColumn;
        protected int sortOrder;

        public ItemStatisticsList(Minecraft minecraft) {
            boolean bl;
            super(minecraft, StatsScreen.this.width, StatsScreen.this.height, 32, StatsScreen.this.height - 64, 20);
            this.iconOffsets = new int[]{3, 4, 1, 2, 5, 6};
            this.headerPressed = -1;
            this.itemStatSorter = new ItemComparator();
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
                for (StatType<ItemLike> statType : this.blockColumns) {
                    if (!statType.contains(block) || StatsScreen.this.stats.getValue(statType.get(block)) <= 0) continue;
                    bl = true;
                }
                if (!bl) continue;
                set.add(block.asItem());
            }
            set.remove(Items.AIR);
            this.statItemList = Lists.newArrayList(set);
            for (int i = 0; i < this.statItemList.size(); ++i) {
                this.addEntry(new ItemRow());
            }
        }

        @Override
        protected void renderHeader(int i, int j, Tesselator tesselator) {
            int l;
            int k;
            if (!this.minecraft.mouseHandler.isLeftPressed()) {
                this.headerPressed = -1;
            }
            for (k = 0; k < this.iconOffsets.length; ++k) {
                StatsScreen.this.blitSlotIcon(i + StatsScreen.this.getColumnX(k) - 18, j + 1, 0, this.headerPressed == k ? 0 : 18);
            }
            if (this.sortColumn != null) {
                k = StatsScreen.this.getColumnX(this.getColumnIndex(this.sortColumn)) - 36;
                l = this.sortOrder == 1 ? 2 : 1;
                StatsScreen.this.blitSlotIcon(i + k, j + 1, 18 * l, 0);
            }
            for (k = 0; k < this.iconOffsets.length; ++k) {
                l = this.headerPressed == k ? 1 : 0;
                StatsScreen.this.blitSlotIcon(i + StatsScreen.this.getColumnX(k) - 18 + l, j + 1 + l, 18 * this.iconOffsets[k], 18);
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
        protected void renderBackground() {
            StatsScreen.this.renderBackground();
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
        protected void renderDecorations(int i, int j) {
            if (j < this.y0 || j > this.y1) {
                return;
            }
            ItemRow itemRow = (ItemRow)this.getEntryAtPosition(i, j);
            int k = (this.width - this.getRowWidth()) / 2;
            if (itemRow != null) {
                if (i < k + 40 || i > k + 40 + 20) {
                    return;
                }
                Item item = this.statItemList.get(this.children().indexOf(itemRow));
                this.renderMousehoverTooltip(this.getString(item), i, j);
            } else {
                TranslatableComponent component = null;
                int l = i - k;
                for (int m = 0; m < this.iconOffsets.length; ++m) {
                    int n = StatsScreen.this.getColumnX(m);
                    if (l < n - 18 || l > n) continue;
                    component = new TranslatableComponent(this.getColumn(m).getTranslationKey(), new Object[0]);
                    break;
                }
                this.renderMousehoverTooltip(component, i, j);
            }
        }

        protected void renderMousehoverTooltip(@Nullable Component component, int i, int j) {
            if (component == null) {
                return;
            }
            String string = component.getColoredString();
            int k = i + 12;
            int l = j - 12;
            int m = StatsScreen.this.font.width(string);
            this.fillGradient(k - 3, l - 3, k + m + 3, l + 8 + 3, -1073741824, -1073741824);
            StatsScreen.this.font.drawShadow(string, k, l, -1);
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
            this.statItemList.sort(this.itemStatSorter);
        }

        @Environment(value=EnvType.CLIENT)
        class ItemRow
        extends ObjectSelectionList.Entry<ItemRow> {
            private ItemRow() {
            }

            @Override
            public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
                int p;
                Item item = ((StatsScreen)StatsScreen.this).itemStatsList.statItemList.get(i);
                StatsScreen.this.blitSlot(k + 40, j, item);
                for (p = 0; p < ((StatsScreen)StatsScreen.this).itemStatsList.blockColumns.size(); ++p) {
                    Stat<Block> stat = item instanceof BlockItem ? ((StatsScreen)StatsScreen.this).itemStatsList.blockColumns.get(p).get(((BlockItem)item).getBlock()) : null;
                    this.renderStat(stat, k + StatsScreen.this.getColumnX(p), j, i % 2 == 0);
                }
                for (p = 0; p < ((StatsScreen)StatsScreen.this).itemStatsList.itemColumns.size(); ++p) {
                    this.renderStat(((StatsScreen)StatsScreen.this).itemStatsList.itemColumns.get(p).get(item), k + StatsScreen.this.getColumnX(p + ((StatsScreen)StatsScreen.this).itemStatsList.blockColumns.size()), j, i % 2 == 0);
                }
            }

            protected void renderStat(@Nullable Stat<?> stat, int i, int j, boolean bl) {
                String string = stat == null ? "-" : stat.format(StatsScreen.this.stats.getValue(stat));
                ItemStatisticsList.this.drawString(StatsScreen.this.font, string, i - StatsScreen.this.font.width(string), j + 5, bl ? 0xFFFFFF : 0x909090);
            }
        }

        @Environment(value=EnvType.CLIENT)
        class ItemComparator
        implements Comparator<Item> {
            private ItemComparator() {
            }

            @Override
            public int compare(Item item, Item item2) {
                int j;
                int i;
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
                return this.compare((Item)object, (Item)object2);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    class GeneralStatisticsList
    extends ObjectSelectionList<Entry> {
        public GeneralStatisticsList(Minecraft minecraft) {
            super(minecraft, StatsScreen.this.width, StatsScreen.this.height, 32, StatsScreen.this.height - 64, 10);
            for (Stat<ResourceLocation> stat : Stats.CUSTOM) {
                this.addEntry(new Entry(stat));
            }
        }

        @Override
        protected void renderBackground() {
            StatsScreen.this.renderBackground();
        }

        @Environment(value=EnvType.CLIENT)
        class Entry
        extends ObjectSelectionList.Entry<Entry> {
            private final Stat<ResourceLocation> stat;

            private Entry(Stat<ResourceLocation> stat) {
                this.stat = stat;
            }

            @Override
            public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
                Component component = new TranslatableComponent("stat." + this.stat.getValue().toString().replace(':', '.'), new Object[0]).withStyle(ChatFormatting.GRAY);
                GeneralStatisticsList.this.drawString(StatsScreen.this.font, component.getString(), k + 2, j + 1, i % 2 == 0 ? 0xFFFFFF : 0x909090);
                String string = this.stat.format(StatsScreen.this.stats.getValue(this.stat));
                GeneralStatisticsList.this.drawString(StatsScreen.this.font, string, k + 2 + 213 - StatsScreen.this.font.width(string), j + 1, i % 2 == 0 ? 0xFFFFFF : 0x909090);
            }
        }
    }
}

