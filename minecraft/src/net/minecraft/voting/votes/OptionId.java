package net.minecraft.voting.votes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Comparator;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;

public record OptionId(UUID voteId, int index) {
	public static final Comparator<OptionId> COMPARATOR = Comparator.comparing(OptionId::voteId).thenComparingInt(OptionId::index);
	public static final Codec<OptionId> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(UUIDUtil.STRING_CODEC.fieldOf("uuid").forGetter(OptionId::voteId), Codec.INT.fieldOf("index").forGetter(OptionId::index))
				.apply(instance, OptionId::new)
	);
	public static final Codec<OptionId> STRING_CODEC = Codec.STRING.comapFlatMap(string -> {
		int i = string.indexOf(58);
		if (i == -1) {
			return DataResult.error(() -> "No separator in " + string);
		} else {
			String string2 = string.substring(0, i);

			UUID uUID;
			try {
				uUID = UUID.fromString(string2);
			} catch (Exception var8) {
				return DataResult.error(() -> "Invalid UUID " + string2 + ": " + var8.getMessage());
			}

			String string3 = string.substring(i + 1);

			int j;
			try {
				j = Integer.parseInt(string3);
			} catch (Exception var7) {
				return DataResult.error(() -> "Invalid index " + string3 + ": " + var7.getMessage());
			}

			return DataResult.success(new OptionId(uUID, j));
		}
	}, optionId -> optionId.voteId + ":" + optionId.index);
	public static final FriendlyByteBuf.Reader<OptionId> READER = friendlyByteBuf -> {
		UUID uUID = friendlyByteBuf.readUUID();
		int i = friendlyByteBuf.readVarInt();
		return new OptionId(uUID, i);
	};
	public static final FriendlyByteBuf.Writer<OptionId> WRITER = (friendlyByteBuf, optionId) -> {
		friendlyByteBuf.writeUUID(optionId.voteId);
		friendlyByteBuf.writeVarInt(optionId.index);
	};

	public String toString() {
		return this.voteId + ":" + this.index;
	}
}
