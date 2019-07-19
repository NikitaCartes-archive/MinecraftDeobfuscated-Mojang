package net.minecraft.world.entity.ai.attributes;

import java.util.Collection;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public interface AttributeInstance {
	Attribute getAttribute();

	double getBaseValue();

	void setBaseValue(double d);

	Collection<AttributeModifier> getModifiers(AttributeModifier.Operation operation);

	Collection<AttributeModifier> getModifiers();

	boolean hasModifier(AttributeModifier attributeModifier);

	@Nullable
	AttributeModifier getModifier(UUID uUID);

	void addModifier(AttributeModifier attributeModifier);

	void removeModifier(AttributeModifier attributeModifier);

	void removeModifier(UUID uUID);

	@Environment(EnvType.CLIENT)
	void removeModifiers();

	double getValue();
}
