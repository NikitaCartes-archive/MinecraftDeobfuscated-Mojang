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
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.CommonComponents;
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
import net.minecraft.world.level.block.Block;

@Environment(EnvType.CLIENT)
public class StatsScreen extends Screen implements StatsUpdateListener {
	private static final Component PENDING_TEXT = new TranslatableComponent("multiplayer.downloadingStats");
	protected final Screen lastScreen;
	private StatsScreen.GeneralStatisticsList statsList;
	StatsScreen.ItemStatisticsList itemStatsList;
	private StatsScreen.MobsStatisticsList mobsStatsList;
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
		super(new TranslatableComponent("gui.stats"));
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
		this.addButton(
			new Button(this.width / 2 - 120, this.height - 52, 80, 20, new TranslatableComponent("stat.generalButton"), buttonx -> this.setActiveList(this.statsList))
		);
		Button button = this.addButton(
			new Button(this.width / 2 - 40, this.height - 52, 80, 20, new TranslatableComponent("stat.itemsButton"), buttonx -> this.setActiveList(this.itemStatsList))
		);
		Button button2 = this.addButton(
			new Button(this.width / 2 + 40, this.height - 52, 80, 20, new TranslatableComponent("stat.mobsButton"), buttonx -> this.setActiveList(this.mobsStatsList))
		);
		this.addButton(new Button(this.width / 2 - 100, this.height - 28, 200, 20, CommonComponents.GUI_DONE, buttonx -> this.minecraft.setScreen(this.lastScreen)));
		if (this.itemStatsList.children().isEmpty()) {
			button.active = false;
		}

		if (this.mobsStatsList.children().isEmpty()) {
			button2.active = false;
		}
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		if (this.isLoading) {
			this.renderBackground(poseStack);
			drawCenteredString(poseStack, this.font, PENDING_TEXT, this.width / 2, this.height / 2, 16777215);
			drawCenteredString(
				poseStack, this.font, LOADING_SYMBOLS[(int)(Util.getMillis() / 150L % (long)LOADING_SYMBOLS.length)], this.width / 2, this.height / 2 + 9 * 2, 16777215
			);
		} else {
			this.getActiveList().render(poseStack, i, j, f);
			drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, 16777215);
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
		this.children.remove(this.statsList);
		this.children.remove(this.itemStatsList);
		this.children.remove(this.mobsStatsList);
		if (objectSelectionList != null) {
			this.children.add(0, objectSelectionList);
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
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, STATS_ICON_LOCATION);
		blit(poseStack, i, j, this.getBlitOffset(), (float)k, (float)l, 18, 18, 128, 128);
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

		@Override
		protected void renderBackground(PoseStack poseStack) {
			StatsScreen.this.renderBackground(poseStack);
		}

		@Environment(EnvType.CLIENT)
		class Entry extends ObjectSelectionList.Entry<StatsScreen.GeneralStatisticsList.Entry> {
			private final Stat<ResourceLocation> stat;
			private final Component statDisplay;

			Entry(Stat<ResourceLocation> stat) {
				this.stat = stat;
				this.statDisplay = new TranslatableComponent(StatsScreen.getTranslationKey(stat));
			}

			@Override
			public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
				GuiComponent.drawString(poseStack, StatsScreen.this.font, this.statDisplay, k + 2, j + 1, i % 2 == 0 ? 16777215 : 9474192);
				String string = this.stat.format(StatsScreen.this.stats.getValue(this.stat));
				GuiComponent.drawString(poseStack, StatsScreen.this.font, string, k + 2 + 213 - StatsScreen.this.font.width(string), j + 1, i % 2 == 0 ? 16777215 : 9474192);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	class ItemStatisticsList extends ObjectSelectionList<StatsScreen.ItemStatisticsList.ItemRow> {
		protected final List<StatType<Block>> blockColumns;
		protected final List<StatType<Item>> itemColumns;
		private final int[] iconOffsets = new int[]{3, 4, 1, 2, 5, 6};
		protected int headerPressed = -1;
		protected final List<Item> statItemList;
		protected final Comparator<Item> itemStatSorter = new StatsScreen.ItemStatisticsList.ItemComparator();
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

			for (Item item : Registry.ITEM) {
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

			for (Block block : Registry.BLOCK) {
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
			this.statItemList = Lists.<Item>newArrayList(set);

			for (int i = 0; i < this.statItemList.size(); i++) {
				this.addEntry(new StatsScreen.ItemStatisticsList.ItemRow());
			}
		}

		@Override
		protected void renderHeader(PoseStack poseStack, int i, int j, Tesselator tesselator) {
			if (!this.minecraft.mouseHandler.isLeftPressed()) {
				this.headerPressed = -1;
			}

			for (int k = 0; k < this.iconOffsets.length; k++) {
				StatsScreen.this.blitSlotIcon(poseStack, i + StatsScreen.this.getColumnX(k) - 18, j + 1, 0, this.headerPressed == k ? 0 : 18);
			}

			if (this.sortColumn != null) {
				int k = StatsScreen.this.getColumnX(this.getColumnIndex(this.sortColumn)) - 36;
				int l = this.sortOrder == 1 ? 2 : 1;
				StatsScreen.this.blitSlotIcon(poseStack, i + k, j + 1, 18 * l, 0);
			}

			for (int k = 0; k < this.iconOffsets.length; k++) {
				int l = this.headerPressed == k ? 1 : 0;
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

			for (int k = 0; k < this.iconOffsets.length; k++) {
				int l = i - StatsScreen.this.getColumnX(k);
				if (l >= -36 && l <= 0) {
					this.headerPressed = k;
					break;
				}
			}

			if (this.headerPressed >= 0) {
				this.sortByColumn(this.getColumn(this.headerPressed));
				this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
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
		protected void renderDecorations(PoseStack poseStack, int i, int j) {
			if (j >= this.y0 && j <= this.y1) {
				StatsScreen.ItemStatisticsList.ItemRow itemRow = this.getEntryAtPosition((double)i, (double)j);
				int k = (this.width - this.getRowWidth()) / 2;
				if (itemRow != null) {
					if (i < k + 40 || i > k + 40 + 20) {
						return;
					}

					Item item = (Item)this.statItemList.get(this.children().indexOf(itemRow));
					this.renderMousehoverTooltip(poseStack, this.getString(item), i, j);
				} else {
					Component component = null;
					int l = i - k;

					for (int m = 0; m < this.iconOffsets.length; m++) {
						int n = StatsScreen.this.getColumnX(m);
						if (l >= n - 18 && l <= n) {
							component = this.getColumn(m).getDisplayName();
							break;
						}
					}

					this.renderMousehoverTooltip(poseStack, component, i, j);
				}
			}
		}

		protected void renderMousehoverTooltip(PoseStack poseStack, @Nullable Component component, int i, int j) {
			if (component != null) {
				int k = i + 12;
				int l = j - 12;
				int m = StatsScreen.this.font.width(component);
				this.fillGradient(poseStack, k - 3, l - 3, k + m + 3, l + 8 + 3, -1073741824, -1073741824);
				poseStack.pushPose();
				poseStack.translate(0.0, 0.0, 400.0);
				StatsScreen.this.font.drawShadow(poseStack, component, (float)k, (float)l, -1);
				poseStack.popPose();
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

			this.statItemList.sort(this.itemStatSorter);
		}

		@Environment(EnvType.CLIENT)
		class ItemComparator implements Comparator<Item> {
			public int compare(Item item, Item item2) {
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

		@Environment(EnvType.CLIENT)
		class ItemRow extends ObjectSelectionList.Entry<StatsScreen.ItemStatisticsList.ItemRow> {
			@Override
			public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
				Item item = (Item)StatsScreen.this.itemStatsList.statItemList.get(i);
				StatsScreen.this.blitSlot(poseStack, k + 40, j, item);

				for (int p = 0; p < StatsScreen.this.itemStatsList.blockColumns.size(); p++) {
					Stat<Block> stat;
					if (item instanceof BlockItem) {
						stat = ((StatType)StatsScreen.this.itemStatsList.blockColumns.get(p)).get(((BlockItem)item).getBlock());
					} else {
						stat = null;
					}

					this.renderStat(poseStack, stat, k + StatsScreen.this.getColumnX(p), j, i % 2 == 0);
				}

				for (int p = 0; p < StatsScreen.this.itemStatsList.itemColumns.size(); p++) {
					this.renderStat(
						poseStack,
						((StatType)StatsScreen.this.itemStatsList.itemColumns.get(p)).get(item),
						k + StatsScreen.this.getColumnX(p + StatsScreen.this.itemStatsList.blockColumns.size()),
						j,
						i % 2 == 0
					);
				}
			}

			protected void renderStat(PoseStack poseStack, @Nullable Stat<?> stat, int i, int j, boolean bl) {
				String string = stat == null ? "-" : stat.format(StatsScreen.this.stats.getValue(stat));
				GuiComponent.drawString(poseStack, StatsScreen.this.font, string, i - StatsScreen.this.font.width(string), j + 5, bl ? 16777215 : 9474192);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	class MobsStatisticsList extends ObjectSelectionList<StatsScreen.MobsStatisticsList.MobRow> {
		public MobsStatisticsList(Minecraft minecraft) {
			super(minecraft, StatsScreen.this.width, StatsScreen.this.height, 32, StatsScreen.this.height - 64, 9 * 4);

			for (EntityType<?> entityType : Registry.ENTITY_TYPE) {
				if (StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED.get(entityType)) > 0 || StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED_BY.get(entityType)) > 0
					)
				 {
					this.addEntry(new StatsScreen.MobsStatisticsList.MobRow(entityType));
				}
			}
		}

		@Override
		protected void renderBackground(PoseStack poseStack) {
			StatsScreen.this.renderBackground(poseStack);
		}

		@Environment(EnvType.CLIENT)
		class MobRow extends ObjectSelectionList.Entry<StatsScreen.MobsStatisticsList.MobRow> {
			private final EntityType<?> type;
			private final Component mobName;
			private final Component kills;
			private final boolean hasKills;
			private final Component killedBy;
			private final boolean wasKilledBy;

			public MobRow(EntityType<?> entityType) {
				this.type = entityType;
				this.mobName = entityType.getDescription();
				int i = StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED.get(entityType));
				if (i == 0) {
					this.kills = new TranslatableComponent("stat_type.minecraft.killed.none", this.mobName);
					this.hasKills = false;
				} else {
					this.kills = new TranslatableComponent("stat_type.minecraft.killed", i, this.mobName);
					this.hasKills = true;
				}

				int j = StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED_BY.get(entityType));
				if (j == 0) {
					this.killedBy = new TranslatableComponent("stat_type.minecraft.killed_by.none", this.mobName);
					this.wasKilledBy = false;
				} else {
					this.killedBy = new TranslatableComponent("stat_type.minecraft.killed_by", this.mobName, j);
					this.wasKilledBy = true;
				}
			}

			@Override
			public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
				GuiComponent.drawString(poseStack, StatsScreen.this.font, this.mobName, k + 2, j + 1, 16777215);
				GuiComponent.drawString(poseStack, StatsScreen.this.font, this.kills, k + 2 + 10, j + 1 + 9, this.hasKills ? 9474192 : 6316128);
				GuiComponent.drawString(poseStack, StatsScreen.this.font, this.killedBy, k + 2 + 10, j + 1 + 9 * 2, this.wasKilledBy ? 9474192 : 6316128);
			}
		}
	}
}
