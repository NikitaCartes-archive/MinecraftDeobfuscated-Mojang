/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.HotbarManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.CreativeInventoryListener;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.inventory.Hotbar;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class CreativeModeInventoryScreen
extends EffectRenderingInventoryScreen<ItemPickerMenu> {
    private static final ResourceLocation CREATIVE_TABS_LOCATION = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
    private static final String GUI_CREATIVE_TAB_PREFIX = "textures/gui/container/creative_inventory/tab_";
    private static final String CUSTOM_SLOT_LOCK = "CustomCreativeLock";
    private static final int NUM_ROWS = 5;
    private static final int NUM_COLS = 9;
    private static final int TAB_WIDTH = 26;
    private static final int TAB_HEIGHT = 32;
    private static final int SCROLLER_WIDTH = 12;
    private static final int SCROLLER_HEIGHT = 15;
    static final SimpleContainer CONTAINER = new SimpleContainer(45);
    private static final Component TRASH_SLOT_TOOLTIP = Component.translatable("inventory.binSlot");
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static CreativeModeTab selectedTab = CreativeModeTabs.getDefaultTab();
    private float scrollOffs;
    private boolean scrolling;
    private EditBox searchBox;
    @Nullable
    private List<Slot> originalSlots;
    @Nullable
    private Slot destroyItemSlot;
    private CreativeInventoryListener listener;
    private boolean ignoreTextInput;
    private boolean hasClickedOutside;
    private final Set<TagKey<Item>> visibleTags = new HashSet<TagKey<Item>>();
    private final boolean displayOperatorCreativeTab;

    public CreativeModeInventoryScreen(Player player, FeatureFlagSet featureFlagSet, boolean bl) {
        super(new ItemPickerMenu(player), player.getInventory(), CommonComponents.EMPTY);
        player.containerMenu = this.menu;
        this.passEvents = true;
        this.imageHeight = 136;
        this.imageWidth = 195;
        this.displayOperatorCreativeTab = bl;
        CreativeModeTabs.tryRebuildTabContents(featureFlagSet, this.hasPermissions(player));
    }

    private boolean hasPermissions(Player player) {
        return player.canUseGameMasterBlocks() && this.displayOperatorCreativeTab;
    }

    private void tryRefreshInvalidatedTabs(FeatureFlagSet featureFlagSet, boolean bl) {
        if (CreativeModeTabs.tryRebuildTabContents(featureFlagSet, bl)) {
            for (CreativeModeTab creativeModeTab : CreativeModeTabs.allTabs()) {
                ItemStackLinkedSet itemStackLinkedSet = creativeModeTab.getDisplayItems();
                if (creativeModeTab != selectedTab) continue;
                if (creativeModeTab.getType() == CreativeModeTab.Type.CATEGORY && itemStackLinkedSet.isEmpty()) {
                    this.selectTab(CreativeModeTabs.getDefaultTab());
                    continue;
                }
                this.refreshCurrentTabContents(itemStackLinkedSet);
            }
        }
    }

    private void refreshCurrentTabContents(ItemStackLinkedSet itemStackLinkedSet) {
        int i = ((ItemPickerMenu)this.menu).getRowIndexForScroll(this.scrollOffs);
        ((ItemPickerMenu)this.menu).items.clear();
        if (selectedTab.getType() == CreativeModeTab.Type.SEARCH) {
            this.refreshSearchResults();
        } else {
            ((ItemPickerMenu)this.menu).items.addAll(itemStackLinkedSet);
        }
        this.scrollOffs = ((ItemPickerMenu)this.menu).getScrollForRowIndex(i);
        ((ItemPickerMenu)this.menu).scrollTo(this.scrollOffs);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (this.minecraft == null) {
            return;
        }
        if (this.minecraft.player != null) {
            this.tryRefreshInvalidatedTabs(this.minecraft.player.connection.enabledFeatures(), this.hasPermissions(this.minecraft.player));
        }
        if (!this.minecraft.gameMode.hasInfiniteItems()) {
            this.minecraft.setScreen(new InventoryScreen(this.minecraft.player));
        } else {
            this.searchBox.tick();
        }
    }

    @Override
    protected void slotClicked(@Nullable Slot slot, int i, int j, ClickType clickType) {
        if (this.isCreativeSlot(slot)) {
            this.searchBox.moveCursorToEnd();
            this.searchBox.setHighlightPos(0);
        }
        boolean bl = clickType == ClickType.QUICK_MOVE;
        ClickType clickType2 = clickType = i == -999 && clickType == ClickType.PICKUP ? ClickType.THROW : clickType;
        if (slot != null || selectedTab.getType() == CreativeModeTab.Type.INVENTORY || clickType == ClickType.QUICK_CRAFT) {
            if (slot != null && !slot.mayPickup(this.minecraft.player)) {
                return;
            }
            if (slot == this.destroyItemSlot && bl) {
                for (int k = 0; k < this.minecraft.player.inventoryMenu.getItems().size(); ++k) {
                    this.minecraft.gameMode.handleCreativeModeItemAdd(ItemStack.EMPTY, k);
                }
            } else if (selectedTab.getType() == CreativeModeTab.Type.INVENTORY) {
                if (slot == this.destroyItemSlot) {
                    ((ItemPickerMenu)this.menu).setCarried(ItemStack.EMPTY);
                } else if (clickType == ClickType.THROW && slot != null && slot.hasItem()) {
                    ItemStack itemStack = slot.remove(j == 0 ? 1 : slot.getItem().getMaxStackSize());
                    ItemStack itemStack2 = slot.getItem();
                    this.minecraft.player.drop(itemStack, true);
                    this.minecraft.gameMode.handleCreativeModeItemDrop(itemStack);
                    this.minecraft.gameMode.handleCreativeModeItemAdd(itemStack2, ((SlotWrapper)slot).target.index);
                } else if (clickType == ClickType.THROW && !((ItemPickerMenu)this.menu).getCarried().isEmpty()) {
                    this.minecraft.player.drop(((ItemPickerMenu)this.menu).getCarried(), true);
                    this.minecraft.gameMode.handleCreativeModeItemDrop(((ItemPickerMenu)this.menu).getCarried());
                    ((ItemPickerMenu)this.menu).setCarried(ItemStack.EMPTY);
                } else {
                    this.minecraft.player.inventoryMenu.clicked(slot == null ? i : ((SlotWrapper)slot).target.index, j, clickType, this.minecraft.player);
                    this.minecraft.player.inventoryMenu.broadcastChanges();
                }
            } else if (clickType != ClickType.QUICK_CRAFT && slot.container == CONTAINER) {
                ItemStack itemStack = ((ItemPickerMenu)this.menu).getCarried();
                ItemStack itemStack2 = slot.getItem();
                if (clickType == ClickType.SWAP) {
                    if (!itemStack2.isEmpty()) {
                        ItemStack itemStack3 = itemStack2.copy();
                        itemStack3.setCount(itemStack3.getMaxStackSize());
                        this.minecraft.player.getInventory().setItem(j, itemStack3);
                        this.minecraft.player.inventoryMenu.broadcastChanges();
                    }
                    return;
                }
                if (clickType == ClickType.CLONE) {
                    if (((ItemPickerMenu)this.menu).getCarried().isEmpty() && slot.hasItem()) {
                        ItemStack itemStack3 = slot.getItem().copy();
                        itemStack3.setCount(itemStack3.getMaxStackSize());
                        ((ItemPickerMenu)this.menu).setCarried(itemStack3);
                    }
                    return;
                }
                if (clickType == ClickType.THROW) {
                    if (!itemStack2.isEmpty()) {
                        ItemStack itemStack3 = itemStack2.copy();
                        itemStack3.setCount(j == 0 ? 1 : itemStack3.getMaxStackSize());
                        this.minecraft.player.drop(itemStack3, true);
                        this.minecraft.gameMode.handleCreativeModeItemDrop(itemStack3);
                    }
                    return;
                }
                if (!itemStack.isEmpty() && !itemStack2.isEmpty() && itemStack.sameItem(itemStack2) && ItemStack.tagMatches(itemStack, itemStack2)) {
                    if (j == 0) {
                        if (bl) {
                            itemStack.setCount(itemStack.getMaxStackSize());
                        } else if (itemStack.getCount() < itemStack.getMaxStackSize()) {
                            itemStack.grow(1);
                        }
                    } else {
                        itemStack.shrink(1);
                    }
                } else if (itemStack2.isEmpty() || !itemStack.isEmpty()) {
                    if (j == 0) {
                        ((ItemPickerMenu)this.menu).setCarried(ItemStack.EMPTY);
                    } else {
                        ((ItemPickerMenu)this.menu).getCarried().shrink(1);
                    }
                } else {
                    ((ItemPickerMenu)this.menu).setCarried(itemStack2.copy());
                    itemStack = ((ItemPickerMenu)this.menu).getCarried();
                    if (bl) {
                        itemStack.setCount(itemStack.getMaxStackSize());
                    }
                }
            } else if (this.menu != null) {
                ItemStack itemStack = slot == null ? ItemStack.EMPTY : ((ItemPickerMenu)this.menu).getSlot(slot.index).getItem();
                ((ItemPickerMenu)this.menu).clicked(slot == null ? i : slot.index, j, clickType, this.minecraft.player);
                if (AbstractContainerMenu.getQuickcraftHeader(j) == 2) {
                    for (int l = 0; l < 9; ++l) {
                        this.minecraft.gameMode.handleCreativeModeItemAdd(((ItemPickerMenu)this.menu).getSlot(45 + l).getItem(), 36 + l);
                    }
                } else if (slot != null) {
                    ItemStack itemStack2 = ((ItemPickerMenu)this.menu).getSlot(slot.index).getItem();
                    this.minecraft.gameMode.handleCreativeModeItemAdd(itemStack2, slot.index - ((ItemPickerMenu)this.menu).slots.size() + 9 + 36);
                    int m = 45 + j;
                    if (clickType == ClickType.SWAP) {
                        this.minecraft.gameMode.handleCreativeModeItemAdd(itemStack, m - ((ItemPickerMenu)this.menu).slots.size() + 9 + 36);
                    } else if (clickType == ClickType.THROW && !itemStack.isEmpty()) {
                        ItemStack itemStack4 = itemStack.copy();
                        itemStack4.setCount(j == 0 ? 1 : itemStack4.getMaxStackSize());
                        this.minecraft.player.drop(itemStack4, true);
                        this.minecraft.gameMode.handleCreativeModeItemDrop(itemStack4);
                    }
                    this.minecraft.player.inventoryMenu.broadcastChanges();
                }
            }
        } else if (!((ItemPickerMenu)this.menu).getCarried().isEmpty() && this.hasClickedOutside) {
            if (j == 0) {
                this.minecraft.player.drop(((ItemPickerMenu)this.menu).getCarried(), true);
                this.minecraft.gameMode.handleCreativeModeItemDrop(((ItemPickerMenu)this.menu).getCarried());
                ((ItemPickerMenu)this.menu).setCarried(ItemStack.EMPTY);
            }
            if (j == 1) {
                ItemStack itemStack = ((ItemPickerMenu)this.menu).getCarried().split(1);
                this.minecraft.player.drop(itemStack, true);
                this.minecraft.gameMode.handleCreativeModeItemDrop(itemStack);
            }
        }
    }

    private boolean isCreativeSlot(@Nullable Slot slot) {
        return slot != null && slot.container == CONTAINER;
    }

    @Override
    protected void init() {
        if (this.minecraft.gameMode.hasInfiniteItems()) {
            super.init();
            this.searchBox = new EditBox(this.font, this.leftPos + 82, this.topPos + 6, 80, this.font.lineHeight, Component.translatable("itemGroup.search"));
            this.searchBox.setMaxLength(50);
            this.searchBox.setBordered(false);
            this.searchBox.setVisible(false);
            this.searchBox.setTextColor(0xFFFFFF);
            this.addWidget(this.searchBox);
            CreativeModeTab creativeModeTab = selectedTab;
            selectedTab = CreativeModeTabs.getDefaultTab();
            this.selectTab(creativeModeTab);
            this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
            this.listener = new CreativeInventoryListener(this.minecraft);
            this.minecraft.player.inventoryMenu.addSlotListener(this.listener);
            if (!selectedTab.shouldDisplay()) {
                this.selectTab(CreativeModeTabs.getDefaultTab());
            }
        } else {
            this.minecraft.setScreen(new InventoryScreen(this.minecraft.player));
        }
    }

    @Override
    public void resize(Minecraft minecraft, int i, int j) {
        int k = ((ItemPickerMenu)this.menu).getRowIndexForScroll(this.scrollOffs);
        String string = this.searchBox.getValue();
        this.init(minecraft, i, j);
        this.searchBox.setValue(string);
        if (!this.searchBox.getValue().isEmpty()) {
            this.refreshSearchResults();
        }
        this.scrollOffs = ((ItemPickerMenu)this.menu).getScrollForRowIndex(k);
        ((ItemPickerMenu)this.menu).scrollTo(this.scrollOffs);
    }

    @Override
    public void removed() {
        super.removed();
        if (this.minecraft.player != null && this.minecraft.player.getInventory() != null) {
            this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
        }
    }

    @Override
    public boolean charTyped(char c, int i) {
        if (this.ignoreTextInput) {
            return false;
        }
        if (selectedTab.getType() != CreativeModeTab.Type.SEARCH) {
            return false;
        }
        String string = this.searchBox.getValue();
        if (this.searchBox.charTyped(c, i)) {
            if (!Objects.equals(string, this.searchBox.getValue())) {
                this.refreshSearchResults();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        this.ignoreTextInput = false;
        if (selectedTab.getType() != CreativeModeTab.Type.SEARCH) {
            if (this.minecraft.options.keyChat.matches(i, j)) {
                this.ignoreTextInput = true;
                this.selectTab(CreativeModeTabs.searchTab());
                return true;
            }
            return super.keyPressed(i, j, k);
        }
        boolean bl = !this.isCreativeSlot(this.hoveredSlot) || this.hoveredSlot.hasItem();
        boolean bl2 = InputConstants.getKey(i, j).getNumericKeyValue().isPresent();
        if (bl && bl2 && this.checkHotbarKeyPressed(i, j)) {
            this.ignoreTextInput = true;
            return true;
        }
        String string = this.searchBox.getValue();
        if (this.searchBox.keyPressed(i, j, k)) {
            if (!Objects.equals(string, this.searchBox.getValue())) {
                this.refreshSearchResults();
            }
            return true;
        }
        if (this.searchBox.isFocused() && this.searchBox.isVisible() && i != 256) {
            return true;
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    public boolean keyReleased(int i, int j, int k) {
        this.ignoreTextInput = false;
        return super.keyReleased(i, j, k);
    }

    private void refreshSearchResults() {
        ((ItemPickerMenu)this.menu).items.clear();
        this.visibleTags.clear();
        String string = this.searchBox.getValue();
        if (string.isEmpty()) {
            ((ItemPickerMenu)this.menu).items.addAll(selectedTab.getDisplayItems());
        } else {
            SearchTree<ItemStack> searchTree;
            if (string.startsWith("#")) {
                string = string.substring(1);
                searchTree = this.minecraft.getSearchTree(SearchRegistry.CREATIVE_TAGS);
                this.updateVisibleTags(string);
            } else {
                searchTree = this.minecraft.getSearchTree(SearchRegistry.CREATIVE_NAMES);
            }
            ((ItemPickerMenu)this.menu).items.addAll(searchTree.search(string.toLowerCase(Locale.ROOT)));
        }
        this.scrollOffs = 0.0f;
        ((ItemPickerMenu)this.menu).scrollTo(0.0f);
    }

    private void updateVisibleTags(String string) {
        Predicate<ResourceLocation> predicate;
        int i = string.indexOf(58);
        if (i == -1) {
            predicate = resourceLocation -> resourceLocation.getPath().contains(string);
        } else {
            String string2 = string.substring(0, i).trim();
            String string3 = string.substring(i + 1).trim();
            predicate = resourceLocation -> resourceLocation.getNamespace().contains(string2) && resourceLocation.getPath().contains(string3);
        }
        BuiltInRegistries.ITEM.getTagNames().filter(tagKey -> predicate.test(tagKey.location())).forEach(this.visibleTags::add);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int i, int j) {
        if (selectedTab.showTitle()) {
            RenderSystem.disableBlend();
            this.font.draw(poseStack, selectedTab.getDisplayName(), 8.0f, 6.0f, 0x404040);
        }
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if (i == 0) {
            double f = d - (double)this.leftPos;
            double g = e - (double)this.topPos;
            for (CreativeModeTab creativeModeTab : CreativeModeTabs.tabs()) {
                if (!this.checkTabClicked(creativeModeTab, f, g)) continue;
                return true;
            }
            if (selectedTab.getType() != CreativeModeTab.Type.INVENTORY && this.insideScrollbar(d, e)) {
                this.scrolling = this.canScroll();
                return true;
            }
        }
        return super.mouseClicked(d, e, i);
    }

    @Override
    public boolean mouseReleased(double d, double e, int i) {
        if (i == 0) {
            double f = d - (double)this.leftPos;
            double g = e - (double)this.topPos;
            this.scrolling = false;
            for (CreativeModeTab creativeModeTab : CreativeModeTabs.tabs()) {
                if (!this.checkTabClicked(creativeModeTab, f, g)) continue;
                this.selectTab(creativeModeTab);
                return true;
            }
        }
        return super.mouseReleased(d, e, i);
    }

    private boolean canScroll() {
        return selectedTab.canScroll() && ((ItemPickerMenu)this.menu).canScroll();
    }

    private void selectTab(CreativeModeTab creativeModeTab) {
        int j;
        int i;
        CreativeModeTab creativeModeTab2 = selectedTab;
        selectedTab = creativeModeTab;
        this.quickCraftSlots.clear();
        ((ItemPickerMenu)this.menu).items.clear();
        this.clearDraggingState();
        if (selectedTab.getType() == CreativeModeTab.Type.HOTBAR) {
            HotbarManager hotbarManager = this.minecraft.getHotbarManager();
            for (i = 0; i < 9; ++i) {
                Hotbar hotbar = hotbarManager.get(i);
                if (hotbar.isEmpty()) {
                    for (j = 0; j < 9; ++j) {
                        if (j == i) {
                            ItemStack itemStack = new ItemStack(Items.PAPER);
                            itemStack.getOrCreateTagElement(CUSTOM_SLOT_LOCK);
                            Component component = this.minecraft.options.keyHotbarSlots[i].getTranslatedKeyMessage();
                            Component component2 = this.minecraft.options.keySaveHotbarActivator.getTranslatedKeyMessage();
                            itemStack.setHoverName(Component.translatable("inventory.hotbarInfo", component2, component));
                            ((ItemPickerMenu)this.menu).items.add(itemStack);
                            continue;
                        }
                        ((ItemPickerMenu)this.menu).items.add(ItemStack.EMPTY);
                    }
                    continue;
                }
                ((ItemPickerMenu)this.menu).items.addAll(hotbar);
            }
        } else if (selectedTab.getType() == CreativeModeTab.Type.CATEGORY) {
            ((ItemPickerMenu)this.menu).items.addAll(selectedTab.getDisplayItems());
        }
        if (selectedTab.getType() == CreativeModeTab.Type.INVENTORY) {
            InventoryMenu abstractContainerMenu = this.minecraft.player.inventoryMenu;
            if (this.originalSlots == null) {
                this.originalSlots = ImmutableList.copyOf(((ItemPickerMenu)this.menu).slots);
            }
            ((ItemPickerMenu)this.menu).slots.clear();
            for (i = 0; i < abstractContainerMenu.slots.size(); ++i) {
                int n;
                if (i >= 5 && i < 9) {
                    int k = i - 5;
                    l = k / 2;
                    m = k % 2;
                    n = 54 + l * 54;
                    j = 6 + m * 27;
                } else if (i >= 0 && i < 5) {
                    n = -2000;
                    j = -2000;
                } else if (i == 45) {
                    n = 35;
                    j = 20;
                } else {
                    int k = i - 9;
                    l = k % 9;
                    m = k / 9;
                    n = 9 + l * 18;
                    j = i >= 36 ? 112 : 54 + m * 18;
                }
                SlotWrapper slot = new SlotWrapper(abstractContainerMenu.slots.get(i), i, n, j);
                ((ItemPickerMenu)this.menu).slots.add(slot);
            }
            this.destroyItemSlot = new Slot(CONTAINER, 0, 173, 112);
            ((ItemPickerMenu)this.menu).slots.add(this.destroyItemSlot);
        } else if (creativeModeTab2.getType() == CreativeModeTab.Type.INVENTORY) {
            ((ItemPickerMenu)this.menu).slots.clear();
            ((ItemPickerMenu)this.menu).slots.addAll(this.originalSlots);
            this.originalSlots = null;
        }
        if (selectedTab.getType() == CreativeModeTab.Type.SEARCH) {
            this.searchBox.setVisible(true);
            this.searchBox.setCanLoseFocus(false);
            this.searchBox.setFocus(true);
            if (creativeModeTab2 != creativeModeTab) {
                this.searchBox.setValue("");
            }
            this.refreshSearchResults();
        } else {
            this.searchBox.setVisible(false);
            this.searchBox.setCanLoseFocus(true);
            this.searchBox.setFocus(false);
            this.searchBox.setValue("");
        }
        this.scrollOffs = 0.0f;
        ((ItemPickerMenu)this.menu).scrollTo(0.0f);
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f) {
        if (!this.canScroll()) {
            return false;
        }
        this.scrollOffs = ((ItemPickerMenu)this.menu).subtractInputFromScroll(this.scrollOffs, f);
        ((ItemPickerMenu)this.menu).scrollTo(this.scrollOffs);
        return true;
    }

    @Override
    protected boolean hasClickedOutside(double d, double e, int i, int j, int k) {
        boolean bl = d < (double)i || e < (double)j || d >= (double)(i + this.imageWidth) || e >= (double)(j + this.imageHeight);
        this.hasClickedOutside = bl && !this.checkTabClicked(selectedTab, d, e);
        return this.hasClickedOutside;
    }

    protected boolean insideScrollbar(double d, double e) {
        int i = this.leftPos;
        int j = this.topPos;
        int k = i + 175;
        int l = j + 18;
        int m = k + 14;
        int n = l + 112;
        return d >= (double)k && e >= (double)l && d < (double)m && e < (double)n;
    }

    @Override
    public boolean mouseDragged(double d, double e, int i, double f, double g) {
        if (this.scrolling) {
            int j = this.topPos + 18;
            int k = j + 112;
            this.scrollOffs = ((float)e - (float)j - 7.5f) / ((float)(k - j) - 15.0f);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0f, 1.0f);
            ((ItemPickerMenu)this.menu).scrollTo(this.scrollOffs);
            return true;
        }
        return super.mouseDragged(d, e, i, f, g);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        super.render(poseStack, i, j, f);
        for (CreativeModeTab creativeModeTab : CreativeModeTabs.tabs()) {
            if (this.checkTabHovering(poseStack, creativeModeTab, i, j)) break;
        }
        if (this.destroyItemSlot != null && selectedTab.getType() == CreativeModeTab.Type.INVENTORY && this.isHovering(this.destroyItemSlot.x, this.destroyItemSlot.y, 16, 16, i, j)) {
            this.renderTooltip(poseStack, TRASH_SLOT_TOOLTIP, i, j);
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.renderTooltip(poseStack, i, j);
    }

    @Override
    protected void renderTooltip(PoseStack poseStack, ItemStack itemStack, int i, int j) {
        List<Component> list2;
        boolean bl = this.hoveredSlot != null && this.hoveredSlot instanceof CustomCreativeSlot;
        boolean bl2 = selectedTab.getType() == CreativeModeTab.Type.CATEGORY;
        boolean bl3 = selectedTab.getType() == CreativeModeTab.Type.SEARCH;
        TooltipFlag.Default default_ = this.minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
        TooltipFlag.Default tooltipFlag = bl ? default_.asCreative() : default_;
        List<Component> list = itemStack.getTooltipLines(this.minecraft.player, tooltipFlag);
        if (!bl2 || !bl) {
            list2 = Lists.newArrayList(list);
            if (bl3 && bl) {
                this.visibleTags.forEach(tagKey -> {
                    if (itemStack.is((TagKey<Item>)tagKey)) {
                        list2.add(1, Component.literal("#" + tagKey.location()).withStyle(ChatFormatting.DARK_PURPLE));
                    }
                });
            }
            int k = 1;
            for (CreativeModeTab creativeModeTab : CreativeModeTabs.tabs()) {
                if (creativeModeTab.getType() == CreativeModeTab.Type.SEARCH || !creativeModeTab.contains(itemStack)) continue;
                list2.add(k++, creativeModeTab.getDisplayName().copy().withStyle(ChatFormatting.BLUE));
            }
        } else {
            list2 = list;
        }
        this.renderTooltip(poseStack, list2, itemStack.getTooltipImage(), i, j);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float f, int i, int j) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        for (CreativeModeTab creativeModeTab : CreativeModeTabs.tabs()) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, CREATIVE_TABS_LOCATION);
            if (creativeModeTab == selectedTab) continue;
            this.renderTabButton(poseStack, creativeModeTab);
        }
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, new ResourceLocation(GUI_CREATIVE_TAB_PREFIX + selectedTab.getBackgroundSuffix()));
        this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        this.searchBox.render(poseStack, i, j, f);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        int k = this.leftPos + 175;
        int l = this.topPos + 18;
        int m = l + 112;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, CREATIVE_TABS_LOCATION);
        if (selectedTab.canScroll()) {
            this.blit(poseStack, k, l + (int)((float)(m - l - 17) * this.scrollOffs), 232 + (this.canScroll() ? 0 : 12), 0, 12, 15);
        }
        this.renderTabButton(poseStack, selectedTab);
        if (selectedTab.getType() == CreativeModeTab.Type.INVENTORY) {
            InventoryScreen.renderEntityInInventory(this.leftPos + 88, this.topPos + 45, 20, this.leftPos + 88 - i, this.topPos + 45 - 30 - j, this.minecraft.player);
        }
    }

    private int getTabX(CreativeModeTab creativeModeTab) {
        int i = creativeModeTab.column();
        int j = 27;
        int k = 27 * i;
        if (creativeModeTab.isAlignedRight()) {
            k = this.imageWidth - 27 * (7 - i);
        }
        return k;
    }

    private int getTabY(CreativeModeTab creativeModeTab) {
        int i = 0;
        i = creativeModeTab.row() == CreativeModeTab.Row.TOP ? (i -= 32) : (i += this.imageHeight);
        return i;
    }

    protected boolean checkTabClicked(CreativeModeTab creativeModeTab, double d, double e) {
        int i = this.getTabX(creativeModeTab);
        int j = this.getTabY(creativeModeTab);
        return d >= (double)i && d <= (double)(i + 26) && e >= (double)j && e <= (double)(j + 32);
    }

    protected boolean checkTabHovering(PoseStack poseStack, CreativeModeTab creativeModeTab, int i, int j) {
        int l;
        int k = this.getTabX(creativeModeTab);
        if (this.isHovering(k + 3, (l = this.getTabY(creativeModeTab)) + 3, 21, 27, i, j)) {
            this.renderTooltip(poseStack, creativeModeTab.getDisplayName(), i, j);
            return true;
        }
        return false;
    }

    protected void renderTabButton(PoseStack poseStack, CreativeModeTab creativeModeTab) {
        boolean bl = creativeModeTab == selectedTab;
        boolean bl2 = creativeModeTab.row() == CreativeModeTab.Row.TOP;
        int i = creativeModeTab.column();
        int j = i * 26;
        int k = 0;
        int l = this.leftPos + this.getTabX(creativeModeTab);
        int m = this.topPos;
        int n = 32;
        if (bl) {
            k += 32;
        }
        if (bl2) {
            m -= 28;
        } else {
            k += 64;
            m += this.imageHeight - 4;
        }
        this.blit(poseStack, l, m, j, k, 26, 32);
        this.itemRenderer.blitOffset = 100.0f;
        int n2 = bl2 ? 1 : -1;
        ItemStack itemStack = creativeModeTab.getIconItem();
        this.itemRenderer.renderAndDecorateItem(itemStack, l += 5, m += 8 + n2);
        this.itemRenderer.renderGuiItemDecorations(this.font, itemStack, l, m);
        this.itemRenderer.blitOffset = 0.0f;
    }

    public boolean isInventoryOpen() {
        return selectedTab.getType() == CreativeModeTab.Type.INVENTORY;
    }

    public static void handleHotbarLoadOrSave(Minecraft minecraft, int i, boolean bl, boolean bl2) {
        LocalPlayer localPlayer = minecraft.player;
        HotbarManager hotbarManager = minecraft.getHotbarManager();
        Hotbar hotbar = hotbarManager.get(i);
        if (bl) {
            for (int j = 0; j < Inventory.getSelectionSize(); ++j) {
                ItemStack itemStack = (ItemStack)hotbar.get(j);
                ItemStack itemStack2 = itemStack.isItemEnabled(localPlayer.level.enabledFeatures()) ? itemStack.copy() : ItemStack.EMPTY;
                localPlayer.getInventory().setItem(j, itemStack2);
                minecraft.gameMode.handleCreativeModeItemAdd(itemStack2, 36 + j);
            }
            localPlayer.inventoryMenu.broadcastChanges();
        } else if (bl2) {
            for (int j = 0; j < Inventory.getSelectionSize(); ++j) {
                hotbar.set(j, localPlayer.getInventory().getItem(j).copy());
            }
            Component component = minecraft.options.keyHotbarSlots[i].getTranslatedKeyMessage();
            Component component2 = minecraft.options.keyLoadHotbarActivator.getTranslatedKeyMessage();
            MutableComponent component3 = Component.translatable("inventory.hotbarSaved", component2, component);
            minecraft.gui.setOverlayMessage(component3, false);
            minecraft.getNarrator().sayNow(component3);
            hotbarManager.save();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class ItemPickerMenu
    extends AbstractContainerMenu {
        public final NonNullList<ItemStack> items = NonNullList.create();
        private final AbstractContainerMenu inventoryMenu;

        public ItemPickerMenu(Player player) {
            super(null, 0);
            int i;
            this.inventoryMenu = player.inventoryMenu;
            Inventory inventory = player.getInventory();
            for (i = 0; i < 5; ++i) {
                for (int j = 0; j < 9; ++j) {
                    this.addSlot(new CustomCreativeSlot(CONTAINER, i * 9 + j, 9 + j * 18, 18 + i * 18));
                }
            }
            for (i = 0; i < 9; ++i) {
                this.addSlot(new Slot(inventory, i, 9 + i * 18, 112));
            }
            this.scrollTo(0.0f);
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        protected int calculateRowCount() {
            return Mth.positiveCeilDiv(this.items.size(), 9) - 5;
        }

        protected int getRowIndexForScroll(float f) {
            return Math.max((int)((double)(f * (float)this.calculateRowCount()) + 0.5), 0);
        }

        protected float getScrollForRowIndex(int i) {
            return Mth.clamp((float)i / (float)this.calculateRowCount(), 0.0f, 1.0f);
        }

        protected float subtractInputFromScroll(float f, double d) {
            return Mth.clamp(f - (float)(d / (double)this.calculateRowCount()), 0.0f, 1.0f);
        }

        public void scrollTo(float f) {
            int i = this.getRowIndexForScroll(f);
            for (int j = 0; j < 5; ++j) {
                for (int k = 0; k < 9; ++k) {
                    int l = k + (j + i) * 9;
                    if (l >= 0 && l < this.items.size()) {
                        CONTAINER.setItem(k + j * 9, this.items.get(l));
                        continue;
                    }
                    CONTAINER.setItem(k + j * 9, ItemStack.EMPTY);
                }
            }
        }

        public boolean canScroll() {
            return this.items.size() > 45;
        }

        @Override
        public ItemStack quickMoveStack(Player player, int i) {
            Slot slot;
            if (i >= this.slots.size() - 9 && i < this.slots.size() && (slot = (Slot)this.slots.get(i)) != null && slot.hasItem()) {
                slot.set(ItemStack.EMPTY);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
            return slot.container != CONTAINER;
        }

        @Override
        public boolean canDragTo(Slot slot) {
            return slot.container != CONTAINER;
        }

        @Override
        public ItemStack getCarried() {
            return this.inventoryMenu.getCarried();
        }

        @Override
        public void setCarried(ItemStack itemStack) {
            this.inventoryMenu.setCarried(itemStack);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class SlotWrapper
    extends Slot {
        final Slot target;

        public SlotWrapper(Slot slot, int i, int j, int k) {
            super(slot.container, i, j, k);
            this.target = slot;
        }

        @Override
        public void onTake(Player player, ItemStack itemStack) {
            this.target.onTake(player, itemStack);
        }

        @Override
        public boolean mayPlace(ItemStack itemStack) {
            return this.target.mayPlace(itemStack);
        }

        @Override
        public ItemStack getItem() {
            return this.target.getItem();
        }

        @Override
        public boolean hasItem() {
            return this.target.hasItem();
        }

        @Override
        public void set(ItemStack itemStack) {
            this.target.set(itemStack);
        }

        @Override
        public void setChanged() {
            this.target.setChanged();
        }

        @Override
        public int getMaxStackSize() {
            return this.target.getMaxStackSize();
        }

        @Override
        public int getMaxStackSize(ItemStack itemStack) {
            return this.target.getMaxStackSize(itemStack);
        }

        @Override
        @Nullable
        public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
            return this.target.getNoItemIcon();
        }

        @Override
        public ItemStack remove(int i) {
            return this.target.remove(i);
        }

        @Override
        public boolean isActive() {
            return this.target.isActive();
        }

        @Override
        public boolean mayPickup(Player player) {
            return this.target.mayPickup(player);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class CustomCreativeSlot
    extends Slot {
        public CustomCreativeSlot(Container container, int i, int j, int k) {
            super(container, i, j, k);
        }

        @Override
        public boolean mayPickup(Player player) {
            ItemStack itemStack = this.getItem();
            if (super.mayPickup(player) && !itemStack.isEmpty()) {
                return itemStack.isItemEnabled(player.level.enabledFeatures()) && itemStack.getTagElement(CreativeModeInventoryScreen.CUSTOM_SLOT_LOCK) == null;
            }
            return itemStack.isEmpty();
        }
    }
}

