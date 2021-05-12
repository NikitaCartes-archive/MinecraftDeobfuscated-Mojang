package net.minecraft.commands.arguments;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EntityAnchorArgument implements ArgumentType<EntityAnchorArgument.Anchor> {
	private static final Collection<String> EXAMPLES = Arrays.asList("eyes", "feet");
	private static final DynamicCommandExceptionType ERROR_INVALID = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("argument.anchor.invalid", object)
	);

	public static EntityAnchorArgument.Anchor getAnchor(CommandContext<CommandSourceStack> commandContext, String string) {
		return commandContext.getArgument(string, EntityAnchorArgument.Anchor.class);
	}

	public static EntityAnchorArgument anchor() {
		return new EntityAnchorArgument();
	}

	public EntityAnchorArgument.Anchor parse(StringReader stringReader) throws CommandSyntaxException {
		int i = stringReader.getCursor();
		String string = stringReader.readUnquotedString();
		EntityAnchorArgument.Anchor anchor = EntityAnchorArgument.Anchor.getByName(string);
		if (anchor == null) {
			stringReader.setCursor(i);
			throw ERROR_INVALID.createWithContext(stringReader, string);
		} else {
			return anchor;
		}
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		return SharedSuggestionProvider.suggest(EntityAnchorArgument.Anchor.BY_NAME.keySet(), suggestionsBuilder);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	public static enum Anchor {
		FEET("feet", (vec3, entity) -> vec3),
		EYES("eyes", (vec3, entity) -> new Vec3(vec3.x, vec3.y + (double)entity.getEyeHeight(), vec3.z));

		static final Map<String, EntityAnchorArgument.Anchor> BY_NAME = Util.make(Maps.<String, EntityAnchorArgument.Anchor>newHashMap(), hashMap -> {
			for (EntityAnchorArgument.Anchor anchor : values()) {
				hashMap.put(anchor.name, anchor);
			}
		});
		private final String name;
		private final BiFunction<Vec3, Entity, Vec3> transform;

		private Anchor(String string2, BiFunction<Vec3, Entity, Vec3> biFunction) {
			this.name = string2;
			this.transform = biFunction;
		}

		@Nullable
		public static EntityAnchorArgument.Anchor getByName(String string) {
			return (EntityAnchorArgument.Anchor)BY_NAME.get(string);
		}

		public Vec3 apply(Entity entity) {
			return (Vec3)this.transform.apply(entity.position(), entity);
		}

		public Vec3 apply(CommandSourceStack commandSourceStack) {
			Entity entity = commandSourceStack.getEntity();
			return entity == null ? commandSourceStack.getPosition() : (Vec3)this.transform.apply(commandSourceStack.getPosition(), entity);
		}
	}
}
