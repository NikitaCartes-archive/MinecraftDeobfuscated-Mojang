package net.minecraft.util.datafix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class FixWolfHealth extends NamedEntityFix {
	private static final String WOLF_ID = "minecraft:wolf";
	private static final ResourceLocation WOLF_HEALTH = new ResourceLocation("generic.max_health");

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
								.map(dynamicxx -> Objects.equals(ResourceLocation.tryParse(dynamicxx.get("Name").asString("")), WOLF_HEALTH) ? dynamicxx.update("Base", dynamicxxx -> {
										if (dynamicxxx.asDouble(0.0) == 20.0) {
											mutableBoolean.setTrue();
											return dynamicxxx.createDouble(40.0);
										} else {
											return dynamicxxx;
										}
									}) : dynamicxx)
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
