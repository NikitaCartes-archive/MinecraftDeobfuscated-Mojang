package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.recipebook.PlaceRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

@Environment(EnvType.CLIENT)
public class RecipeBookComponent implements PlaceRecipe<Ingredient>, Renderable, GuiEventListener, NarratableEntry, RecipeShownListener {
	public static final WidgetSprites RECIPE_BUTTON_SPRITES = new WidgetSprites(
		ResourceLocation.withDefaultNamespace("recipe_book/button"), ResourceLocation.withDefaultNamespace("recipe_book/button_highlighted")
	);
	private static final WidgetSprites FILTER_BUTTON_SPRITES = new WidgetSprites(
		ResourceLocation.withDefaultNamespace("recipe_book/filter_enabled"),
		ResourceLocation.withDefaultNamespace("recipe_book/filter_disabled"),
		ResourceLocation.withDefaultNamespace("recipe_book/filter_enabled_highlighted"),
		ResourceLocation.withDefaultNamespace("recipe_book/filter_disabled_highlighted")
	);
	protected static final ResourceLocation RECIPE_BOOK_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/recipe_book.png");
	private static final Component SEARCH_HINT = Component.translatable("gui.recipebook.search_hint")
		.withStyle(ChatFormatting.ITALIC)
		.withStyle(ChatFormatting.GRAY);
	public static final int IMAGE_WIDTH = 147;
	public static final int IMAGE_HEIGHT = 166;
	private static final int OFFSET_X_POSITION = 86;
	private static final Component ONLY_CRAFTABLES_TOOLTIP = Component.translatable("gui.recipebook.toggleRecipes.craftable");
	private static final Component ALL_RECIPES_TOOLTIP = Component.translatable("gui.recipebook.toggleRecipes.all");
	private int xOffset;
	private int width;
	private int height;
	protected final GhostRecipe ghostRecipe = new GhostRecipe();
	private final List<RecipeBookTabButton> tabButtons = Lists.<RecipeBookTabButton>newArrayList();
	@Nullable
	private RecipeBookTabButton selectedTab;
	protected StateSwitchingButton filterButton;
	protected RecipeBookMenu<?, ?> menu;
	protected Minecraft minecraft;
	@Nullable
	private EditBox searchBox;
	private String lastSearch = "";
	private ClientRecipeBook book;
	private final RecipeBookPage recipeBookPage = new RecipeBookPage();
	private final StackedContents stackedContents = new StackedContents();
	private int timesInventoryChanged;
	private boolean ignoreTextInput;
	private boolean visible;
	private boolean widthTooNarrow;

	public void init(int i, int j, Minecraft minecraft, boolean bl, RecipeBookMenu<?, ?> recipeBookMenu) {
		this.minecraft = minecraft;
		this.width = i;
		this.height = j;
		this.menu = recipeBookMenu;
		this.widthTooNarrow = bl;
		minecraft.player.containerMenu = recipeBookMenu;
		this.book = minecraft.player.getRecipeBook();
		this.timesInventoryChanged = minecraft.player.getInventory().getTimesChanged();
		this.visible = this.isVisibleAccordingToBookData();
		if (this.visible) {
			this.initVisuals();
		}
	}

	public void initVisuals() {
		this.xOffset = this.widthTooNarrow ? 0 : 86;
		int i = (this.width - 147) / 2 - this.xOffset;
		int j = (this.height - 166) / 2;
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
		this.recipeBookPage.init(this.minecraft, i, j);
		this.recipeBookPage.addListener(this);
		this.filterButton = new StateSwitchingButton(i + 110, j + 12, 26, 16, this.book.isFiltering(this.menu));
		this.updateFilterButtonTooltip();
		this.initFilterButtonTextures();
		this.tabButtons.clear();

		for (RecipeBookCategories recipeBookCategories : RecipeBookCategories.getCategories(this.menu.getRecipeBookType())) {
			this.tabButtons.add(new RecipeBookTabButton(recipeBookCategories));
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
		this.updateCollections(false);
		this.updateTabs();
	}

	private void updateFilterButtonTooltip() {
		this.filterButton.setTooltip(this.filterButton.isStateTriggered() ? Tooltip.create(this.getRecipeFilterName()) : Tooltip.create(ALL_RECIPES_TOOLTIP));
	}

	protected void initFilterButtonTextures() {
		this.filterButton.initTextureValues(FILTER_BUTTON_SPRITES);
	}

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

	public void slotClicked(@Nullable Slot slot) {
		if (slot != null && slot.index < this.menu.getSize()) {
			this.ghostRecipe.clear();
			if (this.isVisible()) {
				this.updateStackedContents();
			}
		}
	}

	private void updateCollections(boolean bl) {
		List<RecipeCollection> list = this.book.getCollection(this.selectedTab.getCategory());
		list.forEach(recipeCollection -> recipeCollection.canCraft(this.stackedContents, this.menu.getGridWidth(), this.menu.getGridHeight(), this.book));
		List<RecipeCollection> list2 = Lists.<RecipeCollection>newArrayList(list);
		list2.removeIf(recipeCollection -> !recipeCollection.hasKnownRecipes());
		list2.removeIf(recipeCollection -> !recipeCollection.hasFitting());
		String string = this.searchBox.getValue();
		if (!string.isEmpty()) {
			ClientPacketListener clientPacketListener = this.minecraft.getConnection();
			if (clientPacketListener != null) {
				ObjectSet<RecipeCollection> objectSet = new ObjectLinkedOpenHashSet<>(clientPacketListener.searchTrees().recipes().search(string.toLowerCase(Locale.ROOT)));
				list2.removeIf(recipeCollection -> !objectSet.contains(recipeCollection));
			}
		}

		if (this.book.isFiltering(this.menu)) {
			list2.removeIf(recipeCollection -> !recipeCollection.hasCraftable());
		}

		this.recipeBookPage.updateCollections(list2, bl);
	}

	private void updateTabs() {
		int i = (this.width - 147) / 2 - this.xOffset - 30;
		int j = (this.height - 166) / 2 + 3;
		int k = 27;
		int l = 0;

		for (RecipeBookTabButton recipeBookTabButton : this.tabButtons) {
			RecipeBookCategories recipeBookCategories = recipeBookTabButton.getCategory();
			if (recipeBookCategories == RecipeBookCategories.CRAFTING_SEARCH || recipeBookCategories == RecipeBookCategories.FURNACE_SEARCH) {
				recipeBookTabButton.visible = true;
				recipeBookTabButton.setPosition(i, j + 27 * l++);
			} else if (recipeBookTabButton.updateVisibility(this.book)) {
				recipeBookTabButton.setPosition(i, j + 27 * l++);
				recipeBookTabButton.startAnimation(this.minecraft);
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
		this.updateCollections(false);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		if (this.isVisible()) {
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
			int k = (this.width - 147) / 2 - this.xOffset;
			int l = (this.height - 166) / 2;
			guiGraphics.blit(RECIPE_BOOK_LOCATION, k, l, 1, 1, 147, 166);
			this.searchBox.render(guiGraphics, i, j, f);

			for (RecipeBookTabButton recipeBookTabButton : this.tabButtons) {
				recipeBookTabButton.render(guiGraphics, i, j, f);
			}

			this.filterButton.render(guiGraphics, i, j, f);
			this.recipeBookPage.render(guiGraphics, k, l, i, j, f);
			guiGraphics.pose().popPose();
		}
	}

	public void renderTooltip(GuiGraphics guiGraphics, int i, int j, int k, int l) {
		if (this.isVisible()) {
			this.recipeBookPage.renderTooltip(guiGraphics, k, l);
			this.renderGhostRecipeTooltip(guiGraphics, i, j, k, l);
		}
	}

	protected Component getRecipeFilterName() {
		return ONLY_CRAFTABLES_TOOLTIP;
	}

	private void renderGhostRecipeTooltip(GuiGraphics guiGraphics, int i, int j, int k, int l) {
		ItemStack itemStack = null;

		for (int m = 0; m < this.ghostRecipe.size(); m++) {
			GhostRecipe.GhostIngredient ghostIngredient = this.ghostRecipe.get(m);
			int n = ghostIngredient.getX() + i;
			int o = ghostIngredient.getY() + j;
			if (k >= n && l >= o && k < n + 16 && l < o + 16) {
				itemStack = ghostIngredient.getItem();
			}
		}

		if (itemStack != null && this.minecraft.screen != null) {
			guiGraphics.renderComponentTooltip(this.minecraft.font, Screen.getTooltipFromItem(this.minecraft, itemStack), k, l);
		}
	}

	public void renderGhostRecipe(GuiGraphics guiGraphics, int i, int j, boolean bl, float f) {
		this.ghostRecipe.render(guiGraphics, this.minecraft, i, j, bl, f);
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		if (this.isVisible() && !this.minecraft.player.isSpectator()) {
			if (this.recipeBookPage.mouseClicked(d, e, i, (this.width - 147) / 2 - this.xOffset, (this.height - 166) / 2, 147, 166)) {
				RecipeHolder<?> recipeHolder = this.recipeBookPage.getLastClickedRecipe();
				RecipeCollection recipeCollection = this.recipeBookPage.getLastClickedRecipeCollection();
				if (recipeHolder != null && recipeCollection != null) {
					if (!recipeCollection.isCraftable(recipeHolder) && this.ghostRecipe.getRecipe() == recipeHolder) {
						return false;
					}

					this.ghostRecipe.clear();
					this.minecraft.gameMode.handlePlaceRecipe(this.minecraft.player.containerMenu.containerId, recipeHolder, Screen.hasShiftDown());
					if (!this.isOffsetNextToMainGUI()) {
						this.setVisible(false);
					}
				}

				return true;
			} else if (this.searchBox.mouseClicked(d, e, i)) {
				this.searchBox.setFocused(true);
				return true;
			} else {
				this.searchBox.setFocused(false);
				if (this.filterButton.mouseClicked(d, e, i)) {
					boolean bl = this.toggleFiltering();
					this.filterButton.setStateTriggered(bl);
					this.updateFilterButtonTooltip();
					this.sendUpdateSettings();
					this.updateCollections(false);
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
								this.updateCollections(true);
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
			this.updateCollections(false);
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
		this.updateTabs();
		if (this.isVisible()) {
			this.updateCollections(false);
		}
	}

	@Override
	public void recipesShown(List<RecipeHolder<?>> list) {
		for (RecipeHolder<?> recipeHolder : list) {
			this.minecraft.player.removeRecipeHighlight(recipeHolder);
		}
	}

	public void setupGhostRecipe(RecipeHolder<?> recipeHolder, List<Slot> list) {
		ItemStack itemStack = recipeHolder.value().getResultItem(this.minecraft.level.registryAccess());
		this.ghostRecipe.setRecipe(recipeHolder);
		this.ghostRecipe.addIngredient(Ingredient.of(itemStack), ((Slot)list.get(0)).x, ((Slot)list.get(0)).y);
		this.placeRecipe(
			this.menu.getGridWidth(), this.menu.getGridHeight(), this.menu.getResultSlotIndex(), recipeHolder, recipeHolder.value().getIngredients().iterator(), 0
		);
	}

	public void addItemToSlot(Ingredient ingredient, int i, int j, int k, int l) {
		if (!ingredient.isEmpty()) {
			Slot slot = this.menu.slots.get(i);
			this.ghostRecipe.addIngredient(ingredient, slot.x, slot.y);
		}
	}

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
}
