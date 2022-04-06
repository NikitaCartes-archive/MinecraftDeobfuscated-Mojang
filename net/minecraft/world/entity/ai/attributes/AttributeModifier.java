/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.attributes;

import com.mojang.logging.LogUtils;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class AttributeModifier {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final double amount;
    private final Operation operation;
    private final Supplier<String> nameGetter;
    private final UUID id;

    public AttributeModifier(String string, double d, Operation operation) {
        this(Mth.createInsecureUUID(RandomSource.createNewThreadLocalInstance()), () -> string, d, operation);
    }

    public AttributeModifier(UUID uUID, String string, double d, Operation operation) {
        this(uUID, () -> string, d, operation);
    }

    public AttributeModifier(UUID uUID, Supplier<String> supplier, double d, Operation operation) {
        this.id = uUID;
        this.nameGetter = supplier;
        this.amount = d;
        this.operation = operation;
    }

    public UUID getId() {
        return this.id;
    }

    public String getName() {
        return this.nameGetter.get();
    }

    public Operation getOperation() {
        return this.operation;
    }

    public double getAmount() {
        return this.amount;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        AttributeModifier attributeModifier = (AttributeModifier)object;
        return Objects.equals(this.id, attributeModifier.id);
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    public String toString() {
        return "AttributeModifier{amount=" + this.amount + ", operation=" + this.operation + ", name='" + this.nameGetter.get() + "', id=" + this.id + "}";
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
        try {
            UUID uUID = compoundTag.getUUID("UUID");
            Operation operation = Operation.fromValue(compoundTag.getInt("Operation"));
            return new AttributeModifier(uUID, compoundTag.getString("Name"), compoundTag.getDouble("Amount"), operation);
        } catch (Exception exception) {
            LOGGER.warn("Unable to create attribute: {}", (Object)exception.getMessage());
            return null;
        }
    }

    public static enum Operation {
        ADDITION(0),
        MULTIPLY_BASE(1),
        MULTIPLY_TOTAL(2);

        private static final Operation[] OPERATIONS;
        private final int value;

        private Operation(int j) {
            this.value = j;
        }

        public int toValue() {
            return this.value;
        }

        public static Operation fromValue(int i) {
            if (i < 0 || i >= OPERATIONS.length) {
                throw new IllegalArgumentException("No operation with value " + i);
            }
            return OPERATIONS[i];
        }

        static {
            OPERATIONS = new Operation[]{ADDITION, MULTIPLY_BASE, MULTIPLY_TOTAL};
        }
    }
}

