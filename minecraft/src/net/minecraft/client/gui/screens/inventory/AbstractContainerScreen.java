package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import java.util.Set;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public abstract class AbstractContainerScreen<T extends AbstractContainerMenu> extends Screen implements MenuAccess<T> {
	public static final ResourceLocation INVENTORY_LOCATION = new ResourceLocation("textures/gui/container/inventory.png");
	private static final float SNAPBACK_SPEED = 100.0F;
	private static final int QUICKDROP_DELAY = 500;
	public static final int SLOT_ITEM_BLIT_OFFSET = 100;
	private static final int HOVER_ITEM_BLIT_OFFSET = 200;
	protected int imageWidth = 176;
	protected int imageHeight = 166;
	protected int titleLabelX;
	protected int titleLabelY;
	protected int inventoryLabelX;
	protected int inventoryLabelY;
	protected final T menu;
	protected final Component playerInventoryTitle;
	@Nullable
	protected Slot hoveredSlot;
	@Nullable
	private Slot clickedSlot;
	@Nullable
	private Slot snapbackEnd;
	@Nullable
	private Slot quickdropSlot;
	@Nullable
	private Slot lastClickSlot;
	protected int leftPos;
	protected int topPos;
	private boolean isSplittingStack;
	private ItemStack draggingItem = ItemStack.EMPTY;
	private int snapbackStartX;
	private int snapbackStartY;
	private long snapbackTime;
	private ItemStack snapbackItem = ItemStack.EMPTY;
	private long quickdropTime;
	protected final Set<Slot> quickCraftSlots = Sets.<Slot>newHashSet();
	protected boolean isQuickCrafting;
	private int quickCraftingType;
	private int quickCraftingButton;
	private boolean skipNextRelease;
	private int quickCraftingRemainder;
	private long lastClickTime;
	private int lastClickButton;
	private boolean doubleclick;
	private ItemStack lastQuickMoved = ItemStack.EMPTY;

	public AbstractContainerScreen(T abstractContainerMenu, Inventory inventory, Component component) {
		super(component);
		this.menu = abstractContainerMenu;
		this.playerInventoryTitle = inventory.getDisplayName();
		this.skipNextRelease = true;
		this.titleLabelX = 8;
		this.titleLabelY = 6;
		this.inventoryLabelX = 8;
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	protected void init() {
		this.leftPos = (this.width - this.imageWidth) / 2;
		this.topPos = (this.height - this.imageHeight) / 2;
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		int k = this.leftPos;
		int l = this.topPos;
		this.renderBg(poseStack, f, i, j);
		RenderSystem.disableDepthTest();
		super.render(poseStack, i, j, f);
		poseStack.pushPose();
		poseStack.translate((float)k, (float)l, 0.0F);
		this.hoveredSlot = null;

		for (int m = 0; m < this.menu.slots.size(); m++) {
			Slot slot = this.menu.slots.get(m);
			if (slot.isActive()) {
				this.renderSlot(poseStack, slot);
			}

			if (this.isHovering(slot, (double)i, (double)j) && slot.isActive()) {
				this.hoveredSlot = slot;
				int n = slot.x;
				int o = slot.y;
				renderSlotHighlight(poseStack, n, o, 0);
			}
		}

		this.renderLabels(poseStack, i, j);
		ItemStack itemStack = this.draggingItem.isEmpty() ? this.menu.getCarried() : this.draggingItem;
		if (!itemStack.isEmpty()) {
			int p = 8;
			int n = this.draggingItem.isEmpty() ? 8 : 16;
			String string = null;
			if (!this.draggingItem.isEmpty() && this.isSplittingStack) {
				itemStack = itemStack.copyWithCount(Mth.ceil((float)itemStack.getCount() / 2.0F));
			} else if (this.isQuickCrafting && this.quickCraftSlots.size() > 1) {
				itemStack = itemStack.copyWithCount(this.quickCraftingRemainder);
				if (itemStack.isEmpty()) {
					string = ChatFormatting.YELLOW + "0";
				}
			}

			this.renderFloatingItem(poseStack, itemStack, i - k - 8, j - l - n, string);
		}

		if (!this.snapbackItem.isEmpty()) {
			float g = (float)(Util.getMillis() - this.snapbackTime) / 100.0F;
			if (g >= 1.0F) {
				g = 1.0F;
				this.snapbackItem = ItemStack.EMPTY;
			}

			int n = this.snapbackEnd.x - this.snapbackStartX;
			int o = this.snapbackEnd.y - this.snapbackStartY;
			int q = this.snapbackStartX + (int)((float)n * g);
			int r = this.snapbackStartY + (int)((float)o * g);
			this.renderFloatingItem(poseStack, this.snapbackItem, q, r, null);
		}

		poseStack.popPose();
		RenderSystem.enableDepthTest();
	}

	public static void renderSlotHighlight(PoseStack poseStack, int i, int j, int k) {
		RenderSystem.disableDepthTest();
		RenderSystem.colorMask(true, true, true, false);
		fillGradient(poseStack, i, j, i + 16, j + 16, -2130706433, -2130706433, k);
		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.enableDepthTest();
	}

	protected void renderTooltip(PoseStack poseStack, int i, int j) {
		if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
			this.renderTooltip(poseStack, this.hoveredSlot.getItem(), i, j);
		}
	}

	private void renderFloatingItem(PoseStack poseStack, ItemStack itemStack, int i, int j, String string) {
		poseStack.pushPose();
		poseStack.translate(0.0F, 0.0F, 232.0F);
		this.itemRenderer.renderAndDecorateItem(poseStack, itemStack, i, j);
		this.itemRenderer.renderGuiItemDecorations(poseStack, this.font, itemStack, i, j - (this.draggingItem.isEmpty() ? 0 : 8), string);
		poseStack.popPose();
	}

	protected void renderLabels(PoseStack poseStack, int i, int j) {
		this.font.draw(poseStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
		this.font.draw(poseStack, this.playerInventoryTitle, (float)this.inventoryLabelX, (float)this.inventoryLabelY, 4210752);
	}

	protected abstract void renderBg(PoseStack poseStack, float f, int i, int j);

	private void renderSlot(PoseStack poseStack, Slot slot) {
		int i = slot.x;
		int j = slot.y;
		ItemStack itemStack = slot.getItem();
		boolean bl = false;
		boolean bl2 = slot == this.clickedSlot && !this.draggingItem.isEmpty() && !this.isSplittingStack;
		ItemStack itemStack2 = this.menu.getCarried();
		String string = null;
		if (slot == this.clickedSlot && !this.draggingItem.isEmpty() && this.isSplittingStack && !itemStack.isEmpty()) {
			itemStack = itemStack.copyWithCount(itemStack.getCount() / 2);
		} else if (this.isQuickCrafting && this.quickCraftSlots.contains(slot) && !itemStack2.isEmpty()) {
			if (this.quickCraftSlots.size() == 1) {
				return;
			}

			if (AbstractContainerMenu.canItemQuickReplace(slot, itemStack2, true) && this.menu.canDragTo(slot)) {
				bl = true;
				int k = Math.min(itemStack2.getMaxStackSize(), slot.getMaxStackSize(itemStack2));
				int l = slot.getItem().isEmpty() ? 0 : slot.getItem().getCount();
				int m = AbstractContainerMenu.getQuickCraftPlaceCount(this.quickCraftSlots, this.quickCraftingType, itemStack2) + l;
				if (m > k) {
					m = k;
					string = ChatFormatting.YELLOW.toString() + k;
				}

				itemStack = itemStack2.copyWithCount(m);
			} else {
				this.quickCraftSlots.remove(slot);
				this.recalculateQuickCraftRemaining();
			}
		}

		poseStack.pushPose();
		poseStack.translate(0.0F, 0.0F, 100.0F);
		if (itemStack.isEmpty() && slot.isActive()) {
			Pair<ResourceLocation, ResourceLocation> pair = slot.getNoItemIcon();
			if (pair != null) {
				TextureAtlasSprite textureAtlasSprite = (TextureAtlasSprite)this.minecraft.getTextureAtlas(pair.getFirst()).apply(pair.getSecond());
				RenderSystem.setShaderTexture(0, textureAtlasSprite.atlasLocation());
				blit(poseStack, i, j, 0, 16, 16, textureAtlasSprite);
				bl2 = true;
			}
		}

		if (!bl2) {
			if (bl) {
				fill(poseStack, i, j, i + 16, j + 16, -2130706433);
			}

			this.itemRenderer.renderAndDecorateItem(poseStack, this.minecraft.player, itemStack, i, j, slot.x + slot.y * this.imageWidth);
			this.itemRenderer.renderGuiItemDecorations(poseStack, this.font, itemStack, i, j, string);
		}

		poseStack.popPose();
	}

	private void recalculateQuickCraftRemaining() {
		ItemStack itemStack = this.menu.getCarried();
		if (!itemStack.isEmpty() && this.isQuickCrafting) {
			if (this.quickCraftingType == 2) {
				this.quickCraftingRemainder = itemStack.getMaxStackSize();
			} else {
				this.quickCraftingRemainder = itemStack.getCount();

				for (Slot slot : this.quickCraftSlots) {
					ItemStack itemStack2 = slot.getItem();
					int i = itemStack2.isEmpty() ? 0 : itemStack2.getCount();
					int j = Math.min(itemStack.getMaxStackSize(), slot.getMaxStackSize(itemStack));
					int k = Math.min(AbstractContainerMenu.getQuickCraftPlaceCount(this.quickCraftSlots, this.quickCraftingType, itemStack) + i, j);
					this.quickCraftingRemainder -= k - i;
				}
			}
		}
	}

	@Nullable
	private Slot findSlot(double d, double e) {
		for (int i = 0; i < this.menu.slots.size(); i++) {
			Slot slot = this.menu.slots.get(i);
			if (this.isHovering(slot, d, e) && slot.isActive()) {
				return slot;
			}
		}

		return null;
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		if (super.mouseClicked(d, e, i)) {
			return true;
		} else {
			boolean bl = this.minecraft.options.keyPickItem.matchesMouse(i) && this.minecraft.gameMode.hasInfiniteItems();
			Slot slot = this.findSlot(d, e);
			long l = Util.getMillis();
			this.doubleclick = this.lastClickSlot == slot && l - this.lastClickTime < 250L && this.lastClickButton == i;
			this.skipNextRelease = false;
			if (i != 0 && i != 1 && !bl) {
				this.checkHotbarMouseClicked(i);
			} else {
				int j = this.leftPos;
				int k = this.topPos;
				boolean bl2 = this.hasClickedOutside(d, e, j, k, i);
				int m = -1;
				if (slot != null) {
					m = slot.index;
				}

				if (bl2) {
					m = -999;
				}

				if (this.minecraft.options.touchscreen().get() && bl2 && this.menu.getCarried().isEmpty()) {
					this.onClose();
					return true;
				}

				if (m != -1) {
					if (this.minecraft.options.touchscreen().get()) {
						if (slot != null && slot.hasItem()) {
							this.clickedSlot = slot;
							this.draggingItem = ItemStack.EMPTY;
							this.isSplittingStack = i == 1;
						} else {
							this.clickedSlot = null;
						}
					} else if (!this.isQuickCrafting) {
						if (this.menu.getCarried().isEmpty()) {
							if (bl) {
								this.slotClicked(slot, m, i, ClickType.CLONE);
							} else {
								boolean bl3 = m != -999
									&& (
										InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340)
											|| InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344)
									);
								ClickType clickType = ClickType.PICKUP;
								if (bl3) {
									this.lastQuickMoved = slot != null && slot.hasItem() ? slot.getItem().copy() : ItemStack.EMPTY;
									clickType = ClickType.QUICK_MOVE;
								} else if (m == -999) {
									clickType = ClickType.THROW;
								}

								this.slotClicked(slot, m, i, clickType);
							}

							this.skipNextRelease = true;
						} else {
							this.isQuickCrafting = true;
							this.quickCraftingButton = i;
							this.quickCraftSlots.clear();
							if (i == 0) {
								this.quickCraftingType = 0;
							} else if (i == 1) {
								this.quickCraftingType = 1;
							} else if (bl) {
								this.quickCraftingType = 2;
							}
						}
					}
				}
			}

			this.lastClickSlot = slot;
			this.lastClickTime = l;
			this.lastClickButton = i;
			return true;
		}
	}

	private void checkHotbarMouseClicked(int i) {
		if (this.hoveredSlot != null && this.menu.getCarried().isEmpty()) {
			if (this.minecraft.options.keySwapOffhand.matchesMouse(i)) {
				this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, 40, ClickType.SWAP);
				return;
			}

			for (int j = 0; j < 9; j++) {
				if (this.minecraft.options.keyHotbarSlots[j].matchesMouse(i)) {
					this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, j, ClickType.SWAP);
				}
			}
		}
	}

	protected boolean hasClickedOutside(double d, double e, int i, int j, int k) {
		return d < (double)i || e < (double)j || d >= (double)(i + this.imageWidth) || e >= (double)(j + this.imageHeight);
	}

	@Override
	public boolean mouseDragged(double d, double e, int i, double f, double g) {
		Slot slot = this.findSlot(d, e);
		ItemStack itemStack = this.menu.getCarried();
		if (this.clickedSlot != null && this.minecraft.options.touchscreen().get()) {
			if (i == 0 || i == 1) {
				if (this.draggingItem.isEmpty()) {
					if (slot != this.clickedSlot && !this.clickedSlot.getItem().isEmpty()) {
						this.draggingItem = this.clickedSlot.getItem().copy();
					}
				} else if (this.draggingItem.getCount() > 1 && slot != null && AbstractContainerMenu.canItemQuickReplace(slot, this.draggingItem, false)) {
					long l = Util.getMillis();
					if (this.quickdropSlot == slot) {
						if (l - this.quickdropTime > 500L) {
							this.slotClicked(this.clickedSlot, this.clickedSlot.index, 0, ClickType.PICKUP);
							this.slotClicked(slot, slot.index, 1, ClickType.PICKUP);
							this.slotClicked(this.clickedSlot, this.clickedSlot.index, 0, ClickType.PICKUP);
							this.quickdropTime = l + 750L;
							this.draggingItem.shrink(1);
						}
					} else {
						this.quickdropSlot = slot;
						this.quickdropTime = l;
					}
				}
			}
		} else if (this.isQuickCrafting
			&& slot != null
			&& !itemStack.isEmpty()
			&& (itemStack.getCount() > this.quickCraftSlots.size() || this.quickCraftingType == 2)
			&& AbstractContainerMenu.canItemQuickReplace(slot, itemStack, true)
			&& slot.mayPlace(itemStack)
			&& this.menu.canDragTo(slot)) {
			this.quickCraftSlots.add(slot);
			this.recalculateQuickCraftRemaining();
		}

		return true;
	}

	@Override
	public boolean mouseReleased(double d, double e, int i) {
		Slot slot = this.findSlot(d, e);
		int j = this.leftPos;
		int k = this.topPos;
		boolean bl = this.hasClickedOutside(d, e, j, k, i);
		int l = -1;
		if (slot != null) {
			l = slot.index;
		}

		if (bl) {
			l = -999;
		}

		if (this.doubleclick && slot != null && i == 0 && this.menu.canTakeItemForPickAll(ItemStack.EMPTY, slot)) {
			if (hasShiftDown()) {
				if (!this.lastQuickMoved.isEmpty()) {
					for (Slot slot2 : this.menu.slots) {
						if (slot2 != null
							&& slot2.mayPickup(this.minecraft.player)
							&& slot2.hasItem()
							&& slot2.container == slot.container
							&& AbstractContainerMenu.canItemQuickReplace(slot2, this.lastQuickMoved, true)) {
							this.slotClicked(slot2, slot2.index, i, ClickType.QUICK_MOVE);
						}
					}
				}
			} else {
				this.slotClicked(slot, l, i, ClickType.PICKUP_ALL);
			}

			this.doubleclick = false;
			this.lastClickTime = 0L;
		} else {
			if (this.isQuickCrafting && this.quickCraftingButton != i) {
				this.isQuickCrafting = false;
				this.quickCraftSlots.clear();
				this.skipNextRelease = true;
				return true;
			}

			if (this.skipNextRelease) {
				this.skipNextRelease = false;
				return true;
			}

			if (this.clickedSlot != null && this.minecraft.options.touchscreen().get()) {
				if (i == 0 || i == 1) {
					if (this.draggingItem.isEmpty() && slot != this.clickedSlot) {
						this.draggingItem = this.clickedSlot.getItem();
					}

					boolean bl2 = AbstractContainerMenu.canItemQuickReplace(slot, this.draggingItem, false);
					if (l != -1 && !this.draggingItem.isEmpty() && bl2) {
						this.slotClicked(this.clickedSlot, this.clickedSlot.index, i, ClickType.PICKUP);
						this.slotClicked(slot, l, 0, ClickType.PICKUP);
						if (this.menu.getCarried().isEmpty()) {
							this.snapbackItem = ItemStack.EMPTY;
						} else {
							this.slotClicked(this.clickedSlot, this.clickedSlot.index, i, ClickType.PICKUP);
							this.snapbackStartX = Mth.floor(d - (double)j);
							this.snapbackStartY = Mth.floor(e - (double)k);
							this.snapbackEnd = this.clickedSlot;
							this.snapbackItem = this.draggingItem;
							this.snapbackTime = Util.getMillis();
						}
					} else if (!this.draggingItem.isEmpty()) {
						this.snapbackStartX = Mth.floor(d - (double)j);
						this.snapbackStartY = Mth.floor(e - (double)k);
						this.snapbackEnd = this.clickedSlot;
						this.snapbackItem = this.draggingItem;
						this.snapbackTime = Util.getMillis();
					}

					this.clearDraggingState();
				}
			} else if (this.isQuickCrafting && !this.quickCraftSlots.isEmpty()) {
				this.slotClicked(null, -999, AbstractContainerMenu.getQuickcraftMask(0, this.quickCraftingType), ClickType.QUICK_CRAFT);

				for (Slot slot2x : this.quickCraftSlots) {
					this.slotClicked(slot2x, slot2x.index, AbstractContainerMenu.getQuickcraftMask(1, this.quickCraftingType), ClickType.QUICK_CRAFT);
				}

				this.slotClicked(null, -999, AbstractContainerMenu.getQuickcraftMask(2, this.quickCraftingType), ClickType.QUICK_CRAFT);
			} else if (!this.menu.getCarried().isEmpty()) {
				if (this.minecraft.options.keyPickItem.matchesMouse(i)) {
					this.slotClicked(slot, l, i, ClickType.CLONE);
				} else {
					boolean bl2 = l != -999
						&& (
							InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340)
								|| InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344)
						);
					if (bl2) {
						this.lastQuickMoved = slot != null && slot.hasItem() ? slot.getItem().copy() : ItemStack.EMPTY;
					}

					this.slotClicked(slot, l, i, bl2 ? ClickType.QUICK_MOVE : ClickType.PICKUP);
				}
			}
		}

		if (this.menu.getCarried().isEmpty()) {
			this.lastClickTime = 0L;
		}

		this.isQuickCrafting = false;
		return true;
	}

	public void clearDraggingState() {
		this.draggingItem = ItemStack.EMPTY;
		this.clickedSlot = null;
	}

	private boolean isHovering(Slot slot, double d, double e) {
		return this.isHovering(slot.x, slot.y, 16, 16, d, e);
	}

	protected boolean isHovering(int i, int j, int k, int l, double d, double e) {
		int m = this.leftPos;
		int n = this.topPos;
		d -= (double)m;
		e -= (double)n;
		return d >= (double)(i - 1) && d < (double)(i + k + 1) && e >= (double)(j - 1) && e < (double)(j + l + 1);
	}

	protected void slotClicked(Slot slot, int i, int j, ClickType clickType) {
		if (slot != null) {
			i = slot.index;
		}

		this.minecraft.gameMode.handleInventoryMouseClick(this.menu.containerId, i, j, clickType, this.minecraft.player);
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (super.keyPressed(i, j, k)) {
			return true;
		} else if (this.minecraft.options.keyInventory.matches(i, j)) {
			this.onClose();
			return true;
		} else {
			this.checkHotbarKeyPressed(i, j);
			if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
				if (this.minecraft.options.keyPickItem.matches(i, j)) {
					this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, 0, ClickType.CLONE);
				} else if (this.minecraft.options.keyDrop.matches(i, j)) {
					this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, hasControlDown() ? 1 : 0, ClickType.THROW);
				}
			}

			return true;
		}
	}

	protected boolean checkHotbarKeyPressed(int i, int j) {
		if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null) {
			if (this.minecraft.options.keySwapOffhand.matches(i, j)) {
				this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, 40, ClickType.SWAP);
				return true;
			}

			for (int k = 0; k < 9; k++) {
				if (this.minecraft.options.keyHotbarSlots[k].matches(i, j)) {
					this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, k, ClickType.SWAP);
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void removed() {
		if (this.minecraft.player != null) {
			this.menu.removed(this.minecraft.player);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public final void tick() {
		super.tick();
		if (this.minecraft.player.isAlive() && !this.minecraft.player.isRemoved()) {
			this.containerTick();
		} else {
			this.minecraft.player.closeContainer();
		}
	}

	protected void containerTick() {
	}

	@Override
	public T getMenu() {
		return this.menu;
	}

	@Override
	public void onClose() {
		this.minecraft.player.closeContainer();
		super.onClose();
	}
}
