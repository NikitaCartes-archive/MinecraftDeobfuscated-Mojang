package net.minecraft.world.entity.ai.attributes;

import io.netty.util.internal.ThreadLocalRandom;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.util.Mth;

public class AttributeModifier {
	private final double amount;
	private final AttributeModifier.Operation operation;
	private final Supplier<String> nameGetter;
	private final UUID id;
	private boolean serialize = true;

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

	public boolean isSerializable() {
		return this.serialize;
	}

	public AttributeModifier setSerialize(boolean bl) {
		this.serialize = bl;
		return this;
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
		return this.id != null ? this.id.hashCode() : 0;
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
			+ ", serialize="
			+ this.serialize
			+ '}';
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
