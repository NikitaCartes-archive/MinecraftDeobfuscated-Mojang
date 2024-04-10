package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;

public class ClickEvent {
	public static final Codec<ClickEvent> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ClickEvent.Action.CODEC.forGetter(clickEvent -> clickEvent.action), Codec.STRING.fieldOf("value").forGetter(clickEvent -> clickEvent.value)
				)
				.apply(instance, ClickEvent::new)
	);
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
			return this.action == clickEvent.action && this.value.equals(clickEvent.value);
		} else {
			return false;
		}
	}

	public String toString() {
		return "ClickEvent{action=" + this.action + ", value='" + this.value + "'}";
	}

	public int hashCode() {
		int i = this.action.hashCode();
		return 31 * i + this.value.hashCode();
	}

	public static enum Action implements StringRepresentable {
		OPEN_URL("open_url", true),
		OPEN_FILE("open_file", false),
		RUN_COMMAND("run_command", true),
		SUGGEST_COMMAND("suggest_command", true),
		CHANGE_PAGE("change_page", true),
		COPY_TO_CLIPBOARD("copy_to_clipboard", true);

		public static final MapCodec<ClickEvent.Action> UNSAFE_CODEC = StringRepresentable.fromEnum(ClickEvent.Action::values).fieldOf("action");
		public static final MapCodec<ClickEvent.Action> CODEC = UNSAFE_CODEC.validate(ClickEvent.Action::filterForSerialization);
		private final boolean allowFromServer;
		private final String name;

		private Action(final String string2, final boolean bl) {
			this.name = string2;
			this.allowFromServer = bl;
		}

		public boolean isAllowedFromServer() {
			return this.allowFromServer;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public static DataResult<ClickEvent.Action> filterForSerialization(ClickEvent.Action action) {
			return !action.isAllowedFromServer() ? DataResult.error(() -> "Action not allowed: " + action) : DataResult.success(action, Lifecycle.stable());
		}
	}
}
