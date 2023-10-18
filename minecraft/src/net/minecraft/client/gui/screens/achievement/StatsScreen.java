package net.minecraft.client.gui.screens.achievement;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

@Environment(EnvType.CLIENT)
public class StatsScreen extends Screen implements StatsUpdateListener {
	static final ResourceLocation SLOT_SPRITE = new ResourceLocation("container/slot");
	static final ResourceLocation HEADER_SPRITE = new ResourceLocation("statistics/header");
	static final ResourceLocation SORT_UP_SPRITE = new ResourceLocation("statistics/sort_up");
	static final ResourceLocation SORT_DOWN_SPRITE = new ResourceLocation("statistics/sort_down");
	private static final Component PENDING_TEXT = Component.translatable("multiplayer.downloadingStats");
	static final Component NO_VALUE_DISPLAY = Component.translatable("stats.none");
	protected final Screen lastScreen;
	private StatsScreen.GeneralStatisticsList statsList;
	StatsScreen.ItemStatisticsList itemStatsList;
	private StatsScreen.MobsStatisticsList mobsStatsList;
	final StatsCounter stats;
	@Nullable
	private ObjectSelectionList<?> activeList;
	private boolean isLoading = true;
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
		this.statsList = new StatsScreen.GeneralStatisticsList(this.minecraft);
		this.itemStatsList = new StatsScreen.ItemStatisticsList(this.minecraft);
		this.mobsStatsList = new StatsScreen.MobsStatisticsList(this.minecraft);
	}

	public void initButtons() {
		this.addRenderableWidget(
			Button.builder(Component.translatable("stat.generalButton"), buttonx -> this.setActiveList(this.statsList))
				.bounds(this.width / 2 - 120, this.height - 52, 80, 20)
				.build()
		);
		Button button = this.addRenderableWidget(
			Button.builder(Component.translatable("stat.itemsButton"), buttonx -> this.setActiveList(this.itemStatsList))
				.bounds(this.width / 2 - 40, this.height - 52, 80, 20)
				.build()
		);
		Button button2 = this.addRenderableWidget(
			Button.builder(Component.translatable("stat.mobsButton"), buttonx -> this.setActiveList(this.mobsStatsList))
				.bounds(this.width / 2 + 40, this.height - 52, 80, 20)
				.build()
		);
		this.addRenderableWidget(
			Button.builder(CommonComponents.GUI_DONE, buttonx -> this.minecraft.setScreen(this.lastScreen))
				.bounds(this.width / 2 - 100, this.height - 28, 200, 20)
				.build()
		);
		if (this.itemStatsList.children().isEmpty()) {
			button.active = false;
		}

		if (this.mobsStatsList.children().isEmpty()) {
			button2.active = false;
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		if (this.isLoading) {
			this.renderBackground(guiGraphics, i, j, f);
			guiGraphics.drawCenteredString(this.font, PENDING_TEXT, this.width / 2, this.height / 2, 16777215);
			guiGraphics.drawCenteredString(
				this.font, LOADING_SYMBOLS[(int)(Util.getMillis() / 150L % (long)LOADING_SYMBOLS.length)], this.width / 2, this.height / 2 + 9 * 2, 16777215
			);
		} else {
			super.render(guiGraphics, i, j, f);
			this.getActiveList().render(guiGraphics, i, j, f);
			guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215);
		}
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
		this.renderDirtBackground(guiGraphics);
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

	void blitSlot(GuiGraphics guiGraphics, int i, int j, Item item) {
		this.blitSlotIcon(guiGraphics, i + 1, j + 1, SLOT_SPRITE);
		guiGraphics.renderFakeItem(item.getDefaultInstance(), i + 2, j + 2);
	}

	void blitSlotIcon(GuiGraphics guiGraphics, int i, int j, ResourceLocation resourceLocation) {
		guiGraphics.blitSprite(resourceLocation, i, j, 0, 18, 18);
	}

	@Environment(EnvType.CLIENT)
	class GeneralStatisticsList extends ObjectSelectionList<StatsScreen.GeneralStatisticsList.Entry> {
		public GeneralStatisticsList(Minecraft minecraft) {
			super(minecraft, StatsScreen.this.width, StatsScreen.this.height, 32, StatsScreen.this.height - 64, 10);
			ObjectArrayList<Stat<ResourceLocation>> objectArrayList = new ObjectArrayList<>(Stats.CUSTOM.iterator());
			objectArrayList.sort(Comparator.comparing(statx -> I18n.get(StatsScreen.getTranslationKey(statx))));

			for (Stat<ResourceLocation> stat : objectArrayList) {
				this.addEntry(new StatsScreen.GeneralStatisticsList.Entry(stat));
			}
		}

		@Environment(EnvType.CLIENT)
		class Entry extends ObjectSelectionList.Entry<StatsScreen.GeneralStatisticsList.Entry> {
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
			public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
				guiGraphics.drawString(StatsScreen.this.font, this.statDisplay, k + 2, j + 1, i % 2 == 0 ? 16777215 : 9474192);
				String string = this.getValueText();
				guiGraphics.drawString(StatsScreen.this.font, string, k + 2 + 213 - StatsScreen.this.font.width(string), j + 1, i % 2 == 0 ? 16777215 : 9474192);
			}

			@Override
			public Component getNarration() {
				return Component.translatable("narrator.select", Component.empty().append(this.statDisplay).append(CommonComponents.SPACE).append(this.getValueText()));
			}
		}
	}

	@Environment(EnvType.CLIENT)
	class ItemStatisticsList extends ObjectSelectionList<StatsScreen.ItemStatisticsList.ItemRow> {
		protected final List<StatType<Block>> blockColumns;
		protected final List<StatType<Item>> itemColumns;
		private final ResourceLocation[] iconSprites = new ResourceLocation[]{
			new ResourceLocation("statistics/block_mined"),
			new ResourceLocation("statistics/item_broken"),
			new ResourceLocation("statistics/item_crafted"),
			new ResourceLocation("statistics/item_used"),
			new ResourceLocation("statistics/item_picked_up"),
			new ResourceLocation("statistics/item_dropped")
		};
		protected int headerPressed = -1;
		protected final Comparator<StatsScreen.ItemStatisticsList.ItemRow> itemStatSorter = new StatsScreen.ItemStatisticsList.ItemRowComparator();
		@Nullable
		protected StatType<?> sortColumn;
		protected int sortOrder;

		public ItemStatisticsList(Minecraft minecraft) {
			super(minecraft, StatsScreen.this.width, StatsScreen.this.height, 32, StatsScreen.this.height - 64, 20);
			this.blockColumns = Lists.<StatType<Block>>newArrayList();
			this.blockColumns.add(Stats.BLOCK_MINED);
			this.itemColumns = Lists.<StatType<Item>>newArrayList(Stats.ITEM_BROKEN, Stats.ITEM_CRAFTED, Stats.ITEM_USED, Stats.ITEM_PICKED_UP, Stats.ITEM_DROPPED);
			this.setRenderHeader(true, 20);
			Set<Item> set = Sets.newIdentityHashSet();

			for (Item item : BuiltInRegistries.ITEM) {
				boolean bl = false;

				for (StatType<Item> statType : this.itemColumns) {
					if (statType.contains(item) && StatsScreen.this.stats.getValue(statType.get(item)) > 0) {
						bl = true;
					}
				}

				if (bl) {
					set.add(item);
				}
			}

			for (Block block : BuiltInRegistries.BLOCK) {
				boolean bl = false;

				for (StatType<Block> statTypex : this.blockColumns) {
					if (statTypex.contains(block) && StatsScreen.this.stats.getValue(statTypex.get(block)) > 0) {
						bl = true;
					}
				}

				if (bl) {
					set.add(block.asItem());
				}
			}

			set.remove(Items.AIR);

			for (Item item : set) {
				this.addEntry(new StatsScreen.ItemStatisticsList.ItemRow(item));
			}
		}

		@Override
		protected void renderHeader(GuiGraphics guiGraphics, int i, int j) {
			if (!this.minecraft.mouseHandler.isLeftPressed()) {
				this.headerPressed = -1;
			}

			for (int k = 0; k < this.iconSprites.length; k++) {
				ResourceLocation resourceLocation = this.headerPressed == k ? StatsScreen.SLOT_SPRITE : StatsScreen.HEADER_SPRITE;
				StatsScreen.this.blitSlotIcon(guiGraphics, i + StatsScreen.this.getColumnX(k) - 18, j + 1, resourceLocation);
			}

			if (this.sortColumn != null) {
				int k = StatsScreen.this.getColumnX(this.getColumnIndex(this.sortColumn)) - 36;
				ResourceLocation resourceLocation = this.sortOrder == 1 ? StatsScreen.SORT_UP_SPRITE : StatsScreen.SORT_DOWN_SPRITE;
				StatsScreen.this.blitSlotIcon(guiGraphics, i + k, j + 1, resourceLocation);
			}

			for (int k = 0; k < this.iconSprites.length; k++) {
				int l = this.headerPressed == k ? 1 : 0;
				StatsScreen.this.blitSlotIcon(guiGraphics, i + StatsScreen.this.getColumnX(k) - 18 + l, j + 1 + l, this.iconSprites[k]);
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
		protected boolean clickedHeader(int i, int j) {
			this.headerPressed = -1;

			for (int k = 0; k < this.iconSprites.length; k++) {
				int l = i - StatsScreen.this.getColumnX(k);
				if (l >= -36 && l <= 0) {
					this.headerPressed = k;
					break;
				}
			}

			if (this.headerPressed >= 0) {
				this.sortByColumn(this.getColumn(this.headerPressed));
				this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
				return true;
			} else {
				return super.clickedHeader(i, j);
			}
		}

		private StatType<?> getColumn(int i) {
			return i < this.blockColumns.size() ? (StatType)this.blockColumns.get(i) : (StatType)this.itemColumns.get(i - this.blockColumns.size());
		}

		private int getColumnIndex(StatType<?> statType) {
			int i = this.blockColumns.indexOf(statType);
			if (i >= 0) {
				return i;
			} else {
				int j = this.itemColumns.indexOf(statType);
				return j >= 0 ? j + this.blockColumns.size() : -1;
			}
		}

		@Override
		protected void renderDecorations(GuiGraphics guiGraphics, int i, int j) {
			if (j >= this.y0 && j <= this.y1) {
				StatsScreen.ItemStatisticsList.ItemRow itemRow = this.getHovered();
				int k = (this.width - this.getRowWidth()) / 2;
				if (itemRow != null) {
					if (i < k + 40 || i > k + 40 + 20) {
						return;
					}

					Item item = itemRow.getItem();
					guiGraphics.renderTooltip(StatsScreen.this.font, this.getString(item), i, j);
				} else {
					Component component = null;
					int l = i - k;

					for (int m = 0; m < this.iconSprites.length; m++) {
						int n = StatsScreen.this.getColumnX(m);
						if (l >= n - 18 && l <= n) {
							component = this.getColumn(m).getDisplayName();
							break;
						}
					}

					if (component != null) {
						guiGraphics.renderTooltip(StatsScreen.this.font, component, i, j);
					}
				}
			}
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

		@Environment(EnvType.CLIENT)
		class ItemRow extends ObjectSelectionList.Entry<StatsScreen.ItemStatisticsList.ItemRow> {
			private final Item item;

			ItemRow(Item item) {
				this.item = item;
			}

			public Item getItem() {
				return this.item;
			}

			@Override
			public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
				StatsScreen.this.blitSlot(guiGraphics, k + 40, j, this.item);

				for (int p = 0; p < StatsScreen.this.itemStatsList.blockColumns.size(); p++) {
					Stat<Block> stat;
					if (this.item instanceof BlockItem) {
						stat = ((StatType)StatsScreen.this.itemStatsList.blockColumns.get(p)).get(((BlockItem)this.item).getBlock());
					} else {
						stat = null;
					}

					this.renderStat(guiGraphics, stat, k + StatsScreen.this.getColumnX(p), j, i % 2 == 0);
				}

				for (int p = 0; p < StatsScreen.this.itemStatsList.itemColumns.size(); p++) {
					this.renderStat(
						guiGraphics,
						((StatType)StatsScreen.this.itemStatsList.itemColumns.get(p)).get(this.item),
						k + StatsScreen.this.getColumnX(p + StatsScreen.this.itemStatsList.blockColumns.size()),
						j,
						i % 2 == 0
					);
				}
			}

			protected void renderStat(GuiGraphics guiGraphics, @Nullable Stat<?> stat, int i, int j, boolean bl) {
				Component component = (Component)(stat == null ? StatsScreen.NO_VALUE_DISPLAY : Component.literal(stat.format(StatsScreen.this.stats.getValue(stat))));
				guiGraphics.drawString(StatsScreen.this.font, component, i - StatsScreen.this.font.width(component), j + 5, bl ? 16777215 : 9474192);
			}

			@Override
			public Component getNarration() {
				return Component.translatable("narrator.select", this.item.getDescription());
			}
		}

		@Environment(EnvType.CLIENT)
		class ItemRowComparator implements Comparator<StatsScreen.ItemStatisticsList.ItemRow> {
			public int compare(StatsScreen.ItemStatisticsList.ItemRow itemRow, StatsScreen.ItemStatisticsList.ItemRow itemRow2) {
				Item item = itemRow.getItem();
				Item item2 = itemRow2.getItem();
				int i;
				int j;
				if (ItemStatisticsList.this.sortColumn == null) {
					i = 0;
					j = 0;
				} else if (ItemStatisticsList.this.blockColumns.contains(ItemStatisticsList.this.sortColumn)) {
					StatType<Block> statType = (StatType<Block>)ItemStatisticsList.this.sortColumn;
					i = item instanceof BlockItem ? StatsScreen.this.stats.getValue(statType, ((BlockItem)item).getBlock()) : -1;
					j = item2 instanceof BlockItem ? StatsScreen.this.stats.getValue(statType, ((BlockItem)item2).getBlock()) : -1;
				} else {
					StatType<Item> statType = (StatType<Item>)ItemStatisticsList.this.sortColumn;
					i = StatsScreen.this.stats.getValue(statType, item);
					j = StatsScreen.this.stats.getValue(statType, item2);
				}

				return i == j
					? ItemStatisticsList.this.sortOrder * Integer.compare(Item.getId(item), Item.getId(item2))
					: ItemStatisticsList.this.sortOrder * Integer.compare(i, j);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	class MobsStatisticsList extends ObjectSelectionList<StatsScreen.MobsStatisticsList.MobRow> {
		public MobsStatisticsList(Minecraft minecraft) {
			super(minecraft, StatsScreen.this.width, StatsScreen.this.height, 32, StatsScreen.this.height - 64, 9 * 4);

			for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
				if (StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED.get(entityType)) > 0 || StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED_BY.get(entityType)) > 0
					)
				 {
					this.addEntry(new StatsScreen.MobsStatisticsList.MobRow(entityType));
				}
			}
		}

		@Environment(EnvType.CLIENT)
		class MobRow extends ObjectSelectionList.Entry<StatsScreen.MobsStatisticsList.MobRow> {
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
			public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
				guiGraphics.drawString(StatsScreen.this.font, this.mobName, k + 2, j + 1, 16777215);
				guiGraphics.drawString(StatsScreen.this.font, this.kills, k + 2 + 10, j + 1 + 9, this.hasKills ? 9474192 : 6316128);
				guiGraphics.drawString(StatsScreen.this.font, this.killedBy, k + 2 + 10, j + 1 + 9 * 2, this.wasKilledBy ? 9474192 : 6316128);
			}

			@Override
			public Component getNarration() {
				return Component.translatable("narrator.select", CommonComponents.joinForNarration(this.kills, this.killedBy));
			}
		}
	}
}
