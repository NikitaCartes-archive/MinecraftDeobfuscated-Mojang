package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.Objects;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class RenameBiomesFix extends DataFix {
	private final String name;
	private final Map<String, String> biomes;

	public RenameBiomesFix(Schema schema, boolean bl, String string, Map<String, String> map) {
		super(schema, bl);
		this.biomes = map;
		this.name = string;
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Type<Pair<String, String>> type = DSL.named(References.BIOME.typeName(), NamespacedSchema.namespacedString());
		if (!Objects.equals(type, this.getInputSchema().getType(References.BIOME))) {
			throw new IllegalStateException("Biome type is not what was expected.");
		} else {
			return this.fixTypeEverywhere(this.name, type, dynamicOps -> pair -> pair.mapSecond(string -> (String)this.biomes.getOrDefault(string, string)));
		}
	}
}
