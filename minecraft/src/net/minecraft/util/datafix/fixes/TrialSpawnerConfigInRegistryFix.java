package net.minecraft.util.datafix.fixes;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class TrialSpawnerConfigInRegistryFix extends NamedEntityFix {
	private static final Logger LOGGER = LogUtils.getLogger();

	public TrialSpawnerConfigInRegistryFix(Schema schema) {
		super(schema, false, "TrialSpawnerConfigInRegistryFix", References.BLOCK_ENTITY, "minecraft:trial_spawner");
	}

	public Dynamic<?> fixTag(Dynamic<Tag> dynamic) {
		Optional<Dynamic<Tag>> optional = dynamic.get("normal_config").result();
		if (optional.isEmpty()) {
			return dynamic;
		} else {
			Optional<Dynamic<Tag>> optional2 = dynamic.get("ominous_config").result();
			if (optional2.isEmpty()) {
				return dynamic;
			} else {
				ResourceLocation resourceLocation = (ResourceLocation)TrialSpawnerConfigInRegistryFix.VanillaTrialChambers.CONFIGS_TO_KEY
					.get(Pair.of((Dynamic)optional.get(), (Dynamic)optional2.get()));
				return resourceLocation == null
					? dynamic
					: dynamic.set("normal_config", dynamic.createString(resourceLocation.withSuffix("/normal").toString()))
						.set("ominous_config", dynamic.createString(resourceLocation.withSuffix("/ominous").toString()));
			}
		}
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		return typed.update(DSL.remainderFinder(), dynamic -> {
			DynamicOps<?> dynamicOps = dynamic.getOps();
			Dynamic<?> dynamic2 = this.fixTag(dynamic.convert(NbtOps.INSTANCE));
			return dynamic2.convert(dynamicOps);
		});
	}

	static final class VanillaTrialChambers {
		public static final Map<Pair<Dynamic<Tag>, Dynamic<Tag>>, ResourceLocation> CONFIGS_TO_KEY = new HashMap();

		private VanillaTrialChambers() {
		}

		private static void register(ResourceLocation resourceLocation, String string, String string2) {
			try {
				CompoundTag compoundTag = parse(string);
				CompoundTag compoundTag2 = parse(string2);
				CompoundTag compoundTag3 = compoundTag.copy().merge(compoundTag2);
				CompoundTag compoundTag4 = removeDefaults(compoundTag3.copy());
				Dynamic<Tag> dynamic = asDynamic(compoundTag);
				CONFIGS_TO_KEY.put(Pair.of(dynamic, asDynamic(compoundTag2)), resourceLocation);
				CONFIGS_TO_KEY.put(Pair.of(dynamic, asDynamic(compoundTag3)), resourceLocation);
				CONFIGS_TO_KEY.put(Pair.of(dynamic, asDynamic(compoundTag4)), resourceLocation);
			} catch (RuntimeException var8) {
				throw new IllegalStateException("Failed to parse NBT for " + resourceLocation, var8);
			}
		}

		private static Dynamic<Tag> asDynamic(CompoundTag compoundTag) {
			return new Dynamic<>(NbtOps.INSTANCE, compoundTag);
		}

		private static CompoundTag parse(String string) {
			try {
				return TagParser.parseTag(string);
			} catch (CommandSyntaxException var2) {
				throw new IllegalArgumentException("Failed to parse Trial Spawner NBT config: " + string, var2);
			}
		}

		private static CompoundTag removeDefaults(CompoundTag compoundTag) {
			if (compoundTag.getInt("spawn_range") == 4) {
				compoundTag.remove("spawn_range");
			}

			if (compoundTag.getFloat("total_mobs") == 6.0F) {
				compoundTag.remove("total_mobs");
			}

			if (compoundTag.getFloat("simultaneous_mobs") == 2.0F) {
				compoundTag.remove("simultaneous_mobs");
			}

			if (compoundTag.getFloat("total_mobs_added_per_player") == 2.0F) {
				compoundTag.remove("total_mobs_added_per_player");
			}

			if (compoundTag.getFloat("simultaneous_mobs_added_per_player") == 1.0F) {
				compoundTag.remove("simultaneous_mobs_added_per_player");
			}

			if (compoundTag.getInt("ticks_between_spawn") == 40) {
				compoundTag.remove("ticks_between_spawn");
			}

			return compoundTag;
		}

		static {
			register(
				ResourceLocation.withDefaultNamespace("trial_chamber/breeze"),
				"{simultaneous_mobs: 1.0f, simultaneous_mobs_added_per_player: 0.5f, spawn_potentials: [{data: {entity: {id: \"minecraft:breeze\"}}, weight: 1}], ticks_between_spawn: 20, total_mobs: 2.0f, total_mobs_added_per_player: 1.0f}",
				"{loot_tables_to_eject: [{data: \"minecraft:spawners/ominous/trial_chamber/key\", weight: 3}, {data: \"minecraft:spawners/ominous/trial_chamber/consumables\", weight: 7}], simultaneous_mobs: 2.0f, total_mobs: 4.0f}"
			);
			register(
				ResourceLocation.withDefaultNamespace("trial_chamber/melee/husk"),
				"{simultaneous_mobs: 3.0f, simultaneous_mobs_added_per_player: 0.5f, spawn_potentials: [{data: {entity: {id: \"minecraft:husk\"}}, weight: 1}], ticks_between_spawn: 20}",
				"{loot_tables_to_eject: [{data: \"minecraft:spawners/ominous/trial_chamber/key\", weight: 3}, {data: \"minecraft:spawners/ominous/trial_chamber/consumables\", weight: 7}], spawn_potentials: [{data: {entity: {id: \"minecraft:husk\"}, equipment: {loot_table: \"minecraft:equipment/trial_chamber_melee\", slot_drop_chances: 0.0f}}, weight: 1}]}"
			);
			register(
				ResourceLocation.withDefaultNamespace("trial_chamber/melee/spider"),
				"{simultaneous_mobs: 3.0f, simultaneous_mobs_added_per_player: 0.5f, spawn_potentials: [{data: {entity: {id: \"minecraft:spider\"}}, weight: 1}], ticks_between_spawn: 20}",
				"{loot_tables_to_eject: [{data: \"minecraft:spawners/ominous/trial_chamber/key\", weight: 3}, {data: \"minecraft:spawners/ominous/trial_chamber/consumables\", weight: 7}],simultaneous_mobs: 4.0f, total_mobs: 12.0f}"
			);
			register(
				ResourceLocation.withDefaultNamespace("trial_chamber/melee/zombie"),
				"{simultaneous_mobs: 3.0f, simultaneous_mobs_added_per_player: 0.5f, spawn_potentials: [{data: {entity: {id: \"minecraft:zombie\"}}, weight: 1}], ticks_between_spawn: 20}",
				"{loot_tables_to_eject: [{data: \"minecraft:spawners/ominous/trial_chamber/key\", weight: 3}, {data: \"minecraft:spawners/ominous/trial_chamber/consumables\", weight: 7}],spawn_potentials: [{data: {entity: {id: \"minecraft:zombie\"}, equipment: {loot_table: \"minecraft:equipment/trial_chamber_melee\", slot_drop_chances: 0.0f}}, weight: 1}]}"
			);
			register(
				ResourceLocation.withDefaultNamespace("trial_chamber/ranged/poison_skeleton"),
				"{simultaneous_mobs: 3.0f, simultaneous_mobs_added_per_player: 0.5f, spawn_potentials: [{data: {entity: {id: \"minecraft:bogged\"}}, weight: 1}], ticks_between_spawn: 20}",
				"{loot_tables_to_eject: [{data: \"minecraft:spawners/ominous/trial_chamber/key\", weight: 3}, {data: \"minecraft:spawners/ominous/trial_chamber/consumables\", weight: 7}],spawn_potentials: [{data: {entity: {id: \"minecraft:bogged\"}, equipment: {loot_table: \"minecraft:equipment/trial_chamber_ranged\", slot_drop_chances: 0.0f}}, weight: 1}]}"
			);
			register(
				ResourceLocation.withDefaultNamespace("trial_chamber/ranged/skeleton"),
				"{simultaneous_mobs: 3.0f, simultaneous_mobs_added_per_player: 0.5f, spawn_potentials: [{data: {entity: {id: \"minecraft:skeleton\"}}, weight: 1}], ticks_between_spawn: 20}",
				"{loot_tables_to_eject: [{data: \"minecraft:spawners/ominous/trial_chamber/key\", weight: 3}, {data: \"minecraft:spawners/ominous/trial_chamber/consumables\", weight: 7}], spawn_potentials: [{data: {entity: {id: \"minecraft:skeleton\"}, equipment: {loot_table: \"minecraft:equipment/trial_chamber_ranged\", slot_drop_chances: 0.0f}}, weight: 1}]}"
			);
			register(
				ResourceLocation.withDefaultNamespace("trial_chamber/ranged/stray"),
				"{simultaneous_mobs: 3.0f, simultaneous_mobs_added_per_player: 0.5f, spawn_potentials: [{data: {entity: {id: \"minecraft:stray\"}}, weight: 1}], ticks_between_spawn: 20}",
				"{loot_tables_to_eject: [{data: \"minecraft:spawners/ominous/trial_chamber/key\", weight: 3}, {data: \"minecraft:spawners/ominous/trial_chamber/consumables\", weight: 7}], spawn_potentials: [{data: {entity: {id: \"minecraft:stray\"}, equipment: {loot_table: \"minecraft:equipment/trial_chamber_ranged\", slot_drop_chances: 0.0f}}, weight: 1}]}"
			);
			register(
				ResourceLocation.withDefaultNamespace("trial_chamber/slow_ranged/poison_skeleton"),
				"{simultaneous_mobs: 4.0f, simultaneous_mobs_added_per_player: 2.0f, spawn_potentials: [{data: {entity: {id: \"minecraft:bogged\"}}, weight: 1}], ticks_between_spawn: 160}",
				"{loot_tables_to_eject: [{data: \"minecraft:spawners/ominous/trial_chamber/key\", weight: 3}, {data: \"minecraft:spawners/ominous/trial_chamber/consumables\", weight: 7}], spawn_potentials: [{data: {entity: {id: \"minecraft:bogged\"}, equipment: {loot_table: \"minecraft:equipment/trial_chamber_ranged\", slot_drop_chances: 0.0f}}, weight: 1}]}"
			);
			register(
				ResourceLocation.withDefaultNamespace("trial_chamber/slow_ranged/skeleton"),
				"{simultaneous_mobs: 4.0f, simultaneous_mobs_added_per_player: 2.0f, spawn_potentials: [{data: {entity: {id: \"minecraft:skeleton\"}}, weight: 1}], ticks_between_spawn: 160}",
				"{loot_tables_to_eject: [{data: \"minecraft:spawners/ominous/trial_chamber/key\", weight: 3}, {data: \"minecraft:spawners/ominous/trial_chamber/consumables\", weight: 7}], spawn_potentials: [{data: {entity: {id: \"minecraft:skeleton\"}, equipment: {loot_table: \"minecraft:equipment/trial_chamber_ranged\", slot_drop_chances: 0.0f}}, weight: 1}]}"
			);
			register(
				ResourceLocation.withDefaultNamespace("trial_chamber/slow_ranged/stray"),
				"{simultaneous_mobs: 4.0f, simultaneous_mobs_added_per_player: 2.0f, spawn_potentials: [{data: {entity: {id: \"minecraft:stray\"}}, weight: 1}], ticks_between_spawn: 160}",
				"{loot_tables_to_eject: [{data: \"minecraft:spawners/ominous/trial_chamber/key\", weight: 3}, {data: \"minecraft:spawners/ominous/trial_chamber/consumables\", weight: 7}],spawn_potentials: [{data: {entity: {id: \"minecraft:stray\"}, equipment: {loot_table: \"minecraft:equipment/trial_chamber_ranged\", slot_drop_chances: 0.0f}}, weight: 1}]}"
			);
			register(
				ResourceLocation.withDefaultNamespace("trial_chamber/small_melee/baby_zombie"),
				"{simultaneous_mobs: 2.0f, simultaneous_mobs_added_per_player: 0.5f, spawn_potentials: [{data: {entity: {IsBaby: 1b, id: \"minecraft:zombie\"}}, weight: 1}], ticks_between_spawn: 20}",
				"{loot_tables_to_eject: [{data: \"minecraft:spawners/ominous/trial_chamber/key\", weight: 3}, {data: \"minecraft:spawners/ominous/trial_chamber/consumables\", weight: 7}], spawn_potentials: [{data: {entity: {IsBaby: 1b, id: \"minecraft:zombie\"}, equipment: {loot_table: \"minecraft:equipment/trial_chamber_melee\", slot_drop_chances: 0.0f}}, weight: 1}]}"
			);
			register(
				ResourceLocation.withDefaultNamespace("trial_chamber/small_melee/cave_spider"),
				"{simultaneous_mobs: 3.0f, simultaneous_mobs_added_per_player: 0.5f, spawn_potentials: [{data: {entity: {id: \"minecraft:cave_spider\"}}, weight: 1}], ticks_between_spawn: 20}",
				"{loot_tables_to_eject: [{data: \"minecraft:spawners/ominous/trial_chamber/key\", weight: 3}, {data: \"minecraft:spawners/ominous/trial_chamber/consumables\", weight: 7}], simultaneous_mobs: 4.0f, total_mobs: 12.0f}"
			);
			register(
				ResourceLocation.withDefaultNamespace("trial_chamber/small_melee/silverfish"),
				"{simultaneous_mobs: 3.0f, simultaneous_mobs_added_per_player: 0.5f, spawn_potentials: [{data: {entity: {id: \"minecraft:silverfish\"}}, weight: 1}], ticks_between_spawn: 20}",
				"{loot_tables_to_eject: [{data: \"minecraft:spawners/ominous/trial_chamber/key\", weight: 3}, {data: \"minecraft:spawners/ominous/trial_chamber/consumables\", weight: 7}], simultaneous_mobs: 4.0f, total_mobs: 12.0f}"
			);
			register(
				ResourceLocation.withDefaultNamespace("trial_chamber/small_melee/slime"),
				"{simultaneous_mobs: 3.0f, simultaneous_mobs_added_per_player: 0.5f, spawn_potentials: [{data: {entity: {Size: 1, id: \"minecraft:slime\"}}, weight: 3}, {data: {entity: {Size: 2, id: \"minecraft:slime\"}}, weight: 1}], ticks_between_spawn: 20}",
				"{loot_tables_to_eject: [{data: \"minecraft:spawners/ominous/trial_chamber/key\", weight: 3}, {data: \"minecraft:spawners/ominous/trial_chamber/consumables\", weight: 7}], simultaneous_mobs: 4.0f, total_mobs: 12.0f}"
			);
		}
	}
}
