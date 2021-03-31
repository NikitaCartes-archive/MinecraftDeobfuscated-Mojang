package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;

public class ClientboundSectionBlocksUpdatePacket implements Packet<ClientGamePacketListener> {
	private static final int POS_IN_SECTION_BITS = 12;
	private final SectionPos sectionPos;
	private final short[] positions;
	private final BlockState[] states;
	private final boolean suppressLightUpdates;

	public ClientboundSectionBlocksUpdatePacket(SectionPos sectionPos, ShortSet shortSet, LevelChunkSection levelChunkSection, boolean bl) {
		this.sectionPos = sectionPos;
		this.suppressLightUpdates = bl;
		int i = shortSet.size();
		this.positions = new short[i];
		this.states = new BlockState[i];
		int j = 0;

		for (short s : shortSet) {
			this.positions[j] = s;
			this.states[j] = levelChunkSection.getBlockState(SectionPos.sectionRelativeX(s), SectionPos.sectionRelativeY(s), SectionPos.sectionRelativeZ(s));
			j++;
		}
	}

	public ClientboundSectionBlocksUpdatePacket(FriendlyByteBuf friendlyByteBuf) {
		this.sectionPos = SectionPos.of(friendlyByteBuf.readLong());
		this.suppressLightUpdates = friendlyByteBuf.readBoolean();
		int i = friendlyByteBuf.readVarInt();
		this.positions = new short[i];
		this.states = new BlockState[i];

		for (int j = 0; j < i; j++) {
			long l = friendlyByteBuf.readVarLong();
			this.positions[j] = (short)((int)(l & 4095L));
			this.states[j] = Block.BLOCK_STATE_REGISTRY.byId((int)(l >>> 12));
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeLong(this.sectionPos.asLong());
		friendlyByteBuf.writeBoolean(this.suppressLightUpdates);
		friendlyByteBuf.writeVarInt(this.positions.length);

		for (int i = 0; i < this.positions.length; i++) {
			friendlyByteBuf.writeVarLong((long)(Block.getId(this.states[i]) << 12 | this.positions[i]));
		}
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleChunkBlocksUpdate(this);
	}

	public void runUpdates(BiConsumer<BlockPos, BlockState> biConsumer) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int i = 0; i < this.positions.length; i++) {
			short s = this.positions[i];
			mutableBlockPos.set(this.sectionPos.relativeToBlockX(s), this.sectionPos.relativeToBlockY(s), this.sectionPos.relativeToBlockZ(s));
			biConsumer.accept(mutableBlockPos, this.states[i]);
		}
	}

	public boolean shouldSuppressLightUpdates() {
		return this.suppressLightUpdates;
	}
}
