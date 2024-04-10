package net.minecraft.world.entity.ai.gossip;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum GossipType implements StringRepresentable {
	MAJOR_NEGATIVE("major_negative", -5, 100, 10, 10),
	MINOR_NEGATIVE("minor_negative", -1, 200, 20, 20),
	MINOR_POSITIVE("minor_positive", 1, 25, 1, 5),
	MAJOR_POSITIVE("major_positive", 5, 20, 0, 20),
	TRADING("trading", 1, 25, 2, 20);

	public static final int REPUTATION_CHANGE_PER_EVENT = 25;
	public static final int REPUTATION_CHANGE_PER_EVERLASTING_MEMORY = 20;
	public static final int REPUTATION_CHANGE_PER_TRADE = 2;
	public final String id;
	public final int weight;
	public final int max;
	public final int decayPerDay;
	public final int decayPerTransfer;
	public static final Codec<GossipType> CODEC = StringRepresentable.fromEnum(GossipType::values);

	private GossipType(final String string2, final int j, final int k, final int l, final int m) {
		this.id = string2;
		this.weight = j;
		this.max = k;
		this.decayPerDay = l;
		this.decayPerTransfer = m;
	}

	@Override
	public String getSerializedName() {
		return this.id;
	}
}
