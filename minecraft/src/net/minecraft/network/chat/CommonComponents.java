package net.minecraft.network.chat;

import java.util.Arrays;
import java.util.Collection;

public class CommonComponents {
	public static final Component EMPTY = Component.empty();
	public static final Component OPTION_ON = Component.translatable("options.on");
	public static final Component OPTION_OFF = Component.translatable("options.off");
	public static final Component GUI_DONE = Component.translatable("gui.done");
	public static final Component GUI_CANCEL = Component.translatable("gui.cancel");
	public static final Component GUI_YES = Component.translatable("gui.yes");
	public static final Component GUI_NO = Component.translatable("gui.no");
	public static final Component GUI_PROCEED = Component.translatable("gui.proceed");
	public static final Component GUI_BACK = Component.translatable("gui.back");
	public static final Component CONNECT_FAILED = Component.translatable("connect.failed");
	public static final Component NEW_LINE = Component.literal("\n");
	public static final Component NARRATION_SEPARATOR = Component.literal(". ");

	public static Component optionStatus(boolean bl) {
		return bl ? OPTION_ON : OPTION_OFF;
	}

	public static MutableComponent optionStatus(Component component, boolean bl) {
		return Component.translatable(bl ? "options.on.composed" : "options.off.composed", component);
	}

	public static MutableComponent optionNameValue(Component component, Component component2) {
		return Component.translatable("options.generic_value", component, component2);
	}

	public static MutableComponent joinForNarration(Component component, Component component2) {
		return Component.empty().append(component).append(NARRATION_SEPARATOR).append(component2);
	}

	public static Component joinLines(Component... components) {
		return joinLines(Arrays.asList(components));
	}

	public static Component joinLines(Collection<? extends Component> collection) {
		return ComponentUtils.formatList(collection, NEW_LINE);
	}
}
