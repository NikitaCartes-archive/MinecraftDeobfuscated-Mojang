package net.minecraft.voting.votes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;

public record CommonVoteData(Component displayName, long start, long duration, List<VotingMaterial.Cost> cost) {
	public static final MapCodec<CommonVoteData> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					ExtraCodecs.COMPONENT.fieldOf("display_name").forGetter(CommonVoteData::displayName),
					Codec.LONG.fieldOf("start").forGetter(CommonVoteData::start),
					Codec.LONG.fieldOf("duration").forGetter(CommonVoteData::duration),
					VotingMaterial.Cost.CODEC.listOf().fieldOf("cost").forGetter(CommonVoteData::cost)
				)
				.apply(instance, CommonVoteData::new)
	);

	public long end() {
		return this.start + this.duration;
	}
}
