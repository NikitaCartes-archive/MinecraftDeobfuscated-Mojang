package net.minecraft.client.gui.components.toasts;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class TutorialToast implements Toast {
	private static final ResourceLocation BACKGROUND_SPRITE = new ResourceLocation("toast/tutorial");
	public static final int PROGRESS_BAR_WIDTH = 154;
	public static final int PROGRESS_BAR_HEIGHT = 1;
	public static final int PROGRESS_BAR_X = 3;
	public static final int PROGRESS_BAR_Y = 28;
	private final TutorialToast.Icons icon;
	private final Component title;
	@Nullable
	private final Component message;
	private Toast.Visibility visibility = Toast.Visibility.SHOW;
	private long lastProgressTime;
	private float lastProgress;
	private float progress;
	private final boolean progressable;

	public TutorialToast(TutorialToast.Icons icons, Component component, @Nullable Component component2, boolean bl) {
		this.icon = icons;
		this.title = component;
		this.message = component2;
		this.progressable = bl;
	}

	@Override
	public Toast.Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long l) {
		guiGraphics.blitSprite(BACKGROUND_SPRITE, 0, 0, this.width(), this.height());
		this.icon.render(guiGraphics, 6, 6);
		if (this.message == null) {
			guiGraphics.drawString(toastComponent.getMinecraft().font, this.title, 30, 12, -11534256, false);
		} else {
			guiGraphics.drawString(toastComponent.getMinecraft().font, this.title, 30, 7, -11534256, false);
			guiGraphics.drawString(toastComponent.getMinecraft().font, this.message, 30, 18, -16777216, false);
		}

		if (this.progressable) {
			guiGraphics.fill(3, 28, 157, 29, -1);
			float f = Mth.clampedLerp(this.lastProgress, this.progress, (float)(l - this.lastProgressTime) / 100.0F);
			int i;
			if (this.progress >= this.lastProgress) {
				i = -16755456;
			} else {
				i = -11206656;
			}

			guiGraphics.fill(3, 28, (int)(3.0F + 154.0F * f), 29, i);
			this.lastProgress = f;
			this.lastProgressTime = l;
		}

		return this.visibility;
	}

	public void hide() {
		this.visibility = Toast.Visibility.HIDE;
	}

	public void updateProgress(float f) {
		this.progress = f;
	}

	@Environment(EnvType.CLIENT)
	public static enum Icons {
		MOVEMENT_KEYS(new ResourceLocation("toast/movement_keys")),
		MOUSE(new ResourceLocation("toast/mouse")),
		TREE(new ResourceLocation("toast/tree")),
		RECIPE_BOOK(new ResourceLocation("toast/recipe_book")),
		WOODEN_PLANKS(new ResourceLocation("toast/wooden_planks")),
		SOCIAL_INTERACTIONS(new ResourceLocation("toast/social_interactions")),
		RIGHT_CLICK(new ResourceLocation("toast/right_click"));

		private final ResourceLocation sprite;

		private Icons(ResourceLocation resourceLocation) {
			this.sprite = resourceLocation;
		}

		public void render(GuiGraphics guiGraphics, int i, int j) {
			RenderSystem.enableBlend();
			guiGraphics.blitSprite(this.sprite, i, j, 20, 20);
		}
	}
}
