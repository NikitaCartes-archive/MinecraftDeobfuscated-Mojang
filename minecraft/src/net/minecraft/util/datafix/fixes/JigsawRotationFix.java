package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Optional;

public class JigsawRotationFix extends DataFix {
	private static final Map<String, String> RENAMES = ImmutableMap.<String, String>builder()
		.put("down", "down_south")
		.put("up", "up_north")
		.put("north", "north_up")
		.put("south", "south_up")
		.put("west", "west_up")
		.put("east", "east_up")
		.build();

	public JigsawRotationFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	private static Dynamic<?> fix(Dynamic<?> dynamic) {
		Optional<String> optional = dynamic.get("Name").asString().result();
		return optional.equals(Optional.of("minecraft:jigsaw")) ? dynamic.update("Properties", dynamicx -> {
			String string = dynamicx.get("facing").asString("north");
			return dynamicx.remove("facing").set("orientation", dynamicx.createString((String)RENAMES.getOrDefault(string, string)));
		}) : dynamic;
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return this.fixTypeEverywhereTyped(
			"jigsaw_rotation_fix", this.getInputSchema().getType(References.BLOCK_STATE), typed -> typed.update(DSL.remainderFinder(), JigsawRotationFix::fix)
		);
	}
}
