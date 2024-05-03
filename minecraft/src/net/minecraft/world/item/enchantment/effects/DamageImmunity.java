package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;

public record DamageImmunity() {
	public static final DamageImmunity INSTANCE = new DamageImmunity();
	public static final Codec<DamageImmunity> CODEC = Codec.unit((Supplier<DamageImmunity>)(() -> INSTANCE));
}
