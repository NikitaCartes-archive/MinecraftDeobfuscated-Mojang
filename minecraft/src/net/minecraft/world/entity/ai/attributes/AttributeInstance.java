package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public interface AttributeInstance {
	Attribute getAttribute();

	double getBaseValue();

	void setBaseValue(double d);

	Set<AttributeModifier> getModifiers(AttributeModifier.Operation operation);

	Set<AttributeModifier> getModifiers();

	boolean hasModifier(AttributeModifier attributeModifier);

	@Nullable
	AttributeModifier getModifier(UUID uUID);

	void addModifier(AttributeModifier attributeModifier);

	void removeModifier(AttributeModifier attributeModifier);

	void removeModifier(UUID uUID);

	@Environment(EnvType.CLIENT)
	void removeModifiers();

	double getValue();

	@Environment(EnvType.CLIENT)
	default void copyFrom(AttributeInstance attributeInstance) {
		this.setBaseValue(attributeInstance.getBaseValue());
		Set<AttributeModifier> set = attributeInstance.getModifiers();
		Set<AttributeModifier> set2 = this.getModifiers();
		ImmutableSet<AttributeModifier> immutableSet = ImmutableSet.copyOf(Sets.difference(set, set2));
		ImmutableSet<AttributeModifier> immutableSet2 = ImmutableSet.copyOf(Sets.difference(set2, set));
		immutableSet.forEach(this::addModifier);
		immutableSet2.forEach(this::removeModifier);
	}
}
