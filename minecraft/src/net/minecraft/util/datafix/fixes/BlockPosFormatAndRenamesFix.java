package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.util.datafix.ExtraDataFixUtils;

public class BlockPosFormatAndRenamesFix extends DataFix {
	private static final List<String> PATROLLING_MOBS = List.of(
		"minecraft:witch", "minecraft:ravager", "minecraft:pillager", "minecraft:illusioner", "minecraft:evoker", "minecraft:vindicator"
	);

	public BlockPosFormatAndRenamesFix(Schema schema) {
		super(schema, false);
	}

	private Typed<?> fixFields(Typed<?> typed, Map<String, String> map) {
		return typed.update(DSL.remainderFinder(), dynamic -> {
			for (Entry<String, String> entry : map.entrySet()) {
				dynamic = ExtraDataFixUtils.renameAndFixField(dynamic, (String)entry.getKey(), (String)entry.getValue(), ExtraDataFixUtils::fixBlockPos);
			}

			return dynamic;
		});
	}

	private <T> Dynamic<T> fixMapSavedData(Dynamic<T> dynamic) {
		return dynamic.update("frames", dynamicx -> dynamicx.createList(dynamicx.asStream().map(dynamicxx -> {
				dynamicxx = ExtraDataFixUtils.renameAndFixField(dynamicxx, "Pos", "pos", ExtraDataFixUtils::fixBlockPos);
				dynamicxx = ExtraDataFixUtils.renameField(dynamicxx, "Rotation", "rotation");
				return ExtraDataFixUtils.renameField(dynamicxx, "EntityId", "entity_id");
			}))).update("banners", dynamicx -> dynamicx.createList(dynamicx.asStream().map(dynamicxx -> {
				dynamicxx = ExtraDataFixUtils.renameField(dynamicxx, "Pos", "pos");
				dynamicxx = ExtraDataFixUtils.renameField(dynamicxx, "Color", "color");
				return ExtraDataFixUtils.renameField(dynamicxx, "Name", "name");
			})));
	}

	@Override
	public TypeRewriteRule makeRule() {
		List<TypeRewriteRule> list = new ArrayList();
		this.addEntityRules(list);
		this.addBlockEntityRules(list);
		list.add(
			this.fixTypeEverywhereTyped(
				"BlockPos format for map frames",
				this.getInputSchema().getType(References.SAVED_DATA_MAP_DATA),
				typed -> typed.update(DSL.remainderFinder(), dynamic -> dynamic.update("data", this::fixMapSavedData))
			)
		);
		Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
		list.add(
			this.fixTypeEverywhereTyped(
				"BlockPos format for compass target",
				type,
				ItemStackTagFix.createFixer(type, "minecraft:compass"::equals, dynamic -> dynamic.update("LodestonePos", ExtraDataFixUtils::fixBlockPos))
			)
		);
		return TypeRewriteRule.seq(list);
	}

	private void addEntityRules(List<TypeRewriteRule> list) {
		list.add(this.createEntityFixer(References.ENTITY, "minecraft:bee", Map.of("HivePos", "hive_pos", "FlowerPos", "flower_pos")));
		list.add(this.createEntityFixer(References.ENTITY, "minecraft:end_crystal", Map.of("BeamTarget", "beam_target")));
		list.add(this.createEntityFixer(References.ENTITY, "minecraft:wandering_trader", Map.of("WanderTarget", "wander_target")));

		for (String string : PATROLLING_MOBS) {
			list.add(this.createEntityFixer(References.ENTITY, string, Map.of("PatrolTarget", "patrol_target")));
		}

		list.add(
			this.fixTypeEverywhereTyped(
				"BlockPos format in Leash for mobs",
				this.getInputSchema().getType(References.ENTITY),
				typed -> typed.update(DSL.remainderFinder(), dynamic -> ExtraDataFixUtils.renameAndFixField(dynamic, "Leash", "leash", ExtraDataFixUtils::fixBlockPos))
			)
		);
	}

	private void addBlockEntityRules(List<TypeRewriteRule> list) {
		list.add(this.createEntityFixer(References.BLOCK_ENTITY, "minecraft:beehive", Map.of("FlowerPos", "flower_pos")));
		list.add(this.createEntityFixer(References.BLOCK_ENTITY, "minecraft:end_gateway", Map.of("ExitPortal", "exit_portal")));
	}

	private TypeRewriteRule createEntityFixer(TypeReference typeReference, String string, Map<String, String> map) {
		String string2 = "BlockPos format in " + map.keySet() + " for " + string + " (" + typeReference.typeName() + ")";
		OpticFinder<?> opticFinder = DSL.namedChoice(string, this.getInputSchema().getChoiceType(typeReference, string));
		return this.fixTypeEverywhereTyped(
			string2, this.getInputSchema().getType(typeReference), typed -> typed.updateTyped(opticFinder, typedx -> this.fixFields(typedx, map))
		);
	}
}
