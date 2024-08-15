package net.minecraft.client.gui.components.toasts;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

@Environment(EnvType.CLIENT)
public class RecipeToast implements Toast {
	private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("toast/recipe");
	private static final long DISPLAY_TIME = 5000L;
	private static final Component TITLE_TEXT = Component.translatable("recipe.toast.title");
	private static final Component DESCRIPTION_TEXT = Component.translatable("recipe.toast.description");
	private final List<RecipeToast.RecipeDisplayItems> recipeItems = Lists.<RecipeToast.RecipeDisplayItems>newArrayList();
	private long lastChanged;
	private boolean changed;
	private Toast.Visibility wantedVisibility = Toast.Visibility.HIDE;
	private int displayedRecipeIndex;

	public RecipeToast(ItemStack itemStack, ItemStack itemStack2) {
		this.recipeItems.add(new RecipeToast.RecipeDisplayItems(itemStack, itemStack2));
	}

	@Override
	public Toast.Visibility getWantedVisibility() {
		return this.wantedVisibility;
	}

	@Override
	public void update(ToastManager toastManager, long l) {
		if (this.changed) {
			this.lastChanged = l;
			this.changed = false;
		}

		if (this.recipeItems.isEmpty()) {
			this.wantedVisibility = Toast.Visibility.HIDE;
		} else {
			this.wantedVisibility = (double)(l - this.lastChanged) >= 5000.0 * toastManager.getNotificationDisplayTimeMultiplier()
				? Toast.Visibility.HIDE
				: Toast.Visibility.SHOW;
		}

		this.displayedRecipeIndex = (int)(
			(double)l / Math.max(1.0, 5000.0 * toastManager.getNotificationDisplayTimeMultiplier() / (double)this.recipeItems.size()) % (double)this.recipeItems.size()
		);
	}

	@Override
	public void render(GuiGraphics guiGraphics, Font font, long l) {
		guiGraphics.blitSprite(RenderType::guiTextured, BACKGROUND_SPRITE, 0, 0, this.width(), this.height());
		guiGraphics.drawString(font, TITLE_TEXT, 30, 7, -11534256, false);
		guiGraphics.drawString(font, DESCRIPTION_TEXT, 30, 18, -16777216, false);
		RecipeToast.RecipeDisplayItems recipeDisplayItems = (RecipeToast.RecipeDisplayItems)this.recipeItems.get(this.displayedRecipeIndex);
		guiGraphics.pose().pushPose();
		guiGraphics.pose().scale(0.6F, 0.6F, 1.0F);
		guiGraphics.renderFakeItem(recipeDisplayItems.categoryItem(), 3, 3);
		guiGraphics.pose().popPose();
		guiGraphics.renderFakeItem(recipeDisplayItems.unlockedItem(), 8, 8);
	}

	private void addItem(ItemStack itemStack, ItemStack itemStack2) {
		this.recipeItems.add(new RecipeToast.RecipeDisplayItems(itemStack, itemStack2));
		this.changed = true;
	}

	public static void addOrUpdate(ToastManager toastManager, RecipeHolder<?> recipeHolder) {
		RecipeToast recipeToast = toastManager.getToast(RecipeToast.class, NO_TOKEN);
		ItemStack itemStack = recipeHolder.value().getCategoryIconItem();
		ItemStack itemStack2 = recipeHolder.value().getResultItem(toastManager.getMinecraft().level.registryAccess());
		if (recipeToast == null) {
			toastManager.addToast(new RecipeToast(itemStack, itemStack2));
		} else {
			recipeToast.addItem(itemStack, itemStack2);
		}
	}

	@Environment(EnvType.CLIENT)
	static record RecipeDisplayItems(ItemStack categoryItem, ItemStack unlockedItem) {
	}
}
