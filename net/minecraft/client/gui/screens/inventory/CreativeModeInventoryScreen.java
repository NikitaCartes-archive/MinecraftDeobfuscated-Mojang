/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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
import net.minecraft.client.searchtree.MutableSearchTree;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class CreativeModeInventoryScreen
extends EffectRenderingInventoryScreen<ItemPickerMenu> {
    private static final ResourceLocation CREATIVE_TABS_LOCATION = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
    private static final SimpleContainer CONTAINER = new SimpleContainer(45);
    private static final Component TRASH_SLOT_TOOLTIP = new TranslatableComponent("inventory.binSlot");
    private static int selectedTab = CreativeModeTab.TAB_BUILDING_BLOCKS.getId();
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
    private final Map<ResourceLocation, Tag<Item>> visibleTags = Maps.newTreeMap();

    public CreativeModeInventoryScreen(Player player) {
        super(new ItemPickerMenu(player), player.inventory, TextComponent.EMPTY);
        player.containerMenu = this.menu;
        this.passEvents = true;
        this.imageHeight = 136;
        this.imageWidth = 195;
    }

    @Override
    public void tick() {
        if (!this.minecraft.gameMode.hasInfiniteItems()) {
            this.minecraft.setScreen(new InventoryScreen(this.minecraft.player));
        } else if (this.searchBox != null) {
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
        if (slot != null || selectedTab == CreativeModeTab.TAB_INVENTORY.getId() || clickType == ClickType.QUICK_CRAFT) {
            if (slot != null && !slot.mayPickup(this.minecraft.player)) {
                return;
            }
            if (slot == this.destroyItemSlot && bl) {
                for (int k = 0; k < this.minecraft.player.inventoryMenu.getItems().size(); ++k) {
                    this.minecraft.gameMode.handleCreativeModeItemAdd(ItemStack.EMPTY, k);
                }
            } else if (selectedTab == CreativeModeTab.TAB_INVENTORY.getId()) {
                if (slot == this.destroyItemSlot) {
                    this.minecraft.player.inventory.setCarried(ItemStack.EMPTY);
                } else if (clickType == ClickType.THROW && slot != null && slot.hasItem()) {
                    ItemStack itemStack = slot.remove(j == 0 ? 1 : slot.getItem().getMaxStackSize());
                    ItemStack itemStack2 = slot.getItem();
                    this.minecraft.player.drop(itemStack, true);
                    this.minecraft.gameMode.handleCreativeModeItemDrop(itemStack);
                    this.minecraft.gameMode.handleCreativeModeItemAdd(itemStack2, ((SlotWrapper)((SlotWrapper)slot)).target.index);
                } else if (clickType == ClickType.THROW && !this.minecraft.player.inventory.getCarried().isEmpty()) {
                    this.minecraft.player.drop(this.minecraft.player.inventory.getCarried(), true);
                    this.minecraft.gameMode.handleCreativeModeItemDrop(this.minecraft.player.inventory.getCarried());
                    this.minecraft.player.inventory.setCarried(ItemStack.EMPTY);
                } else {
                    this.minecraft.player.inventoryMenu.clicked(slot == null ? i : ((SlotWrapper)((SlotWrapper)slot)).target.index, j, clickType, this.minecraft.player);
                    this.minecraft.player.inventoryMenu.broadcastChanges();
                }
            } else if (clickType != ClickType.QUICK_CRAFT && slot.container == CONTAINER) {
                Inventory inventory = this.minecraft.player.inventory;
                ItemStack itemStack2 = inventory.getCarried();
                ItemStack itemStack3 = slot.getItem();
                if (clickType == ClickType.SWAP) {
                    if (!itemStack3.isEmpty()) {
                        ItemStack itemStack4 = itemStack3.copy();
                        itemStack4.setCount(itemStack4.getMaxStackSize());
                        this.minecraft.player.inventory.setItem(j, itemStack4);
                        this.minecraft.player.inventoryMenu.broadcastChanges();
                    }
                    return;
                }
                if (clickType == ClickType.CLONE) {
                    if (inventory.getCarried().isEmpty() && slot.hasItem()) {
                        ItemStack itemStack4 = slot.getItem().copy();
                        itemStack4.setCount(itemStack4.getMaxStackSize());
                        inventory.setCarried(itemStack4);
                    }
                    return;
                }
                if (clickType == ClickType.THROW) {
                    if (!itemStack3.isEmpty()) {
                        ItemStack itemStack4 = itemStack3.copy();
                        itemStack4.setCount(j == 0 ? 1 : itemStack4.getMaxStackSize());
                        this.minecraft.player.drop(itemStack4, true);
                        this.minecraft.gameMode.handleCreativeModeItemDrop(itemStack4);
                    }
                    return;
                }
                if (!itemStack2.isEmpty() && !itemStack3.isEmpty() && itemStack2.sameItem(itemStack3) && ItemStack.tagMatches(itemStack2, itemStack3)) {
                    if (j == 0) {
                        if (bl) {
                            itemStack2.setCount(itemStack2.getMaxStackSize());
                        } else if (itemStack2.getCount() < itemStack2.getMaxStackSize()) {
                            itemStack2.grow(1);
                        }
                    } else {
                        itemStack2.shrink(1);
                    }
                } else if (itemStack3.isEmpty() || !itemStack2.isEmpty()) {
                    if (j == 0) {
                        inventory.setCarried(ItemStack.EMPTY);
                    } else {
                        inventory.getCarried().shrink(1);
                    }
                } else {
                    inventory.setCarried(itemStack3.copy());
                    itemStack2 = inventory.getCarried();
                    if (bl) {
                        itemStack2.setCount(itemStack2.getMaxStackSize());
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
        } else {
            Inventory inventory = this.minecraft.player.inventory;
            if (!inventory.getCarried().isEmpty() && this.hasClickedOutside) {
                if (j == 0) {
                    this.minecraft.player.drop(inventory.getCarried(), true);
                    this.minecraft.gameMode.handleCreativeModeItemDrop(inventory.getCarried());
                    inventory.setCarried(ItemStack.EMPTY);
                }
                if (j == 1) {
                    ItemStack itemStack2 = inventory.getCarried().split(1);
                    this.minecraft.player.drop(itemStack2, true);
                    this.minecraft.gameMode.handleCreativeModeItemDrop(itemStack2);
                }
            }
        }
    }

    private boolean isCreativeSlot(@Nullable Slot slot) {
        return slot != null && slot.container == CONTAINER;
    }

    @Override
    protected void checkEffectRendering() {
        int i = this.leftPos;
        super.checkEffectRendering();
        if (this.searchBox != null && this.leftPos != i) {
            this.searchBox.setX(this.leftPos + 82);
        }
    }

    @Override
    protected void init() {
        if (this.minecraft.gameMode.hasInfiniteItems()) {
            super.init();
            this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
            this.searchBox = new EditBox(this.font, this.leftPos + 82, this.topPos + 6, 80, this.font.lineHeight, new TranslatableComponent("itemGroup.search"));
            this.searchBox.setMaxLength(50);
            this.searchBox.setBordered(false);
            this.searchBox.setVisible(false);
            this.searchBox.setTextColor(0xFFFFFF);
            this.children.add(this.searchBox);
            int i = selectedTab;
            selectedTab = -1;
            this.selectTab(CreativeModeTab.TABS[i]);
            this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
            this.listener = new CreativeInventoryListener(this.minecraft);
            this.minecraft.player.inventoryMenu.addSlotListener(this.listener);
        } else {
            this.minecraft.setScreen(new InventoryScreen(this.minecraft.player));
        }
    }

    @Override
    public void resize(Minecraft minecraft, int i, int j) {
        String string = this.searchBox.getValue();
        this.init(minecraft, i, j);
        this.searchBox.setValue(string);
        if (!this.searchBox.getValue().isEmpty()) {
            this.refreshSearchResults();
        }
    }

    @Override
    public void removed() {
        super.removed();
        if (this.minecraft.player != null && this.minecraft.player.inventory != null) {
            this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
        }
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean charTyped(char c, int i) {
        if (this.ignoreTextInput) {
            return false;
        }
        if (selectedTab != CreativeModeTab.TAB_SEARCH.getId()) {
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
        if (selectedTab != CreativeModeTab.TAB_SEARCH.getId()) {
            if (this.minecraft.options.keyChat.matches(i, j)) {
                this.ignoreTextInput = true;
                this.selectTab(CreativeModeTab.TAB_SEARCH);
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
            for (Item item : Registry.ITEM) {
                item.fillItemCategory(CreativeModeTab.TAB_SEARCH, ((ItemPickerMenu)this.menu).items);
            }
        } else {
            MutableSearchTree<ItemStack> searchTree;
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
        TagCollection<Item> tagCollection = ItemTags.getAllTags();
        tagCollection.getAvailableTags().stream().filter(predicate).forEach(resourceLocation -> this.visibleTags.put((ResourceLocation)resourceLocation, tagCollection.getTag((ResourceLocation)resourceLocation)));
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int i, int j) {
        CreativeModeTab creativeModeTab = CreativeModeTab.TABS[selectedTab];
        if (creativeModeTab.showTitle()) {
            RenderSystem.disableBlend();
            this.font.draw(poseStack, creativeModeTab.getDisplayName(), 8.0f, 6.0f, 0x404040);
        }
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if (i == 0) {
            double f = d - (double)this.leftPos;
            double g = e - (double)this.topPos;
            for (CreativeModeTab creativeModeTab : CreativeModeTab.TABS) {
                if (!this.checkTabClicked(creativeModeTab, f, g)) continue;
                return true;
            }
            if (selectedTab != CreativeModeTab.TAB_INVENTORY.getId() && this.insideScrollbar(d, e)) {
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
            for (CreativeModeTab creativeModeTab : CreativeModeTab.TABS) {
                if (!this.checkTabClicked(creativeModeTab, f, g)) continue;
                this.selectTab(creativeModeTab);
                return true;
            }
        }
        return super.mouseReleased(d, e, i);
    }

    private boolean canScroll() {
        return selectedTab != CreativeModeTab.TAB_INVENTORY.getId() && CreativeModeTab.TABS[selectedTab].canScroll() && ((ItemPickerMenu)this.menu).canScroll();
    }

    private void selectTab(CreativeModeTab creativeModeTab) {
        int k;
        int j;
        int i = selectedTab;
        selectedTab = creativeModeTab.getId();
        this.quickCraftSlots.clear();
        ((ItemPickerMenu)this.menu).items.clear();
        if (creativeModeTab == CreativeModeTab.TAB_HOTBAR) {
            HotbarManager hotbarManager = this.minecraft.getHotbarManager();
            for (j = 0; j < 9; ++j) {
                Hotbar hotbar = hotbarManager.get(j);
                if (hotbar.isEmpty()) {
                    for (k = 0; k < 9; ++k) {
                        if (k == j) {
                            ItemStack itemStack = new ItemStack(Items.PAPER);
                            itemStack.getOrCreateTagElement("CustomCreativeLock");
                            Component component = this.minecraft.options.keyHotbarSlots[j].getTranslatedKeyMessage();
                            Component component2 = this.minecraft.options.keySaveHotbarActivator.getTranslatedKeyMessage();
                            itemStack.setHoverName(new TranslatableComponent("inventory.hotbarInfo", component2, component));
                            ((ItemPickerMenu)this.menu).items.add(itemStack);
                            continue;
                        }
                        ((ItemPickerMenu)this.menu).items.add(ItemStack.EMPTY);
                    }
                    continue;
                }
                ((ItemPickerMenu)this.menu).items.addAll(hotbar);
            }
        } else if (creativeModeTab != CreativeModeTab.TAB_SEARCH) {
            creativeModeTab.fillItemList(((ItemPickerMenu)this.menu).items);
        }
        if (creativeModeTab == CreativeModeTab.TAB_INVENTORY) {
            InventoryMenu abstractContainerMenu = this.minecraft.player.inventoryMenu;
            if (this.originalSlots == null) {
                this.originalSlots = ImmutableList.copyOf(((ItemPickerMenu)this.menu).slots);
            }
            ((ItemPickerMenu)this.menu).slots.clear();
            for (j = 0; j < abstractContainerMenu.slots.size(); ++j) {
                int o;
                int n;
                int m;
                if (j >= 5 && j < 9) {
                    int l = j - 5;
                    m = l / 2;
                    n = l % 2;
                    o = 54 + m * 54;
                    k = 6 + n * 27;
                } else if (j >= 0 && j < 5) {
                    o = -2000;
                    k = -2000;
                } else if (j == 45) {
                    o = 35;
                    k = 20;
                } else {
                    int l = j - 9;
                    m = l % 9;
                    n = l / 9;
                    o = 9 + m * 18;
                    k = j >= 36 ? 112 : 54 + n * 18;
                }
                SlotWrapper slot = new SlotWrapper(abstractContainerMenu.slots.get(j), j, o, k);
                ((ItemPickerMenu)this.menu).slots.add(slot);
            }
            this.destroyItemSlot = new Slot(CONTAINER, 0, 173, 112);
            ((ItemPickerMenu)this.menu).slots.add(this.destroyItemSlot);
        } else if (i == CreativeModeTab.TAB_INVENTORY.getId()) {
            ((ItemPickerMenu)this.menu).slots.clear();
            ((ItemPickerMenu)this.menu).slots.addAll(this.originalSlots);
            this.originalSlots = null;
        }
        if (this.searchBox != null) {
            if (creativeModeTab == CreativeModeTab.TAB_SEARCH) {
                this.searchBox.setVisible(true);
                this.searchBox.setCanLoseFocus(false);
                this.searchBox.setFocus(true);
                if (i != creativeModeTab.getId()) {
                    this.searchBox.setValue("");
                }
                this.refreshSearchResults();
            } else {
                this.searchBox.setVisible(false);
                this.searchBox.setCanLoseFocus(true);
                this.searchBox.setFocus(false);
                this.searchBox.setValue("");
            }
        }
        this.scrollOffs = 0.0f;
        ((ItemPickerMenu)this.menu).scrollTo(0.0f);
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f) {
        if (!this.canScroll()) {
            return false;
        }
        int i = (((ItemPickerMenu)this.menu).items.size() + 9 - 1) / 9 - 5;
        this.scrollOffs = (float)((double)this.scrollOffs - f / (double)i);
        this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0f, 1.0f);
        ((ItemPickerMenu)this.menu).scrollTo(this.scrollOffs);
        return true;
    }

    @Override
    protected boolean hasClickedOutside(double d, double e, int i, int j, int k) {
        boolean bl = d < (double)i || e < (double)j || d >= (double)(i + this.imageWidth) || e >= (double)(j + this.imageHeight);
        this.hasClickedOutside = bl && !this.checkTabClicked(CreativeModeTab.TABS[selectedTab], d, e);
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
        for (CreativeModeTab creativeModeTab : CreativeModeTab.TABS) {
            if (this.checkTabHovering(poseStack, creativeModeTab, i, j)) break;
        }
        if (this.destroyItemSlot != null && selectedTab == CreativeModeTab.TAB_INVENTORY.getId() && this.isHovering(this.destroyItemSlot.x, this.destroyItemSlot.y, 16, 16, i, j)) {
            this.renderTooltip(poseStack, TRASH_SLOT_TOOLTIP, i, j);
        }
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.renderTooltip(poseStack, i, j);
    }

    @Override
    protected void renderTooltip(PoseStack poseStack, ItemStack itemStack, int i, int j) {
        if (selectedTab == CreativeModeTab.TAB_SEARCH.getId()) {
            Map<Enchantment, Integer> map;
            List<Component> list = itemStack.getTooltipLines(this.minecraft.player, this.minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
            ArrayList<Component> list2 = Lists.newArrayList(list);
            Item item = itemStack.getItem();
            CreativeModeTab creativeModeTab = item.getItemCategory();
            if (creativeModeTab == null && item == Items.ENCHANTED_BOOK && (map = EnchantmentHelper.getEnchantments(itemStack)).size() == 1) {
                Enchantment enchantment = map.keySet().iterator().next();
                for (CreativeModeTab creativeModeTab2 : CreativeModeTab.TABS) {
                    if (!creativeModeTab2.hasEnchantmentCategory(enchantment.category)) continue;
                    creativeModeTab = creativeModeTab2;
                    break;
                }
            }
            this.visibleTags.forEach((resourceLocation, tag) -> {
                if (tag.contains(item)) {
                    list2.add(1, new TextComponent("#" + resourceLocation).withStyle(ChatFormatting.DARK_PURPLE));
                }
            });
            if (creativeModeTab != null) {
                list2.add(1, creativeModeTab.getDisplayName().copy().withStyle(ChatFormatting.BLUE));
            }
            this.renderComponentTooltip(poseStack, list2, i, j);
        } else {
            super.renderTooltip(poseStack, itemStack, i, j);
        }
    }

    @Override
    protected void renderBg(PoseStack poseStack, float f, int i, int j) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        CreativeModeTab creativeModeTab = CreativeModeTab.TABS[selectedTab];
        for (CreativeModeTab creativeModeTab2 : CreativeModeTab.TABS) {
            this.minecraft.getTextureManager().bind(CREATIVE_TABS_LOCATION);
            if (creativeModeTab2.getId() == selectedTab) continue;
            this.renderTabButton(poseStack, creativeModeTab2);
        }
        this.minecraft.getTextureManager().bind(new ResourceLocation("textures/gui/container/creative_inventory/tab_" + creativeModeTab.getBackgroundSuffix()));
        this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        this.searchBox.render(poseStack, i, j, f);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        int k = this.leftPos + 175;
        int l = this.topPos + 18;
        int m = l + 112;
        this.minecraft.getTextureManager().bind(CREATIVE_TABS_LOCATION);
        if (creativeModeTab.canScroll()) {
            this.blit(poseStack, k, l + (int)((float)(m - l - 17) * this.scrollOffs), 232 + (this.canScroll() ? 0 : 12), 0, 12, 15);
        }
        this.renderTabButton(poseStack, creativeModeTab);
        if (creativeModeTab == CreativeModeTab.TAB_INVENTORY) {
            InventoryScreen.renderEntityInInventory(this.leftPos + 88, this.topPos + 45, 20, this.leftPos + 88 - i, this.topPos + 45 - 30 - j, this.minecraft.player);
        }
    }

    protected boolean checkTabClicked(CreativeModeTab creativeModeTab, double d, double e) {
        int i = creativeModeTab.getColumn();
        int j = 28 * i;
        int k = 0;
        if (creativeModeTab.isAlignedRight()) {
            j = this.imageWidth - 28 * (6 - i) + 2;
        } else if (i > 0) {
            j += i;
        }
        k = creativeModeTab.isTopRow() ? (k -= 32) : (k += this.imageHeight);
        return d >= (double)j && d <= (double)(j + 28) && e >= (double)k && e <= (double)(k + 32);
    }

    protected boolean checkTabHovering(PoseStack poseStack, CreativeModeTab creativeModeTab, int i, int j) {
        int k = creativeModeTab.getColumn();
        int l = 28 * k;
        int m = 0;
        if (creativeModeTab.isAlignedRight()) {
            l = this.imageWidth - 28 * (6 - k) + 2;
        } else if (k > 0) {
            l += k;
        }
        m = creativeModeTab.isTopRow() ? (m -= 32) : (m += this.imageHeight);
        if (this.isHovering(l + 3, m + 3, 23, 27, i, j)) {
            this.renderTooltip(poseStack, creativeModeTab.getDisplayName(), i, j);
            return true;
        }
        return false;
    }

    protected void renderTabButton(PoseStack poseStack, CreativeModeTab creativeModeTab) {
        boolean bl = creativeModeTab.getId() == selectedTab;
        boolean bl2 = creativeModeTab.isTopRow();
        int i = creativeModeTab.getColumn();
        int j = i * 28;
        int k = 0;
        int l = this.leftPos + 28 * i;
        int m = this.topPos;
        int n = 32;
        if (bl) {
            k += 32;
        }
        if (creativeModeTab.isAlignedRight()) {
            l = this.leftPos + this.imageWidth - 28 * (6 - i);
        } else if (i > 0) {
            l += i;
        }
        if (bl2) {
            m -= 28;
        } else {
            k += 64;
            m += this.imageHeight - 4;
        }
        this.blit(poseStack, l, m, j, k, 28, 32);
        this.itemRenderer.blitOffset = 100.0f;
        int n2 = bl2 ? 1 : -1;
        RenderSystem.enableRescaleNormal();
        ItemStack itemStack = creativeModeTab.getIconItem();
        this.itemRenderer.renderAndDecorateItem(itemStack, l += 6, m += 8 + n2);
        this.itemRenderer.renderGuiItemDecorations(this.font, itemStack, l, m);
        this.itemRenderer.blitOffset = 0.0f;
    }

    public int getSelectedTab() {
        return selectedTab;
    }

    public static void handleHotbarLoadOrSave(Minecraft minecraft, int i, boolean bl, boolean bl2) {
        LocalPlayer localPlayer = minecraft.player;
        HotbarManager hotbarManager = minecraft.getHotbarManager();
        Hotbar hotbar = hotbarManager.get(i);
        if (bl) {
            for (int j = 0; j < Inventory.getSelectionSize(); ++j) {
                ItemStack itemStack = ((ItemStack)hotbar.get(j)).copy();
                localPlayer.inventory.setItem(j, itemStack);
                minecraft.gameMode.handleCreativeModeItemAdd(itemStack, 36 + j);
            }
            localPlayer.inventoryMenu.broadcastChanges();
        } else if (bl2) {
            for (int j = 0; j < Inventory.getSelectionSize(); ++j) {
                hotbar.set(j, localPlayer.inventory.getItem(j).copy());
            }
            Component component = minecraft.options.keyHotbarSlots[i].getTranslatedKeyMessage();
            Component component2 = minecraft.options.keyLoadHotbarActivator.getTranslatedKeyMessage();
            minecraft.gui.setOverlayMessage(new TranslatableComponent("inventory.hotbarSaved", component2, component), false);
            hotbarManager.save();
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
            if (super.mayPickup(player) && this.hasItem()) {
                return this.getItem().getTagElement("CustomCreativeLock") == null;
            }
            return !this.hasItem();
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class SlotWrapper
    extends Slot {
        private final Slot target;

        public SlotWrapper(Slot slot, int i, int j, int k) {
            super(slot.container, i, j, k);
            this.target = slot;
        }

        @Override
        public ItemStack onTake(Player player, ItemStack itemStack) {
            return this.target.onTake(player, itemStack);
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
    public static class ItemPickerMenu
    extends AbstractContainerMenu {
        public final NonNullList<ItemStack> items = NonNullList.create();

        public ItemPickerMenu(Player player) {
            super(null, 0);
            int i;
            Inventory inventory = player.inventory;
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

        public void scrollTo(float f) {
            int i = (this.items.size() + 9 - 1) / 9 - 5;
            int j = (int)((double)(f * (float)i) + 0.5);
            if (j < 0) {
                j = 0;
            }
            for (int k = 0; k < 5; ++k) {
                for (int l = 0; l < 9; ++l) {
                    int m = l + (k + j) * 9;
                    if (m >= 0 && m < this.items.size()) {
                        CONTAINER.setItem(l + k * 9, this.items.get(m));
                        continue;
                    }
                    CONTAINER.setItem(l + k * 9, ItemStack.EMPTY);
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
    }
}

