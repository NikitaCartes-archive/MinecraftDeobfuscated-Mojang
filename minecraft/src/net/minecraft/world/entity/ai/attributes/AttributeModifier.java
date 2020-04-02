package net.minecraft.world.entity.ai.attributes;

import io.netty.util.internal.ThreadLocalRandom;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AttributeModifier {
	private static final Logger LOGGER = LogManager.getLogger();
	private final double amount;
	private final AttributeModifier.Operation operation;
	private final Supplier<String> nameGetter;
	private final UUID id;

	public AttributeModifier(String string, double d, AttributeModifier.Operation operation) {
		this(Mth.createInsecureUUID(ThreadLocalRandom.current()), (Supplier<String>)(() -> string), d, operation);
	}

	public AttributeModifier(UUID uUID, String string, double d, AttributeModifier.Operation operation) {
		this(uUID, (Supplier<String>)(() -> string), d, operation);
	}

	public AttributeModifier(UUID uUID, Supplier<String> supplier, double d, AttributeModifier.Operation operation) {
		this.id = uUID;
		this.nameGetter = supplier;
		this.amount = d;
		this.operation = operation;
	}

	public UUID getId() {
		return this.id;
	}

	public String getName() {
		return (String)this.nameGetter.get();
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
		return "AttributeModifier{amount="
			+ this.amount
			+ ", operation="
			+ this.operation
			+ ", name='"
			+ (String)this.nameGetter.get()
			+ '\''
			+ ", id="
			+ this.id
			+ '}';
	}

	public CompoundTag save() {
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.putString("Name", this.getName());
		compoundTag.putDouble("Amount", this.amount);
		compoundTag.putInt("Operation", this.operation.toValue());
		compoundTag.putUUID("UUID", this.id);
		return compoundTag;
	}

	@Nullable
	public static AttributeModifier load(CompoundTag compoundTag) {
		UUID uUID = compoundTag.getUUID("UUID");

		try {
			AttributeModifier.Operation operation = AttributeModifier.Operation.fromValue(compoundTag.getInt("Operation"));
			return new AttributeModifier(uUID, compoundTag.getString("Name"), compoundTag.getDouble("Amount"), operation);
		} catch (Exception var3) {
			LOGGER.warn("Unable to create attribute: {}", var3.getMessage());
			return null;
		}
	}

	public static enum Operation {
		ADDITION(0),
		MULTIPLY_BASE(1),
		MULTIPLY_TOTAL(2);

		private static final AttributeModifier.Operation[] OPERATIONS = new AttributeModifier.Operation[]{ADDITION, MULTIPLY_BASE, MULTIPLY_TOTAL};
		private final int value;

		private Operation(int j) {
			this.value = j;
		}

		public int toValue() {
			return this.value;
		}

		public static AttributeModifier.Operation fromValue(int i) {
			if (i >= 0 && i < OPERATIONS.length) {
				return OPERATIONS[i];
			} else {
				throw new IllegalArgumentException("No operation with value " + i);
			}
		}
	}
}
