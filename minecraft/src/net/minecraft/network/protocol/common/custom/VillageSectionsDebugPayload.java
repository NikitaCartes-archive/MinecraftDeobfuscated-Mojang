package net.minecraft.network.protocol.common.custom;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record VillageSectionsDebugPayload(Set<SectionPos> villageChunks, Set<SectionPos> notVillageChunks) implements CustomPacketPayload {
	public static final StreamCodec<FriendlyByteBuf, VillageSectionsDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(
		VillageSectionsDebugPayload::write, VillageSectionsDebugPayload::new
	);
	public static final CustomPacketPayload.Type<VillageSectionsDebugPayload> TYPE = CustomPacketPayload.createType("debug/village_sections");

	private VillageSectionsDebugPayload(FriendlyByteBuf friendlyByteBuf) {
		this(
			friendlyByteBuf.readCollection(HashSet::new, FriendlyByteBuf::readSectionPos), friendlyByteBuf.readCollection(HashSet::new, FriendlyByteBuf::readSectionPos)
		);
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeCollection(this.villageChunks, FriendlyByteBuf::writeSectionPos);
		friendlyByteBuf.writeCollection(this.notVillageChunks, FriendlyByteBuf::writeSectionPos);
	}

	@Override
	public CustomPacketPayload.Type<VillageSectionsDebugPayload> type() {
		return TYPE;
	}
}
