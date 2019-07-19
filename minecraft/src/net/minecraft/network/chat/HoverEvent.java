package net.minecraft.network.chat;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class HoverEvent {
	private final HoverEvent.Action action;
	private final Component value;

	public HoverEvent(HoverEvent.Action action, Component component) {
		this.action = action;
		this.value = component;
	}

	public HoverEvent.Action getAction() {
		return this.action;
	}

	public Component getValue() {
		return this.value;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			HoverEvent hoverEvent = (HoverEvent)object;
			if (this.action != hoverEvent.action) {
				return false;
			} else {
				return this.value != null ? this.value.equals(hoverEvent.value) : hoverEvent.value == null;
			}
		} else {
			return false;
		}
	}

	public String toString() {
		return "HoverEvent{action=" + this.action + ", value='" + this.value + '\'' + '}';
	}

	public int hashCode() {
		int i = this.action.hashCode();
		return 31 * i + (this.value != null ? this.value.hashCode() : 0);
	}

	public static enum Action {
		SHOW_TEXT("show_text", true),
		SHOW_ITEM("show_item", true),
		SHOW_ENTITY("show_entity", true);

		private static final Map<String, HoverEvent.Action> LOOKUP = (Map<String, HoverEvent.Action>)Arrays.stream(values())
			.collect(Collectors.toMap(HoverEvent.Action::getName, action -> action));
		private final boolean allowFromServer;
		private final String name;

		private Action(String string2, boolean bl) {
			this.name = string2;
			this.allowFromServer = bl;
		}

		public boolean isAllowedFromServer() {
			return this.allowFromServer;
		}

		public String getName() {
			return this.name;
		}

		public static HoverEvent.Action getByName(String string) {
			return (HoverEvent.Action)LOOKUP.get(string);
		}
	}
}
