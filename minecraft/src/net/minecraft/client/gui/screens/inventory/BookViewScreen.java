package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.blaze3d.platform.GlStateManager;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;

@Environment(EnvType.CLIENT)
public class BookViewScreen extends Screen {
	public static final BookViewScreen.BookAccess EMPTY_ACCESS = new BookViewScreen.BookAccess() {
		@Override
		public int getPageCount() {
			return 0;
		}

		@Override
		public Component getPageRaw(int i) {
			return new TextComponent("");
		}
	};
	public static final ResourceLocation BOOK_LOCATION = new ResourceLocation("textures/gui/book.png");
	private BookViewScreen.BookAccess bookAccess;
	private int currentPage;
	private List<Component> cachedPageComponents = Collections.emptyList();
	private int cachedPage = -1;
	private PageButton forwardButton;
	private PageButton backButton;
	private final boolean playTurnSound;

	public BookViewScreen(BookViewScreen.BookAccess bookAccess) {
		this(bookAccess, true);
	}

	public BookViewScreen() {
		this(EMPTY_ACCESS, false);
	}

	private BookViewScreen(BookViewScreen.BookAccess bookAccess, boolean bl) {
		super(NarratorChatListener.NO_TITLE);
		this.bookAccess = bookAccess;
		this.playTurnSound = bl;
	}

	public void setBookAccess(BookViewScreen.BookAccess bookAccess) {
		this.bookAccess = bookAccess;
		this.currentPage = Mth.clamp(this.currentPage, 0, bookAccess.getPageCount());
		this.updateButtonVisibility();
		this.cachedPage = -1;
	}

	public boolean setPage(int i) {
		int j = Mth.clamp(i, 0, this.bookAccess.getPageCount() - 1);
		if (j != this.currentPage) {
			this.currentPage = j;
			this.updateButtonVisibility();
			this.cachedPage = -1;
			return true;
		} else {
			return false;
		}
	}

	protected boolean forcePage(int i) {
		return this.setPage(i);
	}

	@Override
	protected void init() {
		this.createMenuControls();
		this.createPageControlButtons();
	}

	protected void createMenuControls() {
		this.addButton(new Button(this.width / 2 - 100, 196, 200, 20, I18n.get("gui.done"), button -> this.minecraft.setScreen(null)));
	}

	protected void createPageControlButtons() {
		int i = (this.width - 192) / 2;
		int j = 2;
		this.forwardButton = this.addButton(new PageButton(i + 116, 159, true, button -> this.pageForward(), this.playTurnSound));
		this.backButton = this.addButton(new PageButton(i + 43, 159, false, button -> this.pageBack(), this.playTurnSound));
		this.updateButtonVisibility();
	}

	private int getNumPages() {
		return this.bookAccess.getPageCount();
	}

	protected void pageBack() {
		if (this.currentPage > 0) {
			this.currentPage--;
		}

		this.updateButtonVisibility();
	}

	protected void pageForward() {
		if (this.currentPage < this.getNumPages() - 1) {
			this.currentPage++;
		}

		this.updateButtonVisibility();
	}

	private void updateButtonVisibility() {
		this.forwardButton.visible = this.currentPage < this.getNumPages() - 1;
		this.backButton.visible = this.currentPage > 0;
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (super.keyPressed(i, j, k)) {
			return true;
		} else {
			switch (i) {
				case 266:
					this.backButton.onPress();
					return true;
				case 267:
					this.forwardButton.onPress();
					return true;
				default:
					return false;
			}
		}
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bind(BOOK_LOCATION);
		int k = (this.width - 192) / 2;
		int l = 2;
		this.blit(k, 2, 0, 0, 192, 192);
		String string = I18n.get("book.pageIndicator", this.currentPage + 1, Math.max(this.getNumPages(), 1));
		if (this.cachedPage != this.currentPage) {
			Component component = this.bookAccess.getPage(this.currentPage);
			this.cachedPageComponents = ComponentRenderUtils.wrapComponents(component, 114, this.font, true, true);
		}

		this.cachedPage = this.currentPage;
		int m = this.strWidth(string);
		this.font.draw(string, (float)(k - m + 192 - 44), 18.0F, 0);
		int n = Math.min(128 / 9, this.cachedPageComponents.size());

		for (int o = 0; o < n; o++) {
			Component component2 = (Component)this.cachedPageComponents.get(o);
			this.font.draw(component2.getColoredString(), (float)(k + 36), (float)(32 + o * 9), 0);
		}

		Component component3 = this.getClickedComponentAt((double)i, (double)j);
		if (component3 != null) {
			this.renderComponentHoverEffect(component3, i, j);
		}

		super.render(i, j, f);
	}

	private int strWidth(String string) {
		return this.font.width(this.font.isBidirectional() ? this.font.bidirectionalShaping(string) : string);
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		if (i == 0) {
			Component component = this.getClickedComponentAt(d, e);
			if (component != null && this.handleComponentClicked(component)) {
				return true;
			}
		}

		return super.mouseClicked(d, e, i);
	}

	@Override
	public boolean handleComponentClicked(Component component) {
		ClickEvent clickEvent = component.getStyle().getClickEvent();
		if (clickEvent == null) {
			return false;
		} else if (clickEvent.getAction() == ClickEvent.Action.CHANGE_PAGE) {
			String string = clickEvent.getValue();

			try {
				int i = Integer.parseInt(string) - 1;
				return this.forcePage(i);
			} catch (Exception var5) {
				return false;
			}
		} else {
			boolean bl = super.handleComponentClicked(component);
			if (bl && clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
				this.minecraft.setScreen(null);
			}

			return bl;
		}
	}

	@Nullable
	public Component getClickedComponentAt(double d, double e) {
		if (this.cachedPageComponents == null) {
			return null;
		} else {
			int i = Mth.floor(d - (double)((this.width - 192) / 2) - 36.0);
			int j = Mth.floor(e - 2.0 - 30.0);
			if (i >= 0 && j >= 0) {
				int k = Math.min(128 / 9, this.cachedPageComponents.size());
				if (i <= 114 && j < 9 * k + k) {
					int l = j / 9;
					if (l >= 0 && l < this.cachedPageComponents.size()) {
						Component component = (Component)this.cachedPageComponents.get(l);
						int m = 0;

						for (Component component2 : component) {
							if (component2 instanceof TextComponent) {
								m += this.minecraft.font.width(component2.getColoredString());
								if (m > i) {
									return component2;
								}
							}
						}
					}

					return null;
				} else {
					return null;
				}
			} else {
				return null;
			}
		}
	}

	public static List<String> convertPages(CompoundTag compoundTag) {
		ListTag listTag = compoundTag.getList("pages", 8).copy();
		Builder<String> builder = ImmutableList.builder();

		for (int i = 0; i < listTag.size(); i++) {
			builder.add(listTag.getString(i));
		}

		return builder.build();
	}

	@Environment(EnvType.CLIENT)
	public interface BookAccess {
		int getPageCount();

		Component getPageRaw(int i);

		default Component getPage(int i) {
			return (Component)(i >= 0 && i < this.getPageCount() ? this.getPageRaw(i) : new TextComponent(""));
		}

		static BookViewScreen.BookAccess fromItem(ItemStack itemStack) {
			Item item = itemStack.getItem();
			if (item == Items.WRITTEN_BOOK) {
				return new BookViewScreen.WrittenBookAccess(itemStack);
			} else {
				return (BookViewScreen.BookAccess)(item == Items.WRITABLE_BOOK ? new BookViewScreen.WritableBookAccess(itemStack) : BookViewScreen.EMPTY_ACCESS);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public static class WritableBookAccess implements BookViewScreen.BookAccess {
		private final List<String> pages;

		public WritableBookAccess(ItemStack itemStack) {
			this.pages = readPages(itemStack);
		}

		private static List<String> readPages(ItemStack itemStack) {
			CompoundTag compoundTag = itemStack.getTag();
			return (List<String>)(compoundTag != null ? BookViewScreen.convertPages(compoundTag) : ImmutableList.of());
		}

		@Override
		public int getPageCount() {
			return this.pages.size();
		}

		@Override
		public Component getPageRaw(int i) {
			return new TextComponent((String)this.pages.get(i));
		}
	}

	@Environment(EnvType.CLIENT)
	public static class WrittenBookAccess implements BookViewScreen.BookAccess {
		private final List<String> pages;

		public WrittenBookAccess(ItemStack itemStack) {
			this.pages = readPages(itemStack);
		}

		private static List<String> readPages(ItemStack itemStack) {
			CompoundTag compoundTag = itemStack.getTag();
			return (List<String>)(compoundTag != null && WrittenBookItem.makeSureTagIsValid(compoundTag)
				? BookViewScreen.convertPages(compoundTag)
				: ImmutableList.of(new TranslatableComponent("book.invalid.tag").withStyle(ChatFormatting.DARK_RED).getColoredString()));
		}

		@Override
		public int getPageCount() {
			return this.pages.size();
		}

		@Override
		public Component getPageRaw(int i) {
			String string = (String)this.pages.get(i);

			try {
				Component component = Component.Serializer.fromJson(string);
				if (component != null) {
					return component;
				}
			} catch (Exception var4) {
			}

			return new TextComponent(string);
		}
	}
}
