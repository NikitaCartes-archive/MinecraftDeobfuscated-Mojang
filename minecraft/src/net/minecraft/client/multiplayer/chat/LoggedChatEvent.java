package net.minecraft.client.multiplayer.chat;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.StringRepresentable;

@Environment(EnvType.CLIENT)
public interface LoggedChatEvent {
	Codec<LoggedChatEvent> CODEC = StringRepresentable.fromEnum(LoggedChatEvent.Type::values).dispatch(LoggedChatEvent::type, LoggedChatEvent.Type::codec);

	LoggedChatEvent.Type type();

	@Environment(EnvType.CLIENT)
	public static enum Type implements StringRepresentable {
		PLAYER("player", () -> LoggedChatMessage.Player.CODEC),
		SYSTEM("system", () -> LoggedChatMessage.System.CODEC);

		private final String serializedName;
		private final Supplier<Codec<? extends LoggedChatEvent>> codec;

		private Type(String string2, Supplier<Codec<? extends LoggedChatEvent>> supplier) {
			this.serializedName = string2;
			this.codec = supplier;
		}

		private Codec<? extends LoggedChatEvent> codec() {
			return (Codec<? extends LoggedChatEvent>)this.codec.get();
		}

		@Override
		public String getSerializedName() {
			return this.serializedName;
		}
	}
}
