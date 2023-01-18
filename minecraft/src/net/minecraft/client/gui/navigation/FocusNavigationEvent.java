package net.minecraft.client.gui.navigation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface FocusNavigationEvent {
	ScreenDirection getVerticalDirectionForInitialFocus();

	@Environment(EnvType.CLIENT)
	public static record ArrowNavigation(ScreenDirection direction) implements FocusNavigationEvent {
		@Override
		public ScreenDirection getVerticalDirectionForInitialFocus() {
			return this.direction.getAxis() == ScreenAxis.VERTICAL ? this.direction : ScreenDirection.DOWN;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class InitialFocus implements FocusNavigationEvent {
		@Override
		public ScreenDirection getVerticalDirectionForInitialFocus() {
			return ScreenDirection.DOWN;
		}
	}

	@Environment(EnvType.CLIENT)
	public static record TabNavigation(boolean forward) implements FocusNavigationEvent {
		@Override
		public ScreenDirection getVerticalDirectionForInitialFocus() {
			return this.forward ? ScreenDirection.DOWN : ScreenDirection.UP;
		}
	}
}
