package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import java.util.List;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class AttributeIdPrefixFix extends AttributesRenameFix {
	private static final List<String> PREFIXES = List.of("generic.", "horse.", "player.", "zombie.");

	public AttributeIdPrefixFix(Schema schema) {
		super(schema, "AttributeIdPrefixFix", AttributeIdPrefixFix::replaceId);
	}

	private static String replaceId(String string) {
		String string2 = NamespacedSchema.ensureNamespaced(string);

		for (String string3 : PREFIXES) {
			String string4 = NamespacedSchema.ensureNamespaced(string3);
			if (string2.startsWith(string4)) {
				return "minecraft:" + string2.substring(string4.length());
			}
		}

		return string;
	}
}
