package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class EntityItemFrameDirectionFix extends NamedEntityFix {
	public EntityItemFrameDirectionFix(Schema schema, boolean bl) {
		super(schema, bl, "EntityItemFrameDirectionFix", References.ENTITY, "minecraft:item_frame");
	}

	public Dynamic<?> fixTag(Dynamic<?> dynamic) {
		return dynamic.set("Facing", dynamic.createByte(direction2dTo3d(dynamic.get("Facing").asByte((byte)0))));
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		return typed.update(DSL.remainderFinder(), this::fixTag);
	}

	private static byte direction2dTo3d(byte b) {
		switch (b) {
			case 0:
				return 3;
			case 1:
				return 4;
			case 2:
			default:
				return 2;
			case 3:
				return 5;
		}
	}
}
