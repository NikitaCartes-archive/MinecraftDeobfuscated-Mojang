package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;

public class DamageTypeTagsProvider extends TagsProvider<DamageType> {
	public DamageTypeTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
		super(packOutput, Registries.DAMAGE_TYPE, completableFuture);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		this.tag(DamageTypeTags.DAMAGES_HELMET).add(DamageTypes.FALLING_ANVIL, DamageTypes.FALLING_BLOCK, DamageTypes.FALLING_STALACTITE);
		this.tag(DamageTypeTags.BYPASSES_ARMOR)
			.add(
				DamageTypes.ON_FIRE,
				DamageTypes.IN_WALL,
				DamageTypes.CRAMMING,
				DamageTypes.DROWN,
				DamageTypes.FLY_INTO_WALL,
				DamageTypes.GENERIC,
				DamageTypes.WITHER,
				DamageTypes.DRAGON_BREATH,
				DamageTypes.STARVE,
				DamageTypes.FALL,
				DamageTypes.FREEZE,
				DamageTypes.STALAGMITE,
				DamageTypes.MAGIC,
				DamageTypes.INDIRECT_MAGIC,
				DamageTypes.FELL_OUT_OF_WORLD,
				DamageTypes.GENERIC_KILL,
				DamageTypes.SONIC_BOOM,
				DamageTypes.OUTSIDE_BORDER
			);
		this.tag(DamageTypeTags.BYPASSES_SHIELD).addTag(DamageTypeTags.BYPASSES_ARMOR).add(DamageTypes.FALLING_ANVIL, DamageTypes.FALLING_STALACTITE);
		this.tag(DamageTypeTags.BYPASSES_INVULNERABILITY).add(DamageTypes.FELL_OUT_OF_WORLD, DamageTypes.GENERIC_KILL);
		this.tag(DamageTypeTags.BYPASSES_EFFECTS).add(DamageTypes.STARVE);
		this.tag(DamageTypeTags.BYPASSES_RESISTANCE).add(DamageTypes.FELL_OUT_OF_WORLD, DamageTypes.GENERIC_KILL);
		this.tag(DamageTypeTags.BYPASSES_ENCHANTMENTS).add(DamageTypes.SONIC_BOOM);
		this.tag(DamageTypeTags.IS_FIRE)
			.add(DamageTypes.IN_FIRE, DamageTypes.ON_FIRE, DamageTypes.LAVA, DamageTypes.HOT_FLOOR, DamageTypes.UNATTRIBUTED_FIREBALL, DamageTypes.FIREBALL);
		this.tag(DamageTypeTags.IS_PROJECTILE)
			.add(
				DamageTypes.ARROW,
				DamageTypes.TRIDENT,
				DamageTypes.MOB_PROJECTILE,
				DamageTypes.UNATTRIBUTED_FIREBALL,
				DamageTypes.FIREBALL,
				DamageTypes.WITHER_SKULL,
				DamageTypes.THROWN
			);
		this.tag(DamageTypeTags.WITCH_RESISTANT_TO).add(DamageTypes.MAGIC, DamageTypes.INDIRECT_MAGIC, DamageTypes.SONIC_BOOM, DamageTypes.THORNS);
		this.tag(DamageTypeTags.IS_EXPLOSION).add(DamageTypes.FIREWORKS, DamageTypes.EXPLOSION, DamageTypes.PLAYER_EXPLOSION, DamageTypes.BAD_RESPAWN_POINT);
		this.tag(DamageTypeTags.IS_FALL).add(DamageTypes.FALL, DamageTypes.STALAGMITE);
		this.tag(DamageTypeTags.IS_DROWNING).add(DamageTypes.DROWN);
		this.tag(DamageTypeTags.IS_FREEZING).add(DamageTypes.FREEZE);
		this.tag(DamageTypeTags.IS_LIGHTNING).add(DamageTypes.LIGHTNING_BOLT);
		this.tag(DamageTypeTags.NO_ANGER).add(DamageTypes.MOB_ATTACK_NO_AGGRO);
		this.tag(DamageTypeTags.NO_IMPACT).add(DamageTypes.DROWN);
		this.tag(DamageTypeTags.ALWAYS_MOST_SIGNIFICANT_FALL).add(DamageTypes.FELL_OUT_OF_WORLD);
		this.tag(DamageTypeTags.WITHER_IMMUNE_TO).add(DamageTypes.DROWN);
		this.tag(DamageTypeTags.IGNITES_ARMOR_STANDS).add(DamageTypes.IN_FIRE);
		this.tag(DamageTypeTags.BURNS_ARMOR_STANDS).add(DamageTypes.ON_FIRE);
		this.tag(DamageTypeTags.AVOIDS_GUARDIAN_THORNS).add(DamageTypes.MAGIC, DamageTypes.THORNS).addTag(DamageTypeTags.IS_EXPLOSION);
		this.tag(DamageTypeTags.ALWAYS_TRIGGERS_SILVERFISH).add(DamageTypes.MAGIC);
		this.tag(DamageTypeTags.ALWAYS_HURTS_ENDER_DRAGONS).addTag(DamageTypeTags.IS_EXPLOSION);
		this.tag(DamageTypeTags.NO_KNOCKBACK).add(DamageTypes.EXPLOSION, DamageTypes.PLAYER_EXPLOSION, DamageTypes.BAD_RESPAWN_POINT);
		this.tag(DamageTypeTags.ALWAYS_KILLS_ARMOR_STANDS)
			.add(DamageTypes.ARROW, DamageTypes.TRIDENT, DamageTypes.MOB_PROJECTILE, DamageTypes.FIREBALL, DamageTypes.WITHER_SKULL);
	}
}
