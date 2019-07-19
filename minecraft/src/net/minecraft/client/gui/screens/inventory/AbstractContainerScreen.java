package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlas;
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
	protected int imageWidth = 176;
	protected int imageHeight = 166;
	protected final T menu;
	protected final Inventory inventory;
	protected int leftPos;
	protected int topPos;
	protected Slot hoveredSlot;
	private Slot clickedSlot;
	private boolean isSplittingStack;
	private ItemStack draggingItem = ItemStack.EMPTY;
	private int snapbackStartX;
	private int snapbackStartY;
	private Slot snapbackEnd;
	private long snapbackTime;
	private ItemStack snapbackItem = ItemStack.EMPTY;
	private Slot quickdropSlot;
	private long quickdropTime;
	protected final Set<Slot> quickCraftSlots = Sets.<Slot>newHashSet();
	protected boolean isQuickCrafting;
	private int quickCraftingType;
	private int quickCraftingButton;
	private boolean skipNextRelease;
	private int quickCraftingRemainder;
	private long lastClickTime;
	private Slot lastClickSlot;
	private int lastClickButton;
	private boolean doubleclick;
	private ItemStack lastQuickMoved = ItemStack.EMPTY;

	public AbstractContainerScreen(T abstractContainerMenu, Inventory inventory, Component component) {
		super(component);
		this.menu = abstractContainerMenu;
		this.inventory = inventory;
		this.skipNextRelease = true;
	}

	@Override
	protected void init() {
		super.init();
		this.leftPos = (this.width - this.imageWidth) / 2;
		this.topPos = (this.height - this.imageHeight) / 2;
	}

	@Override
	public void render(int i, int j, float f) {
		int k = this.leftPos;
		int l = this.topPos;
		this.renderBg(f, i, j);
		GlStateManager.disableRescaleNormal();
		Lighting.turnOff();
		GlStateManager.disableLighting();
		GlStateManager.disableDepthTest();
		super.render(i, j, f);
		Lighting.turnOnGui();
		GlStateManager.pushMatrix();
		GlStateManager.translatef((float)k, (float)l, 0.0F);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableRescaleNormal();
		this.hoveredSlot = null;
		int m = 240;
		int n = 240;
		GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, 240.0F, 240.0F);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

		for (int o = 0; o < this.menu.slots.size(); o++) {
			Slot slot = (Slot)this.menu.slots.get(o);
			if (slot.isActive()) {
				this.renderSlot(slot);
			}

			if (this.isHovering(slot, (double)i, (double)j) && slot.isActive()) {
				this.hoveredSlot = slot;
				GlStateManager.disableLighting();
				GlStateManager.disableDepthTest();
				int p = slot.x;
				int q = slot.y;
				GlStateManager.colorMask(true, true, true, false);
				this.fillGradient(p, q, p + 16, q + 16, -2130706433, -2130706433);
				GlStateManager.colorMask(true, true, true, true);
				GlStateManager.enableLighting();
				GlStateManager.enableDepthTest();
			}
		}

		Lighting.turnOff();
		this.renderLabels(i, j);
		Lighting.turnOnGui();
		Inventory inventory = this.minecraft.player.inventory;
		ItemStack itemStack = this.draggingItem.isEmpty() ? inventory.getCarried() : this.draggingItem;
		if (!itemStack.isEmpty()) {
			int p = 8;
			int q = this.draggingItem.isEmpty() ? 8 : 16;
			String string = null;
			if (!this.draggingItem.isEmpty() && this.isSplittingStack) {
				itemStack = itemStack.copy();
				itemStack.setCount(Mth.ceil((float)itemStack.getCount() / 2.0F));
			} else if (this.isQuickCrafting && this.quickCraftSlots.size() > 1) {
				itemStack = itemStack.copy();
				itemStack.setCount(this.quickCraftingRemainder);
				if (itemStack.isEmpty()) {
					string = "" + ChatFormatting.YELLOW + "0";
				}
			}

			this.renderFloatingItem(itemStack, i - k - 8, j - l - q, string);
		}

		if (!this.snapbackItem.isEmpty()) {
			float g = (float)(Util.getMillis() - this.snapbackTime) / 100.0F;
			if (g >= 1.0F) {
				g = 1.0F;
				this.snapbackItem = ItemStack.EMPTY;
			}

			int q = this.snapbackEnd.x - this.snapbackStartX;
			int r = this.snapbackEnd.y - this.snapbackStartY;
			int s = this.snapbackStartX + (int)((float)q * g);
			int t = this.snapbackStartY + (int)((float)r * g);
			this.renderFloatingItem(this.snapbackItem, s, t, null);
		}

		GlStateManager.popMatrix();
		GlStateManager.enableLighting();
		GlStateManager.enableDepthTest();
		Lighting.turnOn();
	}

	protected void renderTooltip(int i, int j) {
		if (this.minecraft.player.inventory.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
			this.renderTooltip(this.hoveredSlot.getItem(), i, j);
		}
	}

	private void renderFloatingItem(ItemStack itemStack, int i, int j, String string) {
		GlStateManager.translatef(0.0F, 0.0F, 32.0F);
		this.blitOffset = 200;
		this.itemRenderer.blitOffset = 200.0F;
		this.itemRenderer.renderAndDecorateItem(itemStack, i, j);
		this.itemRenderer.renderGuiItemDecorations(this.font, itemStack, i, j - (this.draggingItem.isEmpty() ? 0 : 8), string);
		this.blitOffset = 0;
		this.itemRenderer.blitOffset = 0.0F;
	}

	protected void renderLabels(int i, int j) {
	}

	protected abstract void renderBg(float f, int i, int j);

	private void renderSlot(Slot slot) {
		int i = slot.x;
		int j = slot.y;
		ItemStack itemStack = slot.getItem();
		boolean bl = false;
		boolean bl2 = slot == this.clickedSlot && !this.draggingItem.isEmpty() && !this.isSplittingStack;
		ItemStack itemStack2 = this.minecraft.player.inventory.getCarried();
		String string = null;
		if (slot == this.clickedSlot && !this.draggingItem.isEmpty() && this.isSplittingStack && !itemStack.isEmpty()) {
			itemStack = itemStack.copy();
			itemStack.setCount(itemStack.getCount() / 2);
		} else if (this.isQuickCrafting && this.quickCraftSlots.contains(slot) && !itemStack2.isEmpty()) {
			if (this.quickCraftSlots.size() == 1) {
				return;
			}

			if (AbstractContainerMenu.canItemQuickReplace(slot, itemStack2, true) && this.menu.canDragTo(slot)) {
				itemStack = itemStack2.copy();
				bl = true;
				AbstractContainerMenu.getQuickCraftSlotCount(
					this.quickCraftSlots, this.quickCraftingType, itemStack, slot.getItem().isEmpty() ? 0 : slot.getItem().getCount()
				);
				int k = Math.min(itemStack.getMaxStackSize(), slot.getMaxStackSize(itemStack));
				if (itemStack.getCount() > k) {
					string = ChatFormatting.YELLOW.toString() + k;
					itemStack.setCount(k);
				}
			} else {
				this.quickCraftSlots.remove(slot);
				this.recalculateQuickCraftRemaining();
			}
		}

		this.blitOffset = 100;
		this.itemRenderer.blitOffset = 100.0F;
		if (itemStack.isEmpty() && slot.isActive()) {
			String string2 = slot.getNoItemIcon();
			if (string2 != null) {
				TextureAtlasSprite textureAtlasSprite = this.minecraft.getTextureAtlas().getTexture(string2);
				GlStateManager.disableLighting();
				this.minecraft.getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
				blit(i, j, this.blitOffset, 16, 16, textureAtlasSprite);
				GlStateManager.enableLighting();
				bl2 = true;
			}
		}

		if (!bl2) {
			if (bl) {
				fill(i, j, i + 16, j + 16, -2130706433);
			}

			GlStateManager.enableDepthTest();
			this.itemRenderer.renderAndDecorateItem(this.minecraft.player, itemStack, i, j);
			this.itemRenderer.renderGuiItemDecorations(this.font, itemStack, i, j, string);
		}

		this.itemRenderer.blitOffset = 0.0F;
		this.blitOffset = 0;
	}

	private void recalculateQuickCraftRemaining() {
		ItemStack itemStack = this.minecraft.player.inventory.getCarried();
		if (!itemStack.isEmpty() && this.isQuickCrafting) {
			if (this.quickCraftingType == 2) {
				this.quickCraftingRemainder = itemStack.getMaxStackSize();
			} else {
				this.quickCraftingRemainder = itemStack.getCount();

				for (Slot slot : this.quickCraftSlots) {
					ItemStack itemStack2 = itemStack.copy();
					ItemStack itemStack3 = slot.getItem();
					int i = itemStack3.isEmpty() ? 0 : itemStack3.getCount();
					AbstractContainerMenu.getQuickCraftSlotCount(this.quickCraftSlots, this.quickCraftingType, itemStack2, i);
					int j = Math.min(itemStack2.getMaxStackSize(), slot.getMaxStackSize(itemStack2));
					if (itemStack2.getCount() > j) {
						itemStack2.setCount(j);
					}

					this.quickCraftingRemainder = this.quickCraftingRemainder - (itemStack2.getCount() - i);
				}
			}
		}
	}

	private Slot findSlot(double d, double e) {
		for (int i = 0; i < this.menu.slots.size(); i++) {
			Slot slot = (Slot)this.menu.slots.get(i);
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
			boolean bl = this.minecraft.options.keyPickItem.matchesMouse(i);
			Slot slot = this.findSlot(d, e);
			long l = Util.getMillis();
			this.doubleclick = this.lastClickSlot == slot && l - this.lastClickTime < 250L && this.lastClickButton == i;
			this.skipNextRelease = false;
			if (i == 0 || i == 1 || bl) {
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

				if (this.minecraft.options.touchscreen && bl2 && this.minecraft.player.inventory.getCarried().isEmpty()) {
					this.minecraft.setScreen(null);
					return true;
				}

				if (m != -1) {
					if (this.minecraft.options.touchscreen) {
						if (slot != null && slot.hasItem()) {
							this.clickedSlot = slot;
							this.draggingItem = ItemStack.EMPTY;
							this.isSplittingStack = i == 1;
						} else {
							this.clickedSlot = null;
						}
					} else if (!this.isQuickCrafting) {
						if (this.minecraft.player.inventory.getCarried().isEmpty()) {
							if (this.minecraft.options.keyPickItem.matchesMouse(i)) {
								this.slotClicked(slot, m, i, ClickType.CLONE);
							} else {
								boolean bl3 = m != -999
									&& (
										InputConstants.isKeyDown(Minecraft.getInstance().window.getWindow(), 340)
											|| InputConstants.isKeyDown(Minecraft.getInstance().window.getWindow(), 344)
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
							} else if (this.minecraft.options.keyPickItem.matchesMouse(i)) {
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

	protected boolean hasClickedOutside(double d, double e, int i, int j, int k) {
		return d < (double)i || e < (double)j || d >= (double)(i + this.imageWidth) || e >= (double)(j + this.imageHeight);
	}

	@Override
	public boolean mouseDragged(double d, double e, int i, double f, double g) {
		Slot slot = this.findSlot(d, e);
		ItemStack itemStack = this.minecraft.player.inventory.getCarried();
		if (this.clickedSlot != null && this.minecraft.options.touchscreen) {
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

			if (this.clickedSlot != null && this.minecraft.options.touchscreen) {
				if (i == 0 || i == 1) {
					if (this.draggingItem.isEmpty() && slot != this.clickedSlot) {
						this.draggingItem = this.clickedSlot.getItem();
					}

					boolean bl2 = AbstractContainerMenu.canItemQuickReplace(slot, this.draggingItem, false);
					if (l != -1 && !this.draggingItem.isEmpty() && bl2) {
						this.slotClicked(this.clickedSlot, this.clickedSlot.index, i, ClickType.PICKUP);
						this.slotClicked(slot, l, 0, ClickType.PICKUP);
						if (this.minecraft.player.inventory.getCarried().isEmpty()) {
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

					this.draggingItem = ItemStack.EMPTY;
					this.clickedSlot = null;
				}
			} else if (this.isQuickCrafting && !this.quickCraftSlots.isEmpty()) {
				this.slotClicked(null, -999, AbstractContainerMenu.getQuickcraftMask(0, this.quickCraftingType), ClickType.QUICK_CRAFT);

				for (Slot slot2x : this.quickCraftSlots) {
					this.slotClicked(slot2x, slot2x.index, AbstractContainerMenu.getQuickcraftMask(1, this.quickCraftingType), ClickType.QUICK_CRAFT);
				}

				this.slotClicked(null, -999, AbstractContainerMenu.getQuickcraftMask(2, this.quickCraftingType), ClickType.QUICK_CRAFT);
			} else if (!this.minecraft.player.inventory.getCarried().isEmpty()) {
				if (this.minecraft.options.keyPickItem.matchesMouse(i)) {
					this.slotClicked(slot, l, i, ClickType.CLONE);
				} else {
					boolean bl2 = l != -999
						&& (
							InputConstants.isKeyDown(Minecraft.getInstance().window.getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().window.getWindow(), 344)
						);
					if (bl2) {
						this.lastQuickMoved = slot != null && slot.hasItem() ? slot.getItem().copy() : ItemStack.EMPTY;
					}

					this.slotClicked(slot, l, i, bl2 ? ClickType.QUICK_MOVE : ClickType.PICKUP);
				}
			}
		}

		if (this.minecraft.player.inventory.getCarried().isEmpty()) {
			this.lastClickTime = 0L;
		}

		this.isQuickCrafting = false;
		return true;
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
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (super.keyPressed(i, j, k)) {
			return true;
		} else {
			if (i == 256 || this.minecraft.options.keyInventory.matches(i, j)) {
				this.minecraft.player.closeContainer();
			}

			this.checkNumkeyPressed(i, j);
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

	protected boolean checkNumkeyPressed(int i, int j) {
		if (this.minecraft.player.inventory.getCarried().isEmpty() && this.hoveredSlot != null) {
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
	public void tick() {
		super.tick();
		if (!this.minecraft.player.isAlive() || this.minecraft.player.removed) {
			this.minecraft.player.closeContainer();
		}
	}

	@Override
	public T getMenu() {
		return this.menu;
	}
}
