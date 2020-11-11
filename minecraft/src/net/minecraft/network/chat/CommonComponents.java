package net.minecraft.network.chat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class CommonComponents {
	public static final Component OPTION_ON = new TranslatableComponent("options.on");
	public static final Component OPTION_OFF = new TranslatableComponent("options.off");
	public static final Component GUI_DONE = new TranslatableComponent("gui.done");
	public static final Component GUI_CANCEL = new TranslatableComponent("gui.cancel");
	public static final Component GUI_YES = new TranslatableComponent("gui.yes");
	public static final Component GUI_NO = new TranslatableComponent("gui.no");
	public static final Component GUI_PROCEED = new TranslatableComponent("gui.proceed");
	public static final Component GUI_BACK = new TranslatableComponent("gui.back");
	public static final Component CONNECT_FAILED = new TranslatableComponent("connect.failed");

	public static MutableComponent optionStatus(Component component, boolean bl) {
		return new TranslatableComponent(bl ? "options.on.composed" : "options.off.composed", component);
	}

	public static MutableComponent optionNameValue(Component component, Component component2) {
		return new TranslatableComponent("options.generic_value", component, component2);
	}
}
