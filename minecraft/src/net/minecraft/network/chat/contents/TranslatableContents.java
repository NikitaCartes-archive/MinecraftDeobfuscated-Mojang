package net.minecraft.network.chat.contents;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;

public class TranslatableContents implements ComponentContents {
	public static final Object[] NO_ARGS = new Object[0];
	private static final Codec<Object> PRIMITIVE_ARG_CODEC = ExtraCodecs.JAVA.validate(TranslatableContents::filterAllowedArguments);
	private static final Codec<Object> ARG_CODEC = Codec.either(PRIMITIVE_ARG_CODEC, ComponentSerialization.CODEC)
		.xmap(
			either -> either.map(object -> object, component -> Objects.requireNonNullElse(component.tryCollapseToString(), component)),
			object -> object instanceof Component component ? Either.right(component) : Either.left(object)
		);
	public static final MapCodec<TranslatableContents> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					Codec.STRING.fieldOf("translate").forGetter(translatableContents -> translatableContents.key),
					Codec.STRING.lenientOptionalFieldOf("fallback").forGetter(translatableContents -> Optional.ofNullable(translatableContents.fallback)),
					ARG_CODEC.listOf().optionalFieldOf("with").forGetter(translatableContents -> adjustArgs(translatableContents.args))
				)
				.apply(instance, TranslatableContents::create)
	);
	public static final ComponentContents.Type<TranslatableContents> TYPE = new ComponentContents.Type<>(CODEC, "translatable");
	private static final FormattedText TEXT_PERCENT = FormattedText.of("%");
	private static final FormattedText TEXT_NULL = FormattedText.of("null");
	private final String key;
	@Nullable
	private final String fallback;
	private final Object[] args;
	@Nullable
	private Language decomposedWith;
	private List<FormattedText> decomposedParts = ImmutableList.of();
	private static final Pattern FORMAT_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

	private static DataResult<Object> filterAllowedArguments(@Nullable Object object) {
		return !isAllowedPrimitiveArgument(object) ? DataResult.error(() -> "This value needs to be parsed as component") : DataResult.success(object);
	}

	public static boolean isAllowedPrimitiveArgument(@Nullable Object object) {
		return object instanceof Number || object instanceof Boolean || object instanceof String;
	}

	private static Optional<List<Object>> adjustArgs(Object[] objects) {
		return objects.length == 0 ? Optional.empty() : Optional.of(Arrays.asList(objects));
	}

	private static Object[] adjustArgs(Optional<List<Object>> optional) {
		return (Object[])optional.map(list -> list.isEmpty() ? NO_ARGS : list.toArray()).orElse(NO_ARGS);
	}

	private static TranslatableContents create(String string, Optional<String> optional, Optional<List<Object>> optional2) {
		return new TranslatableContents(string, (String)optional.orElse(null), adjustArgs(optional2));
	}

	public TranslatableContents(String string, @Nullable String string2, Object[] objects) {
		this.key = string;
		this.fallback = string2;
		this.args = objects;
	}

	@Override
	public ComponentContents.Type<?> type() {
		return TYPE;
	}

	private void decompose() {
		Language language = Language.getInstance();
		if (language != this.decomposedWith) {
			this.decomposedWith = language;
			String string = this.fallback != null ? language.getOrDefault(this.key, this.fallback) : language.getOrDefault(this.key);

			try {
				Builder<FormattedText> builder = ImmutableList.builder();
				this.decomposeTemplate(string, builder::add);
				this.decomposedParts = builder.build();
			} catch (TranslatableFormatException var4) {
				this.decomposedParts = ImmutableList.of(FormattedText.of(string));
			}
		}
	}

	private void decomposeTemplate(String string, Consumer<FormattedText> consumer) {
		Matcher matcher = FORMAT_PATTERN.matcher(string);

		try {
			int i = 0;
			int j = 0;

			while (matcher.find(j)) {
				int k = matcher.start();
				int l = matcher.end();
				if (k > j) {
					String string2 = string.substring(j, k);
					if (string2.indexOf(37) != -1) {
						throw new IllegalArgumentException();
					}

					consumer.accept(FormattedText.of(string2));
				}

				String string2 = matcher.group(2);
				String string3 = string.substring(k, l);
				if ("%".equals(string2) && "%%".equals(string3)) {
					consumer.accept(TEXT_PERCENT);
				} else {
					if (!"s".equals(string2)) {
						throw new TranslatableFormatException(this, "Unsupported format: '" + string3 + "'");
					}

					String string4 = matcher.group(1);
					int m = string4 != null ? Integer.parseInt(string4) - 1 : i++;
					consumer.accept(this.getArgument(m));
				}

				j = l;
			}

			if (j < string.length()) {
				String string5 = string.substring(j);
				if (string5.indexOf(37) != -1) {
					throw new IllegalArgumentException();
				}

				consumer.accept(FormattedText.of(string5));
			}
		} catch (IllegalArgumentException var12) {
			throw new TranslatableFormatException(this, var12);
		}
	}

	private FormattedText getArgument(int i) {
		if (i >= 0 && i < this.args.length) {
			Object object = this.args[i];
			if (object instanceof Component) {
				return (Component)object;
			} else {
				return object == null ? TEXT_NULL : FormattedText.of(object.toString());
			}
		} else {
			throw new TranslatableFormatException(this, i);
		}
	}

	@Override
	public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
		this.decompose();

		for (FormattedText formattedText : this.decomposedParts) {
			Optional<T> optional = formattedText.visit(styledContentConsumer, style);
			if (optional.isPresent()) {
				return optional;
			}
		}

		return Optional.empty();
	}

	@Override
	public <T> Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
		this.decompose();

		for (FormattedText formattedText : this.decomposedParts) {
			Optional<T> optional = formattedText.visit(contentConsumer);
			if (optional.isPresent()) {
				return optional;
			}
		}

		return Optional.empty();
	}

	@Override
	public MutableComponent resolve(@Nullable CommandSourceStack commandSourceStack, @Nullable Entity entity, int i) throws CommandSyntaxException {
		Object[] objects = new Object[this.args.length];

		for (int j = 0; j < objects.length; j++) {
			Object object = this.args[j];
			if (object instanceof Component component) {
				objects[j] = ComponentUtils.updateForEntity(commandSourceStack, component, entity, i);
			} else {
				objects[j] = object;
			}
		}

		return MutableComponent.create(new TranslatableContents(this.key, this.fallback, objects));
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			if (object instanceof TranslatableContents translatableContents
				&& Objects.equals(this.key, translatableContents.key)
				&& Objects.equals(this.fallback, translatableContents.fallback)
				&& Arrays.equals(this.args, translatableContents.args)) {
				return true;
			}

			return false;
		}
	}

	public int hashCode() {
		int i = Objects.hashCode(this.key);
		i = 31 * i + Objects.hashCode(this.fallback);
		return 31 * i + Arrays.hashCode(this.args);
	}

	public String toString() {
		return "translation{key='"
			+ this.key
			+ "'"
			+ (this.fallback != null ? ", fallback='" + this.fallback + "'" : "")
			+ ", args="
			+ Arrays.toString(this.args)
			+ "}";
	}

	public String getKey() {
		return this.key;
	}

	@Nullable
	public String getFallback() {
		return this.fallback;
	}

	public Object[] getArgs() {
		return this.args;
	}
}
