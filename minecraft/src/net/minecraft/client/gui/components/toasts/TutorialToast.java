package net.minecraft.client.gui.components.toasts;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class TutorialToast implements Toast {
	private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("toast/tutorial");
	public static final int PROGRESS_BAR_WIDTH = 154;
	public static final int PROGRESS_BAR_HEIGHT = 1;
	public static final int PROGRESS_BAR_X = 3;
	public static final int PROGRESS_BAR_Y = 28;
	private final TutorialToast.Icons icon;
	private final Component title;
	@Nullable
	private final Component message;
	private Toast.Visibility visibility = Toast.Visibility.SHOW;
	private long lastSmoothingTime;
	private float smoothedProgress;
	private float progress;
	private final boolean progressable;
	private final int timeToDisplayMs;

	public TutorialToast(TutorialToast.Icons icons, Component component, @Nullable Component component2, boolean bl, int i) {
		this.icon = icons;
		this.title = component;
		this.message = component2;
		this.progressable = bl;
		this.timeToDisplayMs = i;
	}

	public TutorialToast(TutorialToast.Icons icons, Component component, @Nullable Component component2, boolean bl) {
		this(icons, component, component2, bl, 0);
	}

	@Override
	public Toast.Visibility getWantedVisibility() {
		return this.visibility;
	}

	@Override
	public void update(ToastManager toastManager, long l) {
		if (this.timeToDisplayMs > 0) {
			this.progress = Math.min((float)l / (float)this.timeToDisplayMs, 1.0F);
			this.smoothedProgress = this.progress;
			this.lastSmoothingTime = l;
			if (l > (long)this.timeToDisplayMs) {
				this.hide();
			}
		} else if (this.progressable) {
			this.smoothedProgress = Mth.clampedLerp(this.smoothedProgress, this.progress, (float)(l - this.lastSmoothingTime) / 100.0F);
			this.lastSmoothingTime = l;
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, Font font, long l) {
		guiGraphics.blitSprite(RenderType::guiTextured, BACKGROUND_SPRITE, 0, 0, this.width(), this.height());
		this.icon.render(guiGraphics, 6, 6);
		if (this.message == null) {
			guiGraphics.drawString(font, this.title, 30, 12, -11534256, false);
		} else {
			guiGraphics.drawString(font, this.title, 30, 7, -11534256, false);
			guiGraphics.drawString(font, this.message, 30, 18, -16777216, false);
		}

		if (this.progressable) {
			guiGraphics.fill(3, 28, 157, 29, -1);
			int i;
			if (this.progress >= this.smoothedProgress) {
				i = -16755456;
			} else {
				i = -11206656;
			}

			guiGraphics.fill(3, 28, (int)(3.0F + 154.0F * this.smoothedProgress), 29, i);
		}
	}

	public void hide() {
		this.visibility = Toast.Visibility.HIDE;
	}

	public void updateProgress(float f) {
		this.progress = f;
	}

	@Environment(EnvType.CLIENT)
	public static enum Icons {
		MOVEMENT_KEYS(ResourceLocation.withDefaultNamespace("toast/movement_keys")),
		MOUSE(ResourceLocation.withDefaultNamespace("toast/mouse")),
		TREE(ResourceLocation.withDefaultNamespace("toast/tree")),
		RECIPE_BOOK(ResourceLocation.withDefaultNamespace("toast/recipe_book")),
		WOODEN_PLANKS(ResourceLocation.withDefaultNamespace("toast/wooden_planks")),
		SOCIAL_INTERACTIONS(ResourceLocation.withDefaultNamespace("toast/social_interactions")),
		RIGHT_CLICK(ResourceLocation.withDefaultNamespace("toast/right_click"));

		private final ResourceLocation sprite;

		private Icons(final ResourceLocation resourceLocation) {
			this.sprite = resourceLocation;
		}

		public void render(GuiGraphics guiGraphics, int i, int j) {
			guiGraphics.blitSprite(RenderType::guiTextured, this.sprite, i, j, 20, 20);
		}
	}
}
