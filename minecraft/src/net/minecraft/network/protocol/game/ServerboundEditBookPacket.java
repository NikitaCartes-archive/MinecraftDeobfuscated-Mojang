package net.minecraft.network.protocol.game;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundEditBookPacket implements Packet<ServerGamePacketListener> {
	public static final int MAX_BYTES_PER_CHAR = 4;
	private static final int TITLE_MAX_CHARS = 128;
	private static final int PAGE_MAX_CHARS = 8192;
	private static final int MAX_PAGES_COUNT = 200;
	private final int slot;
	private final List<String> pages;
	private final Optional<String> title;

	public ServerboundEditBookPacket(int i, List<String> list, Optional<String> optional) {
		this.slot = i;
		this.pages = ImmutableList.copyOf(list);
		this.title = optional;
	}

	public ServerboundEditBookPacket(FriendlyByteBuf friendlyByteBuf) {
		this.slot = friendlyByteBuf.readVarInt();
		this.pages = friendlyByteBuf.readCollection(
			FriendlyByteBuf.limitValue(Lists::newArrayListWithCapacity, 200), friendlyByteBufx -> friendlyByteBufx.readUtf(8192)
		);
		this.title = friendlyByteBuf.readOptional(friendlyByteBufx -> friendlyByteBufx.readUtf(128));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.slot);
		friendlyByteBuf.writeCollection(this.pages, (friendlyByteBufx, string) -> friendlyByteBufx.writeUtf(string, 8192));
		friendlyByteBuf.writeOptional(this.title, (friendlyByteBufx, string) -> friendlyByteBufx.writeUtf(string, 128));
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleEditBook(this);
	}

	public List<String> getPages() {
		return this.pages;
	}

	public Optional<String> getTitle() {
		return this.title;
	}

	public int getSlot() {
		return this.slot;
	}
}
