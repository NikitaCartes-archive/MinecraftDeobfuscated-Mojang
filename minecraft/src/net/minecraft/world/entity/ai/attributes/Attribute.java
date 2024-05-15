package net.minecraft.world.entity.ai.attributes;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class Attribute {
	public static final Codec<Holder<Attribute>> CODEC = BuiltInRegistries.ATTRIBUTE.holderByNameCodec();
	public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Attribute>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.ATTRIBUTE);
	private final double defaultValue;
	private boolean syncable;
	private final String descriptionId;
	private Attribute.Sentiment sentiment = Attribute.Sentiment.POSITIVE;

	protected Attribute(String string, double d) {
		this.defaultValue = d;
		this.descriptionId = string;
	}

	public double getDefaultValue() {
		return this.defaultValue;
	}

	public boolean isClientSyncable() {
		return this.syncable;
	}

	public Attribute setSyncable(boolean bl) {
		this.syncable = bl;
		return this;
	}

	public Attribute setSentiment(Attribute.Sentiment sentiment) {
		this.sentiment = sentiment;
		return this;
	}

	public double sanitizeValue(double d) {
		return d;
	}

	public String getDescriptionId() {
		return this.descriptionId;
	}

	public ChatFormatting getStyle(boolean bl) {
		return this.sentiment.getStyle(bl);
	}

	public static enum Sentiment {
		POSITIVE,
		NEUTRAL,
		NEGATIVE;

		public ChatFormatting getStyle(boolean bl) {
			return switch (this) {
				case POSITIVE -> bl ? ChatFormatting.BLUE : ChatFormatting.RED;
				case NEUTRAL -> ChatFormatting.GRAY;
				case NEGATIVE -> bl ? ChatFormatting.RED : ChatFormatting.BLUE;
			};
		}
	}
}
