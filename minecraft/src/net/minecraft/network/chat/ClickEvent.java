package net.minecraft.network.chat;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ClickEvent {
	private final ClickEvent.Action action;
	private final String value;

	public ClickEvent(ClickEvent.Action action, String string) {
		this.action = action;
		this.value = string;
	}

	public ClickEvent.Action getAction() {
		return this.action;
	}

	public String getValue() {
		return this.value;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			ClickEvent clickEvent = (ClickEvent)object;
			if (this.action != clickEvent.action) {
				return false;
			} else {
				return this.value != null ? this.value.equals(clickEvent.value) : clickEvent.value == null;
			}
		} else {
			return false;
		}
	}

	public String toString() {
		return "ClickEvent{action=" + this.action + ", value='" + this.value + '\'' + '}';
	}

	public int hashCode() {
		int i = this.action.hashCode();
		return 31 * i + (this.value != null ? this.value.hashCode() : 0);
	}

	public static enum Action {
		OPEN_URL("open_url", true),
		OPEN_FILE("open_file", false),
		RUN_COMMAND("run_command", true),
		SUGGEST_COMMAND("suggest_command", true),
		CHANGE_PAGE("change_page", true),
		COPY_TO_CLIPBOARD("copy_to_clipboard", true);

		private static final Map<String, ClickEvent.Action> LOOKUP = (Map<String, ClickEvent.Action>)Arrays.stream(values())
			.collect(Collectors.toMap(ClickEvent.Action::getName, action -> action));
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

		public static ClickEvent.Action getByName(String string) {
			return (ClickEvent.Action)LOOKUP.get(string);
		}
	}
}
