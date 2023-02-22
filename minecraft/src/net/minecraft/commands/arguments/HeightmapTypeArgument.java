package net.minecraft.commands.arguments;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.Heightmap;

public class HeightmapTypeArgument extends StringRepresentableArgument<Heightmap.Types> {
	private static final Codec<Heightmap.Types> LOWER_CASE_CODEC = StringRepresentable.fromEnumWithMapping(
		HeightmapTypeArgument::keptTypes, string -> string.toLowerCase(Locale.ROOT)
	);

	private static Heightmap.Types[] keptTypes() {
		return (Heightmap.Types[])Arrays.stream(Heightmap.Types.values()).filter(Heightmap.Types::keepAfterWorldgen).toArray(Heightmap.Types[]::new);
	}

	private HeightmapTypeArgument() {
		super(LOWER_CASE_CODEC, HeightmapTypeArgument::keptTypes);
	}

	public static HeightmapTypeArgument heightmap() {
		return new HeightmapTypeArgument();
	}

	public static Heightmap.Types getHeightmap(CommandContext<CommandSourceStack> commandContext, String string) {
		return commandContext.getArgument(string, Heightmap.Types.class);
	}

	@Override
	protected String convertId(String string) {
		return string.toLowerCase(Locale.ROOT);
	}
}
