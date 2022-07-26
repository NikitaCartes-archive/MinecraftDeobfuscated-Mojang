package net.minecraft.client.multiplayer.chat;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Collection;
import java.util.Objects;
import java.util.Spliterators;
import java.util.PrimitiveIterator.OfInt;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface ChatLog {
	int NO_MESSAGE = -1;

	void push(LoggedChatEvent loggedChatEvent);

	@Nullable
	LoggedChatEvent lookup(int i);

	@Nullable
	default ChatLog.Entry<LoggedChatEvent> lookupEntry(int i) {
		LoggedChatEvent loggedChatEvent = this.lookup(i);
		return loggedChatEvent != null ? new ChatLog.Entry<>(i, loggedChatEvent) : null;
	}

	default boolean contains(int i) {
		return this.lookup(i) != null;
	}

	int offset(int i, int j);

	default int before(int i) {
		return this.offset(i, -1);
	}

	default int after(int i) {
		return this.offset(i, 1);
	}

	int newest();

	int oldest();

	default ChatLog.Selection selectAll() {
		return this.selectAfter(this.oldest());
	}

	default ChatLog.Selection selectAllDescending() {
		return this.selectBefore(this.newest());
	}

	default ChatLog.Selection selectAfter(int i) {
		return this.selectSequence(i, this::after);
	}

	default ChatLog.Selection selectBefore(int i) {
		return this.selectSequence(i, this::before);
	}

	default ChatLog.Selection selectBetween(int i, int j) {
		return this.contains(i) && this.contains(j) ? this.selectSequence(i, jx -> jx == j ? -1 : this.after(jx)) : this.selectNone();
	}

	default ChatLog.Selection selectSequence(int i, IntUnaryOperator intUnaryOperator) {
		return !this.contains(i) ? this.selectNone() : new ChatLog.Selection(this, new OfInt() {
			private int nextId = i;

			public int nextInt() {
				int i = this.nextId;
				this.nextId = intUnaryOperator.applyAsInt(i);
				return i;
			}

			public boolean hasNext() {
				return this.nextId != -1;
			}
		});
	}

	private ChatLog.Selection selectNone() {
		return new ChatLog.Selection(this, IntList.of().iterator());
	}

	@Environment(EnvType.CLIENT)
	public static record Entry<T extends LoggedChatEvent>(int id, T event) {
		@Nullable
		public <U extends LoggedChatEvent> ChatLog.Entry<U> tryCast(Class<U> class_) {
			return class_.isInstance(this.event) ? new ChatLog.Entry<>(this.id, (U)class_.cast(this.event)) : null;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Selection {
		private static final int CHARACTERISTICS = 1041;
		private final ChatLog log;
		private final OfInt ids;

		Selection(ChatLog chatLog, OfInt ofInt) {
			this.log = chatLog;
			this.ids = ofInt;
		}

		public IntStream ids() {
			return StreamSupport.intStream(Spliterators.spliteratorUnknownSize(this.ids, 1041), false);
		}

		public Stream<LoggedChatEvent> events() {
			return this.ids().mapToObj(this.log::lookup).filter(Objects::nonNull);
		}

		public Collection<GameProfile> reportableGameProfiles() {
			return this.events().map(loggedChatEvent -> {
				if (loggedChatEvent instanceof LoggedChatMessage.Player player && player.canReport(player.profile().getId())) {
					return player.profile();
				}

				return null;
			}).filter(Objects::nonNull).distinct().toList();
		}

		public Stream<ChatLog.Entry<LoggedChatEvent>> entries() {
			return this.ids().mapToObj(this.log::lookupEntry).filter(Objects::nonNull);
		}
	}
}
