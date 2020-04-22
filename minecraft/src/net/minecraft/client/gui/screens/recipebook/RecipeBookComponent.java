package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.Language;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundRecipeBookUpdatePacket;
import net.minecraft.recipebook.PlaceRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

@Environment(EnvType.CLIENT)
public class RecipeBookComponent extends GuiComponent implements Widget, GuiEventListener, RecipeShownListener, PlaceRecipe<Ingredient> {
	protected static final ResourceLocation RECIPE_BOOK_LOCATION = new ResourceLocation("textures/gui/recipe_book.png");
	private int xOffset;
	private int width;
	private int height;
	protected final GhostRecipe ghostRecipe = new GhostRecipe();
	private final List<RecipeBookTabButton> tabButtons = Lists.<RecipeBookTabButton>newArrayList();
	private RecipeBookTabButton selectedTab;
	protected StateSwitchingButton filterButton;
	protected RecipeBookMenu<?> menu;
	protected Minecraft minecraft;
	private EditBox searchBox;
	private String lastSearch = "";
	protected ClientRecipeBook book;
	protected final RecipeBookPage recipeBookPage = new RecipeBookPage();
	protected final StackedContents stackedContents = new StackedContents();
	private int timesInventoryChanged;
	private boolean ignoreTextInput;

	public void init(int i, int j, Minecraft minecraft, boolean bl, RecipeBookMenu<?> recipeBookMenu) {
		this.minecraft = minecraft;
		this.width = i;
		this.height = j;
		this.menu = recipeBookMenu;
		minecraft.player.containerMenu = recipeBookMenu;
		this.book = minecraft.player.getRecipeBook();
		this.timesInventoryChanged = minecraft.player.inventory.getTimesChanged();
		if (this.isVisible()) {
			this.initVisuals(bl);
		}

		minecraft.keyboardHandler.setSendRepeatsToGui(true);
	}

	public void initVisuals(boolean bl) {
		this.xOffset = bl ? 0 : 86;
		int i = (this.width - 147) / 2 - this.xOffset;
		int j = (this.height - 166) / 2;
		this.stackedContents.clear();
		this.minecraft.player.inventory.fillStackedContents(this.stackedContents);
		this.menu.fillCraftSlotsStackedContents(this.stackedContents);
		String string = this.searchBox != null ? this.searchBox.getValue() : "";
		this.searchBox = new EditBox(this.minecraft.font, i + 25, j + 14, 80, 9 + 5, new TranslatableComponent("itemGroup.search"));
		this.searchBox.setMaxLength(50);
		this.searchBox.setBordered(false);
		this.searchBox.setVisible(true);
		this.searchBox.setTextColor(16777215);
		this.searchBox.setValue(string);
		this.recipeBookPage.init(this.minecraft, i, j);
		this.recipeBookPage.addListener(this);
		this.filterButton = new StateSwitchingButton(i + 110, j + 12, 26, 16, this.book.isFilteringCraftable(this.menu));
		this.initFilterButtonTextures();
		this.tabButtons.clear();

		for (RecipeBookCategories recipeBookCategories : ClientRecipeBook.getCategories(this.menu)) {
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

	@Override
	public boolean changeFocus(boolean bl) {
		return false;
	}

	protected void initFilterButtonTextures() {
		this.filterButton.initTextureValues(152, 41, 28, 18, RECIPE_BOOK_LOCATION);
	}

	public void removed() {
		this.searchBox = null;
		this.selectedTab = null;
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
	}

	public int updateScreenPosition(boolean bl, int i, int j) {
		int k;
		if (this.isVisible() && !bl) {
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
		return this.book.isGuiOpen();
	}

	protected void setVisible(boolean bl) {
		this.book.setGuiOpen(bl);
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
			ObjectSet<RecipeCollection> objectSet = new ObjectLinkedOpenHashSet<>(
				this.minecraft.getSearchTree(SearchRegistry.RECIPE_COLLECTIONS).search(string.toLowerCase(Locale.ROOT))
			);
			list2.removeIf(recipeCollection -> !objectSet.contains(recipeCollection));
		}

		if (this.book.isFilteringCraftable(this.menu)) {
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
			if (recipeBookCategories == RecipeBookCategories.SEARCH || recipeBookCategories == RecipeBookCategories.FURNACE_SEARCH) {
				recipeBookTabButton.visible = true;
				recipeBookTabButton.setPosition(i, j + 27 * l++);
			} else if (recipeBookTabButton.updateVisibility(this.book)) {
				recipeBookTabButton.setPosition(i, j + 27 * l++);
				recipeBookTabButton.startAnimation(this.minecraft);
			}
		}
	}

	public void tick() {
		if (this.isVisible()) {
			if (this.timesInventoryChanged != this.minecraft.player.inventory.getTimesChanged()) {
				this.updateStackedContents();
				this.timesInventoryChanged = this.minecraft.player.inventory.getTimesChanged();
			}
		}
	}

	private void updateStackedContents() {
		this.stackedContents.clear();
		this.minecraft.player.inventory.fillStackedContents(this.stackedContents);
		this.menu.fillCraftSlotsStackedContents(this.stackedContents);
		this.updateCollections(false);
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		if (this.isVisible()) {
			RenderSystem.pushMatrix();
			RenderSystem.translatef(0.0F, 0.0F, 100.0F);
			this.minecraft.getTextureManager().bind(RECIPE_BOOK_LOCATION);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			int k = (this.width - 147) / 2 - this.xOffset;
			int l = (this.height - 166) / 2;
			this.blit(poseStack, k, l, 1, 1, 147, 166);
			this.searchBox.render(poseStack, i, j, f);

			for (RecipeBookTabButton recipeBookTabButton : this.tabButtons) {
				recipeBookTabButton.render(poseStack, i, j, f);
			}

			this.filterButton.render(poseStack, i, j, f);
			this.recipeBookPage.render(poseStack, k, l, i, j, f);
			RenderSystem.popMatrix();
		}
	}

	public void renderTooltip(PoseStack poseStack, int i, int j, int k, int l) {
		if (this.isVisible()) {
			this.recipeBookPage.renderTooltip(poseStack, k, l);
			if (this.filterButton.isHovered()) {
				Component component = this.getFilterButtonTooltip();
				if (this.minecraft.screen != null) {
					this.minecraft.screen.renderTooltip(poseStack, component, k, l);
				}
			}

			this.renderGhostRecipeTooltip(poseStack, i, j, k, l);
		}
	}

	protected Component getFilterButtonTooltip() {
		return new TranslatableComponent(this.filterButton.isStateTriggered() ? "gui.recipebook.toggleRecipes.craftable" : "gui.recipebook.toggleRecipes.all");
	}

	private void renderGhostRecipeTooltip(PoseStack poseStack, int i, int j, int k, int l) {
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
			this.minecraft.screen.renderTooltip(poseStack, this.minecraft.screen.getTooltipFromItem(itemStack), k, l);
		}
	}

	public void renderGhostRecipe(PoseStack poseStack, int i, int j, boolean bl, float f) {
		this.ghostRecipe.render(poseStack, this.minecraft, i, j, bl, f);
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		if (this.isVisible() && !this.minecraft.player.isSpectator()) {
			if (this.recipeBookPage.mouseClicked(d, e, i, (this.width - 147) / 2 - this.xOffset, (this.height - 166) / 2, 147, 166)) {
				Recipe<?> recipe = this.recipeBookPage.getLastClickedRecipe();
				RecipeCollection recipeCollection = this.recipeBookPage.getLastClickedRecipeCollection();
				if (recipe != null && recipeCollection != null) {
					if (!recipeCollection.isCraftable(recipe) && this.ghostRecipe.getRecipe() == recipe) {
						return false;
					}

					this.ghostRecipe.clear();
					this.minecraft.gameMode.handlePlaceRecipe(this.minecraft.player.containerMenu.containerId, recipe, Screen.hasShiftDown());
					if (!this.isOffsetNextToMainGUI()) {
						this.setVisible(false);
					}
				}

				return true;
			} else if (this.searchBox.mouseClicked(d, e, i)) {
				return true;
			} else if (this.filterButton.mouseClicked(d, e, i)) {
				boolean bl = this.updateFiltering();
				this.filterButton.setStateTriggered(bl);
				this.sendUpdateSettings();
				this.updateCollections(false);
				return true;
			} else {
				for (RecipeBookTabButton recipeBookTabButton : this.tabButtons) {
					if (recipeBookTabButton.mouseClicked(d, e, i)) {
						if (this.selectedTab != recipeBookTabButton) {
							this.selectedTab.setStateTriggered(false);
							this.selectedTab = recipeBookTabButton;
							this.selectedTab.setStateTriggered(true);
							this.updateCollections(true);
						}

						return true;
					}
				}

				return false;
			}
		} else {
			return false;
		}
	}

	protected boolean updateFiltering() {
		boolean bl = !this.book.isFilteringCraftable();
		this.book.setFilteringCraftable(bl);
		return bl;
	}

	public boolean hasClickedOutside(double d, double e, int i, int j, int k, int l, int m) {
		if (!this.isVisible()) {
			return true;
		} else {
			boolean bl = d < (double)i || e < (double)j || d >= (double)(i + k) || e >= (double)(j + l);
			boolean bl2 = (double)(i - 147) < d && d < (double)i && (double)j < e && e < (double)(j + l);
			return bl && !bl2 && !this.selectedTab.isHovered();
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
			this.searchBox.setFocus(true);
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
			Language language = languageManager.getLanguage("en_pt");
			if (languageManager.getSelected().compareTo(language) == 0) {
				return;
			}

			languageManager.setSelected(language);
			this.minecraft.options.languageCode = language.getCode();
			this.minecraft.reloadResourcePacks();
			this.minecraft.font.setBidirectional(languageManager.isBidirectional());
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
	public void recipesShown(List<Recipe<?>> list) {
		for (Recipe<?> recipe : list) {
			this.minecraft.player.removeRecipeHighlight(recipe);
		}
	}

	public void setupGhostRecipe(Recipe<?> recipe, List<Slot> list) {
		ItemStack itemStack = recipe.getResultItem();
		this.ghostRecipe.setRecipe(recipe);
		this.ghostRecipe.addIngredient(Ingredient.of(itemStack), ((Slot)list.get(0)).x, ((Slot)list.get(0)).y);
		this.placeRecipe(this.menu.getGridWidth(), this.menu.getGridHeight(), this.menu.getResultSlotIndex(), recipe, recipe.getIngredients().iterator(), 0);
	}

	@Override
	public void addItemToSlot(Iterator<Ingredient> iterator, int i, int j, int k, int l) {
		Ingredient ingredient = (Ingredient)iterator.next();
		if (!ingredient.isEmpty()) {
			Slot slot = (Slot)this.menu.slots.get(i);
			this.ghostRecipe.addIngredient(ingredient, slot.x, slot.y);
		}
	}

	protected void sendUpdateSettings() {
		if (this.minecraft.getConnection() != null) {
			this.minecraft
				.getConnection()
				.send(
					new ServerboundRecipeBookUpdatePacket(
						this.book.isGuiOpen(),
						this.book.isFilteringCraftable(),
						this.book.isFurnaceGuiOpen(),
						this.book.isFurnaceFilteringCraftable(),
						this.book.isBlastingFurnaceGuiOpen(),
						this.book.isBlastingFurnaceFilteringCraftable()
					)
				);
		}
	}
}
