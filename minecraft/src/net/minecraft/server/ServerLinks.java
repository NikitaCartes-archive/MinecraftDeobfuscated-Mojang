package net.minecraft.server;

import com.mojang.datafixers.util.Either;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

public record ServerLinks(List<ServerLinks.Entry> entries) {
	public static final ServerLinks EMPTY = new ServerLinks(List.of());
	public static final StreamCodec<ByteBuf, ServerLinks> STREAM_CODEC = StreamCodec.composite(
		ServerLinks.Entry.STREAM_CODEC.apply(ByteBufCodecs.list()), ServerLinks::entries, ServerLinks::new
	);

	public boolean isEmpty() {
		return this.entries.isEmpty();
	}

	public Optional<ServerLinks.Entry> findKnownType(ServerLinks.KnownLinkType knownLinkType) {
		return this.entries.stream().filter(entry -> entry.type.<Boolean>map(knownLinkType2 -> knownLinkType2 == knownLinkType, component -> false)).findFirst();
	}

	public static record Entry(Either<ServerLinks.KnownLinkType, Component> type, String url) {
		public static final StreamCodec<ByteBuf, Either<ServerLinks.KnownLinkType, Component>> TYPE_STREAM_CODEC = ByteBufCodecs.either(
			ServerLinks.KnownLinkType.STREAM_CODEC, ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC
		);
		public static final StreamCodec<ByteBuf, ServerLinks.Entry> STREAM_CODEC = StreamCodec.composite(
			TYPE_STREAM_CODEC, ServerLinks.Entry::type, ByteBufCodecs.STRING_UTF8, ServerLinks.Entry::url, ServerLinks.Entry::new
		);

		public static ServerLinks.Entry knownType(ServerLinks.KnownLinkType knownLinkType, String string) {
			return new ServerLinks.Entry(Either.left(knownLinkType), string);
		}

		public static ServerLinks.Entry custom(Component component, String string) {
			return new ServerLinks.Entry(Either.right(component), string);
		}

		public Component displayName() {
			return this.type.map(ServerLinks.KnownLinkType::displayName, component -> component);
		}
	}

	public static enum KnownLinkType {
		BUG_REPORT(0, "report_bug");

		private static final IntFunction<ServerLinks.KnownLinkType> BY_ID = ByIdMap.continuous(
			knownLinkType -> knownLinkType.id, values(), ByIdMap.OutOfBoundsStrategy.ZERO
		);
		public static final StreamCodec<ByteBuf, ServerLinks.KnownLinkType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, knownLinkType -> knownLinkType.id);
		private final int id;
		private final String name;

		private KnownLinkType(final int j, final String string2) {
			this.id = j;
			this.name = string2;
		}

		private Component displayName() {
			return Component.translatable("known_server_link." + this.name);
		}

		public ServerLinks.Entry create(String string) {
			return ServerLinks.Entry.knownType(this, string);
		}
	}
}
