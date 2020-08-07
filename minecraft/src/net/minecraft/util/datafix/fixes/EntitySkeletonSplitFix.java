package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;

public class EntitySkeletonSplitFix extends SimpleEntityRenameFix {
	public EntitySkeletonSplitFix(Schema schema, boolean bl) {
		super("EntitySkeletonSplitFix", schema, bl);
	}

	@Override
	protected Pair<String, Dynamic<?>> getNewNameAndTag(String string, Dynamic<?> dynamic) {
		if (Objects.equals(string, "Skeleton")) {
			int i = dynamic.get("SkeletonType").asInt(0);
			if (i == 1) {
				string = "WitherSkeleton";
			} else if (i == 2) {
				string = "Stray";
			}
		}

		return Pair.of(string, dynamic);
	}
}
