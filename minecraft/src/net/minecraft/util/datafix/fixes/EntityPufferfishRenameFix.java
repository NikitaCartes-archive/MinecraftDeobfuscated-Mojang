package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.schemas.Schema;
import java.util.Map;
import java.util.Objects;

public class EntityPufferfishRenameFix extends SimplestEntityRenameFix {
	public static final Map<String, String> RENAMED_IDS = ImmutableMap.<String, String>builder()
		.put("minecraft:puffer_fish_spawn_egg", "minecraft:pufferfish_spawn_egg")
		.build();

	public EntityPufferfishRenameFix(Schema schema, boolean bl) {
		super("EntityPufferfishRenameFix", schema, bl);
	}

	@Override
	protected String rename(String string) {
		return Objects.equals("minecraft:puffer_fish", string) ? "minecraft:pufferfish" : string;
	}
}
