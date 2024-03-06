package net.minecraft.world.entity.ai.attributes;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import org.slf4j.Logger;

public record AttributeModifier(UUID id, String name, double amount, AttributeModifier.Operation operation) {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final MapCodec<AttributeModifier> MAP_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					UUIDUtil.CODEC.fieldOf("uuid").forGetter(AttributeModifier::id),
					Codec.STRING.fieldOf("name").forGetter(attributeModifier -> attributeModifier.name),
					Codec.DOUBLE.fieldOf("amount").forGetter(AttributeModifier::amount),
					AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(AttributeModifier::operation)
				)
				.apply(instance, AttributeModifier::new)
	);
	public static final Codec<AttributeModifier> CODEC = MAP_CODEC.codec();
	public static final StreamCodec<ByteBuf, AttributeModifier> STREAM_CODEC = StreamCodec.composite(
		UUIDUtil.STREAM_CODEC,
		AttributeModifier::id,
		ByteBufCodecs.STRING_UTF8,
		attributeModifier -> attributeModifier.name,
		ByteBufCodecs.DOUBLE,
		AttributeModifier::amount,
		AttributeModifier.Operation.STREAM_CODEC,
		AttributeModifier::operation,
		AttributeModifier::new
	);

	public AttributeModifier(String string, double d, AttributeModifier.Operation operation) {
		this(Mth.createInsecureUUID(RandomSource.createNewThreadLocalInstance()), string, d, operation);
	}

	public CompoundTag save() {
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.putString("Name", this.name);
		compoundTag.putDouble("Amount", this.amount);
		compoundTag.putInt("Operation", this.operation.id());
		compoundTag.putUUID("UUID", this.id);
		return compoundTag;
	}

	@Nullable
	public static AttributeModifier load(CompoundTag compoundTag) {
		try {
			UUID uUID = compoundTag.getUUID("UUID");
			AttributeModifier.Operation operation = (AttributeModifier.Operation)AttributeModifier.Operation.BY_ID.apply(compoundTag.getInt("Operation"));
			return new AttributeModifier(uUID, compoundTag.getString("Name"), compoundTag.getDouble("Amount"), operation);
		} catch (Exception var3) {
			LOGGER.warn("Unable to create attribute: {}", var3.getMessage());
			return null;
		}
	}

	public static enum Operation implements StringRepresentable {
		ADD_VALUE("add_value", 0),
		ADD_MULTIPLIED_BASE("add_multiplied_base", 1),
		ADD_MULTIPLIED_TOTAL("add_multiplied_total", 2);

		public static final IntFunction<AttributeModifier.Operation> BY_ID = ByIdMap.continuous(
			AttributeModifier.Operation::id, values(), ByIdMap.OutOfBoundsStrategy.ZERO
		);
		public static final StreamCodec<ByteBuf, AttributeModifier.Operation> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, AttributeModifier.Operation::id);
		public static final Codec<AttributeModifier.Operation> CODEC = StringRepresentable.fromEnum(AttributeModifier.Operation::values);
		private final String name;
		private final int id;

		private Operation(String string2, int j) {
			this.name = string2;
			this.id = j;
		}

		public int id() {
			return this.id;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
