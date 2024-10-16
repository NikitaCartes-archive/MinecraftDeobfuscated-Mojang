package net.minecraft.util.datafix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class FixWolfHealth extends NamedEntityFix {
	private static final String WOLF_ID = "minecraft:wolf";
	private static final String WOLF_HEALTH = "minecraft:generic.max_health";

	public FixWolfHealth(Schema schema) {
		super(schema, false, "FixWolfHealth", References.ENTITY, "minecraft:wolf");
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		return typed.update(
			DSL.remainderFinder(),
			dynamic -> {
				MutableBoolean mutableBoolean = new MutableBoolean(false);
				dynamic = dynamic.update(
					"Attributes",
					dynamicx -> dynamicx.createList(
							dynamicx.asStream()
								.map(
									dynamicxx -> "minecraft:generic.max_health".equals(NamespacedSchema.ensureNamespaced(dynamicxx.get("Name").asString("")))
											? dynamicxx.update("Base", dynamicxxx -> {
												if (dynamicxxx.asDouble(0.0) == 20.0) {
													mutableBoolean.setTrue();
													return dynamicxxx.createDouble(40.0);
												} else {
													return dynamicxxx;
												}
											})
											: dynamicxx
								)
						)
				);
				if (mutableBoolean.isTrue()) {
					dynamic = dynamic.update("Health", dynamicx -> dynamicx.createFloat(dynamicx.asFloat(0.0F) * 2.0F));
				}

				return dynamic;
			}
		);
	}
}
