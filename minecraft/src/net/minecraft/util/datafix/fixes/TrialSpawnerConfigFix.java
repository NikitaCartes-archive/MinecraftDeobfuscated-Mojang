package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TrialSpawnerConfigFix extends NamedEntityWriteReadFix {
	public TrialSpawnerConfigFix(Schema schema) {
		super(schema, true, "Trial Spawner config tag fixer", References.BLOCK_ENTITY, "minecraft:trial_spawner");
	}

	private static <T> Dynamic<T> moveToConfigTag(Dynamic<T> dynamic) {
		List<String> list = List.of(
			"spawn_range",
			"total_mobs",
			"simultaneous_mobs",
			"total_mobs_added_per_player",
			"simultaneous_mobs_added_per_player",
			"ticks_between_spawn",
			"spawn_potentials",
			"loot_tables_to_eject",
			"items_to_drop_when_ominous"
		);
		Map<Dynamic<T>, Dynamic<T>> map = new HashMap(list.size());

		for (String string : list) {
			Optional<Dynamic<T>> optional = dynamic.get(string).get().result();
			if (optional.isPresent()) {
				map.put(dynamic.createString(string), (Dynamic)optional.get());
				dynamic = dynamic.remove(string);
			}
		}

		return map.isEmpty() ? dynamic : dynamic.set("normal_config", dynamic.createMap(map));
	}

	@Override
	protected <T> Dynamic<T> fix(Dynamic<T> dynamic) {
		return moveToConfigTag(dynamic);
	}
}
