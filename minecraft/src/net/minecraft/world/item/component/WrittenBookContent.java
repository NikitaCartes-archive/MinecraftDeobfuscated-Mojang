package net.minecraft.world.item.component;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.network.Filterable;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;

public record WrittenBookContent(Filterable<String> title, String author, int generation, List<Filterable<Component>> pages, boolean resolved)
	implements BookContent<Component, WrittenBookContent> {
	public static final WrittenBookContent EMPTY = new WrittenBookContent(Filterable.passThrough(""), "", 0, List.of(), true);
	public static final int PAGE_LENGTH = 32767;
	public static final int MAX_PAGES = 100;
	public static final int TITLE_LENGTH = 16;
	public static final int TITLE_MAX_LENGTH = 32;
	public static final int MAX_GENERATION = 3;
	public static final int MAX_CRAFTABLE_GENERATION = 2;
	public static final Codec<Component> CONTENT_CODEC = ComponentSerialization.flatCodec(32767);
	public static final Codec<List<Filterable<Component>>> PAGES_CODEC = pagesCodec(CONTENT_CODEC);
	public static final Codec<WrittenBookContent> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Filterable.codec(ExtraCodecs.sizeLimitedString(0, 32)).fieldOf("title").forGetter(WrittenBookContent::title),
					Codec.STRING.fieldOf("author").forGetter(WrittenBookContent::author),
					ExtraCodecs.strictOptionalField(ExtraCodecs.intRange(0, 3), "generation", 0).forGetter(WrittenBookContent::generation),
					ExtraCodecs.strictOptionalField(PAGES_CODEC, "pages", List.of()).forGetter(WrittenBookContent::pages),
					ExtraCodecs.strictOptionalField(Codec.BOOL, "resolved", false).forGetter(WrittenBookContent::resolved)
				)
				.apply(instance, WrittenBookContent::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, WrittenBookContent> STREAM_CODEC = StreamCodec.composite(
		Filterable.streamCodec(ByteBufCodecs.stringUtf8(32)),
		WrittenBookContent::title,
		ByteBufCodecs.STRING_UTF8,
		WrittenBookContent::author,
		ByteBufCodecs.VAR_INT,
		WrittenBookContent::generation,
		Filterable.streamCodec(ComponentSerialization.STREAM_CODEC).apply(ByteBufCodecs.list(100)),
		WrittenBookContent::pages,
		ByteBufCodecs.BOOL,
		WrittenBookContent::resolved,
		WrittenBookContent::new
	);

	private static Codec<Filterable<Component>> pageCodec(Codec<Component> codec) {
		return Filterable.codec(codec);
	}

	public static Codec<List<Filterable<Component>>> pagesCodec(Codec<Component> codec) {
		return ExtraCodecs.sizeLimitedList(pageCodec(codec).listOf(), 100);
	}

	@Nullable
	public WrittenBookContent tryCraftCopy() {
		return this.generation >= 2 ? null : new WrittenBookContent(this.title, this.author, this.generation + 1, this.pages, this.resolved);
	}

	@Nullable
	public WrittenBookContent resolve(CommandSourceStack commandSourceStack, @Nullable Player player) {
		if (this.resolved) {
			return null;
		} else {
			Builder<Filterable<Component>> builder = ImmutableList.builderWithExpectedSize(this.pages.size());

			for (Filterable<Component> filterable : this.pages) {
				Optional<Filterable<Component>> optional = resolvePage(commandSourceStack, player, filterable);
				if (optional.isEmpty()) {
					return null;
				}

				builder.add((Filterable<Component>)optional.get());
			}

			return new WrittenBookContent(this.title, this.author, this.generation, builder.build(), true);
		}
	}

	public WrittenBookContent markResolved() {
		return new WrittenBookContent(this.title, this.author, this.generation, this.pages, true);
	}

	private static Optional<Filterable<Component>> resolvePage(CommandSourceStack commandSourceStack, @Nullable Player player, Filterable<Component> filterable) {
		return filterable.resolve(component -> {
			try {
				Component component2 = ComponentUtils.updateForEntity(commandSourceStack, component, player, 0);
				return isPageTooLarge(component2, commandSourceStack.registryAccess()) ? Optional.empty() : Optional.of(component2);
			} catch (Exception var4) {
				return Optional.of(component);
			}
		});
	}

	private static boolean isPageTooLarge(Component component, HolderLookup.Provider provider) {
		return Component.Serializer.toJson(component, provider).length() > 32767;
	}

	public List<Component> getPages(boolean bl) {
		return Lists.transform(this.pages, filterable -> (Component)filterable.get(bl));
	}

	public WrittenBookContent withReplacedPages(List<Filterable<Component>> list) {
		return new WrittenBookContent(this.title, this.author, this.generation, list, false);
	}
}
