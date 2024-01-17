package net.minecraft.world.entity.ai.attributes;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
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

public class AttributeModifier {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final Codec<AttributeModifier> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					UUIDUtil.CODEC.fieldOf("UUID").forGetter(AttributeModifier::getId),
					Codec.STRING.fieldOf("Name").forGetter(attributeModifier -> attributeModifier.name),
					Codec.DOUBLE.fieldOf("Amount").forGetter(AttributeModifier::getAmount),
					AttributeModifier.Operation.CODEC.fieldOf("Operation").forGetter(AttributeModifier::getOperation)
				)
				.apply(instance, AttributeModifier::new)
	);
	private final double amount;
	private final AttributeModifier.Operation operation;
	private final String name;
	private final UUID id;

	public AttributeModifier(String string, double d, AttributeModifier.Operation operation) {
		this(Mth.createInsecureUUID(RandomSource.createNewThreadLocalInstance()), string, d, operation);
	}

	public AttributeModifier(UUID uUID, String string, double d, AttributeModifier.Operation operation) {
		this.id = uUID;
		this.name = string;
		this.amount = d;
		this.operation = operation;
	}

	public UUID getId() {
		return this.id;
	}

	public AttributeModifier.Operation getOperation() {
		return this.operation;
	}

	public double getAmount() {
		return this.amount;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			AttributeModifier attributeModifier = (AttributeModifier)object;
			return Objects.equals(this.id, attributeModifier.id);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return this.id.hashCode();
	}

	public String toString() {
		return "AttributeModifier{amount=" + this.amount + ", operation=" + this.operation + ", name='" + this.name + "', id=" + this.id + "}";
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
		ADDITION("addition", 0),
		MULTIPLY_BASE("multiply_base", 1),
		MULTIPLY_TOTAL("multiply_total", 2);

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
