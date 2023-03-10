/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.GhostRecipe;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.RecipeShownListener;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.client.searchtree.SearchRegistry;
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
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class RecipeBookComponent
extends GuiComponent
implements PlaceRecipe<Ingredient>,
Renderable,
GuiEventListener,
NarratableEntry,
RecipeShownListener {
    protected static final ResourceLocation RECIPE_BOOK_LOCATION = new ResourceLocation("textures/gui/recipe_book.png");
    private static final Component SEARCH_HINT = Component.translatable("gui.recipebook.search_hint").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY);
    public static final int IMAGE_WIDTH = 147;
    public static final int IMAGE_HEIGHT = 166;
    private static final int OFFSET_X_POSITION = 86;
    private static final Component ONLY_CRAFTABLES_TOOLTIP = Component.translatable("gui.recipebook.toggleRecipes.craftable");
    private static final Component ALL_RECIPES_TOOLTIP = Component.translatable("gui.recipebook.toggleRecipes.all");
    private int xOffset;
    private int width;
    private int height;
    protected final GhostRecipe ghostRecipe = new GhostRecipe();
    private final List<RecipeBookTabButton> tabButtons = Lists.newArrayList();
    @Nullable
    private RecipeBookTabButton selectedTab;
    protected StateSwitchingButton filterButton;
    protected RecipeBookMenu<?> menu;
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

    public void init(int i, int j, Minecraft minecraft, boolean bl, RecipeBookMenu<?> recipeBookMenu) {
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
        this.searchBox = new EditBox(this.minecraft.font, i + 26, j + 14, 79, this.minecraft.font.lineHeight + 3, Component.translatable("itemGroup.search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setVisible(true);
        this.searchBox.setTextColor(0xFFFFFF);
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
            this.selectedTab = this.tabButtons.stream().filter(recipeBookTabButton -> recipeBookTabButton.getCategory().equals((Object)this.selectedTab.getCategory())).findFirst().orElse(null);
        }
        if (this.selectedTab == null) {
            this.selectedTab = this.tabButtons.get(0);
        }
        this.selectedTab.setStateTriggered(true);
        this.updateCollections(false);
        this.updateTabs();
    }

    private void updateFilterButtonTooltip() {
        this.filterButton.setTooltip(this.filterButton.isStateTriggered() ? Tooltip.create(this.getRecipeFilterName()) : Tooltip.create(ALL_RECIPES_TOOLTIP));
    }

    protected void initFilterButtonTextures() {
        this.filterButton.initTextureValues(152, 41, 28, 18, RECIPE_BOOK_LOCATION);
    }

    public int updateScreenPosition(int i, int j) {
        int k = this.isVisible() && !this.widthTooNarrow ? 177 + (i - j - 200) / 2 : (i - j) / 2;
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
        ArrayList<RecipeCollection> list2 = Lists.newArrayList(list);
        list2.removeIf(recipeCollection -> !recipeCollection.hasKnownRecipes());
        list2.removeIf(recipeCollection -> !recipeCollection.hasFitting());
        String string = this.searchBox.getValue();
        if (!string.isEmpty()) {
            ObjectLinkedOpenHashSet<RecipeCollection> objectSet = new ObjectLinkedOpenHashSet<RecipeCollection>(this.minecraft.getSearchTree(SearchRegistry.RECIPE_COLLECTIONS).search(string.toLowerCase(Locale.ROOT)));
            list2.removeIf(recipeCollection -> !objectSet.contains(recipeCollection));
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
                continue;
            }
            if (!recipeBookTabButton.updateVisibility(this.book)) continue;
            recipeBookTabButton.setPosition(i, j + 27 * l++);
            recipeBookTabButton.startAnimation(this.minecraft);
        }
    }

    public void tick() {
        boolean bl = this.isVisibleAccordingToBookData();
        if (this.isVisible() != bl) {
            this.setVisible(bl);
        }
        if (!this.isVisible()) {
            return;
        }
        if (this.timesInventoryChanged != this.minecraft.player.getInventory().getTimesChanged()) {
            this.updateStackedContents();
            this.timesInventoryChanged = this.minecraft.player.getInventory().getTimesChanged();
        }
        this.searchBox.tick();
    }

    private void updateStackedContents() {
        this.stackedContents.clear();
        this.minecraft.player.getInventory().fillStackedContents(this.stackedContents);
        this.menu.fillCraftSlotsStackedContents(this.stackedContents);
        this.updateCollections(false);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        if (!this.isVisible()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.0f, 100.0f);
        RenderSystem.setShaderTexture(0, RECIPE_BOOK_LOCATION);
        int k = (this.width - 147) / 2 - this.xOffset;
        int l = (this.height - 166) / 2;
        RecipeBookComponent.blit(poseStack, k, l, 1, 1, 147, 166);
        this.searchBox.render(poseStack, i, j, f);
        for (RecipeBookTabButton recipeBookTabButton : this.tabButtons) {
            recipeBookTabButton.render(poseStack, i, j, f);
        }
        this.filterButton.render(poseStack, i, j, f);
        this.recipeBookPage.render(poseStack, k, l, i, j, f);
        poseStack.popPose();
    }

    public void renderTooltip(PoseStack poseStack, int i, int j, int k, int l) {
        if (!this.isVisible()) {
            return;
        }
        this.recipeBookPage.renderTooltip(poseStack, k, l);
        this.renderGhostRecipeTooltip(poseStack, i, j, k, l);
    }

    protected Component getRecipeFilterName() {
        return ONLY_CRAFTABLES_TOOLTIP;
    }

    private void renderGhostRecipeTooltip(PoseStack poseStack, int i, int j, int k, int l) {
        ItemStack itemStack = null;
        for (int m = 0; m < this.ghostRecipe.size(); ++m) {
            GhostRecipe.GhostIngredient ghostIngredient = this.ghostRecipe.get(m);
            int n = ghostIngredient.getX() + i;
            int o = ghostIngredient.getY() + j;
            if (k < n || l < o || k >= n + 16 || l >= o + 16) continue;
            itemStack = ghostIngredient.getItem();
        }
        if (itemStack != null && this.minecraft.screen != null) {
            this.minecraft.screen.renderComponentTooltip(poseStack, this.minecraft.screen.getTooltipFromItem(itemStack), k, l);
        }
    }

    public void renderGhostRecipe(PoseStack poseStack, int i, int j, boolean bl, float f) {
        this.ghostRecipe.render(poseStack, this.minecraft, i, j, bl, f);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if (!this.isVisible() || this.minecraft.player.isSpectator()) {
            return false;
        }
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
        }
        if (this.searchBox.mouseClicked(d, e, i)) {
            return true;
        }
        if (this.filterButton.mouseClicked(d, e, i)) {
            boolean bl = this.toggleFiltering();
            this.filterButton.setStateTriggered(bl);
            this.updateFilterButtonTooltip();
            this.sendUpdateSettings();
            this.updateCollections(false);
            return true;
        }
        for (RecipeBookTabButton recipeBookTabButton : this.tabButtons) {
            if (!recipeBookTabButton.mouseClicked(d, e, i)) continue;
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
        return false;
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
        }
        boolean bl = d < (double)i || e < (double)j || d >= (double)(i + k) || e >= (double)(j + l);
        boolean bl2 = (double)(i - 147) < d && d < (double)i && (double)j < e && e < (double)(j + l);
        return bl && !bl2 && !this.selectedTab.isHoveredOrFocused();
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        this.ignoreTextInput = false;
        if (!this.isVisible() || this.minecraft.player.isSpectator()) {
            return false;
        }
        if (i == 256 && !this.isOffsetNextToMainGUI()) {
            this.setVisible(false);
            return true;
        }
        if (this.searchBox.keyPressed(i, j, k)) {
            this.checkSearchStringUpdate();
            return true;
        }
        if (this.searchBox.isFocused() && this.searchBox.isVisible() && i != 256) {
            return true;
        }
        if (this.minecraft.options.keyChat.matches(i, j) && !this.searchBox.isFocused()) {
            this.ignoreTextInput = true;
            this.searchBox.setFocused(true);
            return true;
        }
        return false;
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
        }
        if (!this.isVisible() || this.minecraft.player.isSpectator()) {
            return false;
        }
        if (this.searchBox.charTyped(c, i)) {
            this.checkSearchStringUpdate();
            return true;
        }
        return GuiEventListener.super.charTyped(c, i);
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
    public void recipesShown(List<Recipe<?>> list) {
        for (Recipe<?> recipe : list) {
            this.minecraft.player.removeRecipeHighlight(recipe);
        }
    }

    public void setupGhostRecipe(Recipe<?> recipe, List<Slot> list) {
        ItemStack itemStack = recipe.getResultItem(this.minecraft.level.registryAccess());
        this.ghostRecipe.setRecipe(recipe);
        this.ghostRecipe.addIngredient(Ingredient.of(itemStack), list.get((int)0).x, list.get((int)0).y);
        this.placeRecipe(this.menu.getGridWidth(), this.menu.getGridHeight(), this.menu.getResultSlotIndex(), recipe, recipe.getIngredients().iterator(), 0);
    }

    @Override
    public void addItemToSlot(Iterator<Ingredient> iterator, int i, int j, int k, int l) {
        Ingredient ingredient = iterator.next();
        if (!ingredient.isEmpty()) {
            Slot slot = (Slot)this.menu.slots.get(i);
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
        ArrayList<AbstractWidget> list = Lists.newArrayList();
        this.recipeBookPage.listButtons(abstractWidget -> {
            if (abstractWidget.isActive()) {
                list.add((AbstractWidget)abstractWidget);
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

