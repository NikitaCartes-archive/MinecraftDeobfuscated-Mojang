package net.minecraft.client.gui.components.toasts;

import com.google.common.collect.Queues;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class ToastManager {
	private static final int SLOT_COUNT = 5;
	private static final int ALL_SLOTS_OCCUPIED = -1;
	final Minecraft minecraft;
	private final List<ToastManager.ToastInstance<?>> visibleToasts = new ArrayList();
	private final BitSet occupiedSlots = new BitSet(5);
	private final Deque<Toast> queued = Queues.<Toast>newArrayDeque();

	public ToastManager(Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	public void update() {
		this.visibleToasts.removeIf(toastInstance -> {
			toastInstance.update();
			if (toastInstance.hasFinishedRendering()) {
				this.occupiedSlots.clear(toastInstance.firstSlotIndex, toastInstance.firstSlotIndex + toastInstance.occupiedSlotCount);
				return true;
			} else {
				return false;
			}
		});
		if (!this.queued.isEmpty() && this.freeSlotCount() > 0) {
			this.queued.removeIf(toast -> {
				int i = toast.occcupiedSlotCount();
				int j = this.findFreeSlotsIndex(i);
				if (j == -1) {
					return false;
				} else {
					this.visibleToasts.add(new ToastManager.ToastInstance<>(toast, j, i));
					this.occupiedSlots.set(j, j + i);
					return true;
				}
			});
		}
	}

	public void render(GuiGraphics guiGraphics) {
		if (!this.minecraft.options.hideGui) {
			int i = guiGraphics.guiWidth();

			for (ToastManager.ToastInstance<?> toastInstance : this.visibleToasts) {
				toastInstance.render(guiGraphics, i);
			}
		}
	}

	private int findFreeSlotsIndex(int i) {
		if (this.freeSlotCount() >= i) {
			int j = 0;

			for (int k = 0; k < 5; k++) {
				if (this.occupiedSlots.get(k)) {
					j = 0;
				} else if (++j == i) {
					return k + 1 - j;
				}
			}
		}

		return -1;
	}

	private int freeSlotCount() {
		return 5 - this.occupiedSlots.cardinality();
	}

	@Nullable
	public <T extends Toast> T getToast(Class<? extends T> class_, Object object) {
		for (ToastManager.ToastInstance<?> toastInstance : this.visibleToasts) {
			if (toastInstance != null && class_.isAssignableFrom(toastInstance.getToast().getClass()) && toastInstance.getToast().getToken().equals(object)) {
				return (T)toastInstance.getToast();
			}
		}

		for (Toast toast : this.queued) {
			if (class_.isAssignableFrom(toast.getClass()) && toast.getToken().equals(object)) {
				return (T)toast;
			}
		}

		return null;
	}

	public void clear() {
		this.occupiedSlots.clear();
		this.visibleToasts.clear();
		this.queued.clear();
	}

	public void addToast(Toast toast) {
		this.queued.add(toast);
	}

	public Minecraft getMinecraft() {
		return this.minecraft;
	}

	public double getNotificationDisplayTimeMultiplier() {
		return this.minecraft.options.notificationDisplayTime().get();
	}

	@Environment(EnvType.CLIENT)
	class ToastInstance<T extends Toast> {
		private static final long SLIDE_ANIMATION_DURATION_MS = 600L;
		private final T toast;
		final int firstSlotIndex;
		final int occupiedSlotCount;
		private long animationStartTime = -1L;
		private long becameFullyVisibleAt = -1L;
		private Toast.Visibility visibility = Toast.Visibility.SHOW;
		private long fullyVisibleFor;
		private float visiblePortion;
		private boolean hasFinishedRendering;

		ToastInstance(final T toast, final int i, final int j) {
			this.toast = toast;
			this.firstSlotIndex = i;
			this.occupiedSlotCount = j;
		}

		public T getToast() {
			return this.toast;
		}

		public boolean hasFinishedRendering() {
			return this.hasFinishedRendering;
		}

		private void calculateVisiblePortion(long l) {
			float f = Mth.clamp((float)(l - this.animationStartTime) / 600.0F, 0.0F, 1.0F);
			f *= f;
			if (this.visibility == Toast.Visibility.HIDE) {
				this.visiblePortion = 1.0F - f;
			} else {
				this.visiblePortion = f;
			}
		}

		public void update() {
			long l = Util.getMillis();
			if (this.animationStartTime == -1L) {
				this.animationStartTime = l;
				this.visibility.playSound(ToastManager.this.minecraft.getSoundManager());
			}

			if (this.visibility == Toast.Visibility.SHOW && l - this.animationStartTime <= 600L) {
				this.becameFullyVisibleAt = l;
			}

			this.fullyVisibleFor = l - this.becameFullyVisibleAt;
			this.calculateVisiblePortion(l);
			this.toast.update(ToastManager.this, this.fullyVisibleFor);
			Toast.Visibility visibility = this.toast.getWantedVisibility();
			if (visibility != this.visibility) {
				this.animationStartTime = l - (long)((int)((1.0F - this.visiblePortion) * 600.0F));
				this.visibility = visibility;
				this.visibility.playSound(ToastManager.this.minecraft.getSoundManager());
			}

			this.hasFinishedRendering = this.visibility == Toast.Visibility.HIDE && l - this.animationStartTime > 600L;
		}

		public void render(GuiGraphics guiGraphics, int i) {
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate((float)i - (float)this.toast.width() * this.visiblePortion, (float)(this.firstSlotIndex * 32), 800.0F);
			this.toast.render(guiGraphics, ToastManager.this.minecraft.font, this.fullyVisibleFor);
			guiGraphics.pose().popPose();
		}
	}
}
