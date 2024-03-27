package net.minecraft.network.chat.contents;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;

public class NbtContents implements ComponentContents {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final MapCodec<NbtContents> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					Codec.STRING.fieldOf("nbt").forGetter(NbtContents::getNbtPath),
					Codec.BOOL.lenientOptionalFieldOf("interpret", Boolean.valueOf(false)).forGetter(NbtContents::isInterpreting),
					ComponentSerialization.CODEC.lenientOptionalFieldOf("separator").forGetter(NbtContents::getSeparator),
					DataSource.CODEC.forGetter(NbtContents::getDataSource)
				)
				.apply(instance, NbtContents::new)
	);
	public static final ComponentContents.Type<NbtContents> TYPE = new ComponentContents.Type<>(CODEC, "nbt");
	private final boolean interpreting;
	private final Optional<Component> separator;
	private final String nbtPathPattern;
	private final DataSource dataSource;
	@Nullable
	protected final NbtPathArgument.NbtPath compiledNbtPath;

	public NbtContents(String string, boolean bl, Optional<Component> optional, DataSource dataSource) {
		this(string, compileNbtPath(string), bl, optional, dataSource);
	}

	private NbtContents(String string, @Nullable NbtPathArgument.NbtPath nbtPath, boolean bl, Optional<Component> optional, DataSource dataSource) {
		this.nbtPathPattern = string;
		this.compiledNbtPath = nbtPath;
		this.interpreting = bl;
		this.separator = optional;
		this.dataSource = dataSource;
	}

	@Nullable
	private static NbtPathArgument.NbtPath compileNbtPath(String string) {
		try {
			return new NbtPathArgument().parse(new StringReader(string));
		} catch (CommandSyntaxException var2) {
			return null;
		}
	}

	public String getNbtPath() {
		return this.nbtPathPattern;
	}

	public boolean isInterpreting() {
		return this.interpreting;
	}

	public Optional<Component> getSeparator() {
		return this.separator;
	}

	public DataSource getDataSource() {
		return this.dataSource;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			if (object instanceof NbtContents nbtContents
				&& this.dataSource.equals(nbtContents.dataSource)
				&& this.separator.equals(nbtContents.separator)
				&& this.interpreting == nbtContents.interpreting
				&& this.nbtPathPattern.equals(nbtContents.nbtPathPattern)) {
				return true;
			}

			return false;
		}
	}

	public int hashCode() {
		int i = this.interpreting ? 1 : 0;
		i = 31 * i + this.separator.hashCode();
		i = 31 * i + this.nbtPathPattern.hashCode();
		return 31 * i + this.dataSource.hashCode();
	}

	public String toString() {
		return "nbt{" + this.dataSource + ", interpreting=" + this.interpreting + ", separator=" + this.separator + "}";
	}

	@Override
	public MutableComponent resolve(@Nullable CommandSourceStack commandSourceStack, @Nullable Entity entity, int i) throws CommandSyntaxException {
		if (commandSourceStack != null && this.compiledNbtPath != null) {
			Stream<String> stream = this.dataSource.getData(commandSourceStack).flatMap(compoundTag -> {
				try {
					return this.compiledNbtPath.get(compoundTag).stream();
				} catch (CommandSyntaxException var3) {
					return Stream.empty();
				}
			}).map(Tag::getAsString);
			if (this.interpreting) {
				Component component = DataFixUtils.orElse(
					ComponentUtils.updateForEntity(commandSourceStack, this.separator, entity, i), ComponentUtils.DEFAULT_NO_STYLE_SEPARATOR
				);
				return (MutableComponent)stream.flatMap(string -> {
					try {
						MutableComponent mutableComponent = Component.Serializer.fromJson(string, commandSourceStack.registryAccess());
						return Stream.of(ComponentUtils.updateForEntity(commandSourceStack, mutableComponent, entity, i));
					} catch (Exception var5x) {
						LOGGER.warn("Failed to parse component: {}", string, var5x);
						return Stream.of();
					}
				}).reduce((mutableComponent, mutableComponent2) -> mutableComponent.append(component).append(mutableComponent2)).orElseGet(Component::empty);
			} else {
				return (MutableComponent)ComponentUtils.updateForEntity(commandSourceStack, this.separator, entity, i)
					.map(
						mutableComponent -> (MutableComponent)stream.map(Component::literal)
								.reduce((mutableComponent2, mutableComponent3) -> mutableComponent2.append(mutableComponent).append(mutableComponent3))
								.orElseGet(Component::empty)
					)
					.orElseGet(() -> Component.literal((String)stream.collect(Collectors.joining(", "))));
			}
		} else {
			return Component.empty();
		}
	}

	@Override
	public ComponentContents.Type<?> type() {
		return TYPE;
	}
}
