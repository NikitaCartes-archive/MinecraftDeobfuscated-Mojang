/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.attributes;

import io.netty.util.internal.ThreadLocalRandom;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.util.Mth;

public class AttributeModifier {
    private final double amount;
    private final Operation operation;
    private final Supplier<String> nameGetter;
    private final UUID id;
    private boolean serialize = true;

    public AttributeModifier(String string, double d, Operation operation) {
        this(Mth.createInsecureUUID(ThreadLocalRandom.current()), () -> string, d, operation);
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
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        AttributeModifier attributeModifier = (AttributeModifier)object;
        return Objects.equals(this.id, attributeModifier.id);
    }

    public int hashCode() {
        return this.id != null ? this.id.hashCode() : 0;
    }

    public String toString() {
        return "AttributeModifier{amount=" + this.amount + ", operation=" + (Object)((Object)this.operation) + ", name='" + this.nameGetter.get() + '\'' + ", id=" + this.id + ", serialize=" + this.serialize + '}';
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

