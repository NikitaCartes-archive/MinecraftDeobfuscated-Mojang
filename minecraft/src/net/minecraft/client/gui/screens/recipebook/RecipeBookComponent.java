package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.ExtendedRecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public abstract class RecipeBookComponent<T extends RecipeBookMenu> implements Renderable, GuiEventListener, NarratableEntry {
	public static final WidgetSprites RECIPE_BUTTON_SPRITES = new WidgetSprites(
		ResourceLocation.withDefaultNamespace("recipe_book/button"), ResourceLocation.withDefaultNamespace("recipe_book/button_highlighted")
	);
	protected static final ResourceLocation RECIPE_BOOK_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/recipe_book.png");
	private static final int BACKGROUND_TEXTURE_WIDTH = 256;
	private static final int BACKGROUND_TEXTURE_HEIGHT = 256;
	private static final Component SEARCH_HINT = Component.translatable("gui.recipebook.search_hint")
		.withStyle(ChatFormatting.ITALIC)
		.withStyle(ChatFormatting.GRAY);
	public static final int IMAGE_WIDTH = 147;
	public static final int IMAGE_HEIGHT = 166;
	private static final int OFFSET_X_POSITION = 86;
	private static final int BORDER_WIDTH = 8;
	private static final Component ALL_RECIPES_TOOLTIP = Component.translatable("gui.recipebook.toggleRecipes.all");
	private static final int TICKS_TO_SWAP_SLOT = 30;
	private int xOffset;
	private int width;
	private int height;
	private float time;
	@Nullable
	private RecipeDisplayId lastPlacedRecipe;
	private final GhostSlots ghostSlots;
	private final List<RecipeBookTabButton> tabButtons = Lists.<RecipeBookTabButton>newArrayList();
	@Nullable
	private RecipeBookTabButton selectedTab;
	protected StateSwitchingButton filterButton;
	protected final T menu;
	protected Minecraft minecraft;
	@Nullable
	private EditBox searchBox;
	private String lastSearch = "";
	private final List<RecipeBookComponent.TabInfo> tabInfos;
	private ClientRecipeBook book;
	private final RecipeBookPage recipeBookPage;
	@Nullable
	private RecipeDisplayId lastRecipe;
	@Nullable
	private RecipeCollection lastRecipeCollection;
	private final StackedItemContents stackedContents = new StackedItemContents();
	private int timesInventoryChanged;
	private boolean ignoreTextInput;
	private boolean visible;
	private boolean widthTooNarrow;
	@Nullable
	private ScreenRectangle magnifierIconPlacement;

	public RecipeBookComponent(T recipeBookMenu, List<RecipeBookComponent.TabInfo> list) {
		this.menu = recipeBookMenu;
		this.tabInfos = list;
		SlotSelectTime slotSelectTime = () -> Mth.floor(this.time / 30.0F);
		this.ghostSlots = new GhostSlots(slotSelectTime);
		this.recipeBookPage = new RecipeBookPage(this, slotSelectTime, recipeBookMenu instanceof AbstractFurnaceMenu);
	}

	public void init(int i, int j, Minecraft minecraft, boolean bl) {
		this.minecraft = minecraft;
		this.width = i;
		this.height = j;
		this.widthTooNarrow = bl;
		this.book = minecraft.player.getRecipeBook();
		this.timesInventoryChanged = minecraft.player.getInventory().getTimesChanged();
		this.visible = this.isVisibleAccordingToBookData();
		if (this.visible) {
			this.initVisuals();
		}
	}

	private void initVisuals() {
		boolean bl = this.isFiltering();
		this.xOffset = this.widthTooNarrow ? 0 : 86;
		int i = this.getXOrigin();
		int j = this.getYOrigin();
		this.stackedContents.clear();
		this.minecraft.player.getInventory().fillStackedContents(this.stackedContents);
		this.menu.fillCraftSlotsStackedContents(this.stackedContents);
		String string = this.searchBox != null ? this.searchBox.getValue() : "";
		this.searchBox = new EditBox(this.minecraft.font, i + 25, j + 13, 81, 9 + 5, Component.translatable("itemGroup.search"));
		this.searchBox.setMaxLength(50);
		this.searchBox.setVisible(true);
		this.searchBox.setTextColor(16777215);
		this.searchBox.setValue(string);
		this.searchBox.setHint(SEARCH_HINT);
		this.magnifierIconPlacement = ScreenRectangle.of(
			ScreenAxis.HORIZONTAL, i + 8, this.searchBox.getY(), this.searchBox.getX() - this.getXOrigin(), this.searchBox.getHeight()
		);
		this.recipeBookPage.init(this.minecraft, i, j);
		this.filterButton = new StateSwitchingButton(i + 110, j + 12, 26, 16, bl);
		this.updateFilterButtonTooltip();
		this.initFilterButtonTextures();
		this.tabButtons.clear();

		for (RecipeBookComponent.TabInfo tabInfo : this.tabInfos) {
			this.tabButtons.add(new RecipeBookTabButton(tabInfo));
		}

		if (this.selectedTab != null) {
			this.selectedTab = (RecipeBookTabButton)this.tabButtons
				.stream()
				.filter(recipeBookTabButton -> recipeBookTabButton.getCategory().equals(this.selectedTab.getCategory()))
				.findFirst()
				.orElse(null);
		}

		if (this.selectedTab == null) {
			this.selectedTab = (RecipeBookTabButton)this.tabButtons.get(0);
		}

		this.selectedTab.setStateTriggered(true);
		this.selectMatchingRecipes();
		this.updateTabs(bl);
		this.updateCollections(false, bl);
	}

	private int getYOrigin() {
		return (this.height - 166) / 2;
	}

	private int getXOrigin() {
		return (this.width - 147) / 2 - this.xOffset;
	}

	private void updateFilterButtonTooltip() {
		this.filterButton.setTooltip(this.filterButton.isStateTriggered() ? Tooltip.create(this.getRecipeFilterName()) : Tooltip.create(ALL_RECIPES_TOOLTIP));
	}

	protected abstract void initFilterButtonTextures();

	public int updateScreenPosition(int i, int j) {
		int k;
		if (this.isVisible() && !this.widthTooNarrow) {
			k = 177 + (i - j - 200) / 2;
		} else {
			k = (i - j) / 2;
		}

		return k;
	}

	public void toggleVisibility() {
		this.setVisible(!this.isVisible());
	}

	public boolean isVisible() {
		return this.visible;
	}

	private boolean isVisibleAccordingToBookData() {
		return this.book.isOpen(this.menu.getRecipeBookType());
	}

	protected void setVisible(boolean bl) {
		if (bl) {
			this.initVisuals();
		}

		this.visible = bl;
		this.book.setOpen(this.menu.getRecipeBookType(), bl);
		if (!bl) {
			this.recipeBookPage.setInvisible();
		}

		this.sendUpdateSettings();
	}

	protected abstract boolean isCraftingSlot(Slot slot);

	public void slotClicked(@Nullable Slot slot) {
		if (slot != null && this.isCraftingSlot(slot)) {
			this.lastPlacedRecipe = null;
			this.ghostSlots.clear();
			if (this.isVisible()) {
				this.updateStackedContents();
			}
		}
	}

	private void selectMatchingRecipes() {
		for (RecipeBookComponent.TabInfo tabInfo : this.tabInfos) {
			for (RecipeCollection recipeCollection : this.book.getCollection(tabInfo.category())) {
				this.selectMatchingRecipes(recipeCollection, this.stackedContents);
			}
		}
	}

	protected abstract void selectMatchingRecipes(RecipeCollection recipeCollection, StackedItemContents stackedItemContents);

	private void updateCollections(boolean bl, boolean bl2) {
		List<RecipeCollection> list = this.book.getCollection(this.selectedTab.getCategory());
		List<RecipeCollection> list2 = Lists.<RecipeCollection>newArrayList(list);
		list2.removeIf(recipeCollection -> !recipeCollection.hasAnySelected());
		String string = this.searchBox.getValue();
		if (!string.isEmpty()) {
			ClientPacketListener clientPacketListener = this.minecraft.getConnection();
			if (clientPacketListener != null) {
				ObjectSet<RecipeCollection> objectSet = new ObjectLinkedOpenHashSet<>(clientPacketListener.searchTrees().recipes().search(string.toLowerCase(Locale.ROOT)));
				list2.removeIf(recipeCollection -> !objectSet.contains(recipeCollection));
			}
		}

		if (bl2) {
			list2.removeIf(recipeCollection -> !recipeCollection.hasCraftable());
		}

		this.recipeBookPage.updateCollections(list2, bl, bl2);
	}

	private void updateTabs(boolean bl) {
		int i = (this.width - 147) / 2 - this.xOffset - 30;
		int j = (this.height - 166) / 2 + 3;
		int k = 27;
		int l = 0;

		for (RecipeBookTabButton recipeBookTabButton : this.tabButtons) {
			ExtendedRecipeBookCategory extendedRecipeBookCategory = recipeBookTabButton.getCategory();
			if (extendedRecipeBookCategory instanceof SearchRecipeBookCategory) {
				recipeBookTabButton.visible = true;
				recipeBookTabButton.setPosition(i, j + 27 * l++);
			} else if (recipeBookTabButton.updateVisibility(this.book)) {
				recipeBookTabButton.setPosition(i, j + 27 * l++);
				recipeBookTabButton.startAnimation(this.book, bl);
			}
		}
	}

	public void tick() {
		boolean bl = this.isVisibleAccordingToBookData();
		if (this.isVisible() != bl) {
			this.setVisible(bl);
		}

		if (this.isVisible()) {
			if (this.timesInventoryChanged != this.minecraft.player.getInventory().getTimesChanged()) {
				this.updateStackedContents();
				this.timesInventoryChanged = this.minecraft.player.getInventory().getTimesChanged();
			}
		}
	}

	private void updateStackedContents() {
		this.stackedContents.clear();
		this.minecraft.player.getInventory().fillStackedContents(this.stackedContents);
		this.menu.fillCraftSlotsStackedContents(this.stackedContents);
		this.selectMatchingRecipes();
		this.updateCollections(false, this.isFiltering());
	}

	private boolean isFiltering() {
		return this.book.isFiltering(this.menu.getRecipeBookType());
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		if (this.isVisible()) {
			if (!Screen.hasControlDown()) {
				this.time += f;
			}

			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
			int k = this.getXOrigin();
			int l = this.getYOrigin();
			guiGraphics.blit(RenderType::guiTextured, RECIPE_BOOK_LOCATION, k, l, 1.0F, 1.0F, 147, 166, 256, 256);
			this.searchBox.render(guiGraphics, i, j, f);

			for (RecipeBookTabButton recipeBookTabButton : this.tabButtons) {
				recipeBookTabButton.render(guiGraphics, i, j, f);
			}

			this.filterButton.render(guiGraphics, i, j, f);
			this.recipeBookPage.render(guiGraphics, k, l, i, j, f);
			guiGraphics.pose().popPose();
		}
	}

	public void renderTooltip(GuiGraphics guiGraphics, int i, int j, @Nullable Slot slot) {
		if (this.isVisible()) {
			this.recipeBookPage.renderTooltip(guiGraphics, i, j);
			this.ghostSlots.renderTooltip(guiGraphics, this.minecraft, i, j, slot);
		}
	}

	protected abstract Component getRecipeFilterName();

	public void renderGhostRecipe(GuiGraphics guiGraphics, boolean bl) {
		this.ghostSlots.render(guiGraphics, this.minecraft, bl);
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		if (this.isVisible() && !this.minecraft.player.isSpectator()) {
			if (this.recipeBookPage.mouseClicked(d, e, i, this.getXOrigin(), this.getYOrigin(), 147, 166)) {
				RecipeDisplayId recipeDisplayId = this.recipeBookPage.getLastClickedRecipe();
				RecipeCollection recipeCollection = this.recipeBookPage.getLastClickedRecipeCollection();
				if (recipeDisplayId != null && recipeCollection != null) {
					if (!this.tryPlaceRecipe(recipeCollection, recipeDisplayId)) {
						return false;
					}

					this.lastRecipeCollection = recipeCollection;
					this.lastRecipe = recipeDisplayId;
					if (!this.isOffsetNextToMainGUI()) {
						this.setVisible(false);
					}
				}

				return true;
			} else {
				if (this.searchBox != null) {
					boolean bl = this.magnifierIconPlacement != null && this.magnifierIconPlacement.containsPoint(Mth.floor(d), Mth.floor(e));
					if (bl || this.searchBox.mouseClicked(d, e, i)) {
						this.searchBox.setFocused(true);
						return true;
					}

					this.searchBox.setFocused(false);
				}

				if (this.filterButton.mouseClicked(d, e, i)) {
					boolean bl = this.toggleFiltering();
					this.filterButton.setStateTriggered(bl);
					this.updateFilterButtonTooltip();
					this.sendUpdateSettings();
					this.updateCollections(false, bl);
					return true;
				} else {
					for (RecipeBookTabButton recipeBookTabButton : this.tabButtons) {
						if (recipeBookTabButton.mouseClicked(d, e, i)) {
							if (this.selectedTab != recipeBookTabButton) {
								if (this.selectedTab != null) {
									this.selectedTab.setStateTriggered(false);
								}

								this.selectedTab = recipeBookTabButton;
								this.selectedTab.setStateTriggered(true);
								this.updateCollections(true, this.isFiltering());
							}

							return true;
						}
					}

					return false;
				}
			}
		} else {
			return false;
		}
	}

	private boolean tryPlaceRecipe(RecipeCollection recipeCollection, RecipeDisplayId recipeDisplayId) {
		if (!recipeCollection.isCraftable(recipeDisplayId) && recipeDisplayId.equals(this.lastPlacedRecipe)) {
			return false;
		} else {
			this.lastPlacedRecipe = recipeDisplayId;
			this.ghostSlots.clear();
			this.minecraft.gameMode.handlePlaceRecipe(this.minecraft.player.containerMenu.containerId, recipeDisplayId, Screen.hasShiftDown());
			return true;
		}
	}

	private boolean toggleFiltering() {
		RecipeBookType recipeBookType = this.menu.getRecipeBookType();
		boolean bl = !this.book.isFiltering(recipeBookType);
		this.book.setFiltering(recipeBookType, bl);
		return bl;
	}

	public boolean hasClickedOutside(double d, double e, int i, int j, int k, int l, int m) {
		if (!this.isVisible()) {
			return true;
		} else {
			boolean bl = d < (double)i || e < (double)j || d >= (double)(i + k) || e >= (double)(j + l);
			boolean bl2 = (double)(i - 147) < d && d < (double)i && (double)j < e && e < (double)(j + l);
			return bl && !bl2 && !this.selectedTab.isHoveredOrFocused();
		}
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		this.ignoreTextInput = false;
		if (!this.isVisible() || this.minecraft.player.isSpectator()) {
			return false;
		} else if (i == 256 && !this.isOffsetNextToMainGUI()) {
			this.setVisible(false);
			return true;
		} else if (this.searchBox.keyPressed(i, j, k)) {
			this.checkSearchStringUpdate();
			return true;
		} else if (this.searchBox.isFocused() && this.searchBox.isVisible() && i != 256) {
			return true;
		} else if (this.minecraft.options.keyChat.matches(i, j) && !this.searchBox.isFocused()) {
			this.ignoreTextInput = true;
			this.searchBox.setFocused(true);
			return true;
		} else if (CommonInputs.selected(i) && this.lastRecipeCollection != null && this.lastRecipe != null) {
			AbstractWidget.playButtonClickSound(Minecraft.getInstance().getSoundManager());
			return this.tryPlaceRecipe(this.lastRecipeCollection, this.lastRecipe);
		} else {
			return false;
		}
	}

	@Override
	public boolean keyReleased(int i, int j, int k) {
		this.ignoreTextInput = false;
		return GuiEventListener.super.keyReleased(i, j, k);
	}

	@Override
	public boolean charTyped(char c, int i) {
		if (this.ignoreTextInput) {
			return false;
		} else if (!this.isVisible() || this.minecraft.player.isSpectator()) {
			return false;
		} else if (this.searchBox.charTyped(c, i)) {
			this.checkSearchStringUpdate();
			return true;
		} else {
			return GuiEventListener.super.charTyped(c, i);
		}
	}

	@Override
	public boolean isMouseOver(double d, double e) {
		return false;
	}

	@Override
	public void setFocused(boolean bl) {
	}

	@Override
	public boolean isFocused() {
		return false;
	}

	private void checkSearchStringUpdate() {
		String string = this.searchBox.getValue().toLowerCase(Locale.ROOT);
		this.pirateSpeechForThePeople(string);
		if (!string.equals(this.lastSearch)) {
			this.updateCollections(false, this.isFiltering());
			this.lastSearch = string;
		}
	}

	private void pirateSpeechForThePeople(String string) {
		if ("excitedze".equals(string)) {
			LanguageManager languageManager = this.minecraft.getLanguageManager();
			String string2 = "en_pt";
			LanguageInfo languageInfo = languageManager.getLanguage("en_pt");
			if (languageInfo == null || languageManager.getSelected().equals("en_pt")) {
				return;
			}

			languageManager.setSelected("en_pt");
			this.minecraft.options.languageCode = "en_pt";
			this.minecraft.reloadResourcePacks();
			this.minecraft.options.save();
		}
	}

	private boolean isOffsetNextToMainGUI() {
		return this.xOffset == 86;
	}

	public void recipesUpdated() {
		this.selectMatchingRecipes();
		this.updateTabs(this.isFiltering());
		if (this.isVisible()) {
			this.updateCollections(false, this.isFiltering());
		}
	}

	public void recipeShown(RecipeDisplayId recipeDisplayId) {
		this.minecraft.player.removeRecipeHighlight(recipeDisplayId);
	}

	public void fillGhostRecipe(RecipeDisplay recipeDisplay) {
		this.ghostSlots.clear();
		ContextMap contextMap = SlotDisplayContext.fromLevel((Level)Objects.requireNonNull(this.minecraft.level));
		this.fillGhostRecipe(this.ghostSlots, recipeDisplay, contextMap);
	}

	protected abstract void fillGhostRecipe(GhostSlots ghostSlots, RecipeDisplay recipeDisplay, ContextMap contextMap);

	protected void sendUpdateSettings() {
		if (this.minecraft.getConnection() != null) {
			RecipeBookType recipeBookType = this.menu.getRecipeBookType();
			boolean bl = this.book.getBookSettings().isOpen(recipeBookType);
			boolean bl2 = this.book.getBookSettings().isFiltering(recipeBookType);
			this.minecraft.getConnection().send(new ServerboundRecipeBookChangeSettingsPacket(recipeBookType, bl, bl2));
		}
	}

	@Override
	public NarratableEntry.NarrationPriority narrationPriority() {
		return this.visible ? NarratableEntry.NarrationPriority.HOVERED : NarratableEntry.NarrationPriority.NONE;
	}

	@Override
	public void updateNarration(NarrationElementOutput narrationElementOutput) {
		List<NarratableEntry> list = Lists.<NarratableEntry>newArrayList();
		this.recipeBookPage.listButtons(abstractWidget -> {
			if (abstractWidget.isActive()) {
				list.add(abstractWidget);
			}
		});
		list.add(this.searchBox);
		list.add(this.filterButton);
		list.addAll(this.tabButtons);
		Screen.NarratableSearchResult narratableSearchResult = Screen.findNarratableWidget(list, null);
		if (narratableSearchResult != null) {
			narratableSearchResult.entry.updateNarration(narrationElementOutput.nest());
		}
	}

	@Environment(EnvType.CLIENT)
	public static record TabInfo(ItemStack primaryIcon, Optional<ItemStack> secondaryIcon, ExtendedRecipeBookCategory category) {
		public TabInfo(SearchRecipeBookCategory searchRecipeBookCategory) {
			this(new ItemStack(Items.COMPASS), Optional.empty(), searchRecipeBookCategory);
		}

		public TabInfo(Item item, RecipeBookCategory recipeBookCategory) {
			this(new ItemStack(item), Optional.empty(), recipeBookCategory);
		}

		public TabInfo(Item item, Item item2, RecipeBookCategory recipeBookCategory) {
			this(new ItemStack(item), Optional.of(new ItemStack(item2)), recipeBookCategory);
		}
	}
}
