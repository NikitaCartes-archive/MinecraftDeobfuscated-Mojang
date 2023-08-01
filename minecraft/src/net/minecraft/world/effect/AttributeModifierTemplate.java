package net.minecraft.world.effect;

import java.util.UUID;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public interface AttributeModifierTemplate {
	UUID getAttributeModifierId();

	AttributeModifier create(int i);
}
