package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;

public class WriteAndReadFix extends DataFix {
	private final String name;
	private final TypeReference type;

	public WriteAndReadFix(Schema schema, String string, TypeReference typeReference) {
		super(schema, true);
		this.name = string;
		this.type = typeReference;
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return this.writeAndRead(this.name, this.getInputSchema().getType(this.type), this.getOutputSchema().getType(this.type));
	}
}
