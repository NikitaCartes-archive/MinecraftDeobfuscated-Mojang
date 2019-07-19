/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.state.properties;

import com.google.common.base.MoreObjects;
import net.minecraft.world.level.block.state.properties.Property;

public abstract class AbstractProperty<T extends Comparable<T>>
implements Property<T> {
    private final Class<T> clazz;
    private final String name;
    private Integer hashCode;

    protected AbstractProperty(String string, Class<T> class_) {
        this.clazz = class_;
        this.name = string;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Class<T> getValueClass() {
        return this.clazz;
    }

    public String toString() {
        return MoreObjects.toStringHelper(this).add("name", this.name).add("clazz", this.clazz).add("values", this.getPossibleValues()).toString();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof AbstractProperty) {
            AbstractProperty abstractProperty = (AbstractProperty)object;
            return this.clazz.equals(abstractProperty.clazz) && this.name.equals(abstractProperty.name);
        }
        return false;
    }

    public final int hashCode() {
        if (this.hashCode == null) {
            this.hashCode = this.generateHashCode();
        }
        return this.hashCode;
    }

    public int generateHashCode() {
        return 31 * this.clazz.hashCode() + this.name.hashCode();
    }
}

