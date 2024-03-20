package net.minecraft.commands.arguments.item;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.util.parsing.packrat.Atom;
import net.minecraft.util.parsing.packrat.Dictionary;
import net.minecraft.util.parsing.packrat.Term;
import net.minecraft.util.parsing.packrat.commands.Grammar;
import net.minecraft.util.parsing.packrat.commands.ResourceLocationParseRule;
import net.minecraft.util.parsing.packrat.commands.ResourceLookupRule;
import net.minecraft.util.parsing.packrat.commands.StringReaderTerms;
import net.minecraft.util.parsing.packrat.commands.TagParseRule;

public class ComponentPredicateParser {
	public static <T, C, P> Grammar<List<T>> createGrammar(ComponentPredicateParser.Context<T, C, P> context) {
		Atom<List<T>> atom = Atom.of("top");
		Atom<Optional<T>> atom2 = Atom.of("type");
		Atom<Unit> atom3 = Atom.of("any_type");
		Atom<T> atom4 = Atom.of("element_type");
		Atom<T> atom5 = Atom.of("tag_type");
		Atom<List<T>> atom6 = Atom.of("conditions");
		Atom<List<T>> atom7 = Atom.of("alternatives");
		Atom<T> atom8 = Atom.of("term");
		Atom<T> atom9 = Atom.of("negation");
		Atom<T> atom10 = Atom.of("test");
		Atom<C> atom11 = Atom.of("component_type");
		Atom<P> atom12 = Atom.of("predicate_type");
		Atom<ResourceLocation> atom13 = Atom.of("id");
		Atom<Tag> atom14 = Atom.of("tag");
		Dictionary<StringReader> dictionary = new Dictionary<>();
		dictionary.put(
			atom,
			Term.alternative(
				Term.sequence(Term.named(atom2), StringReaderTerms.character('['), Term.cut(), Term.optional(Term.named(atom6)), StringReaderTerms.character(']')),
				Term.named(atom2)
			),
			scope -> {
				Builder<T> builder = ImmutableList.builder();
				scope.getOrThrow(atom2).ifPresent(builder::add);
				List<T> list = scope.get(atom6);
				if (list != null) {
					builder.addAll(list);
				}

				return builder.build();
			}
		);
		dictionary.put(
			atom2,
			Term.alternative(Term.named(atom4), Term.sequence(StringReaderTerms.character('#'), Term.cut(), Term.named(atom5)), Term.named(atom3)),
			scope -> Optional.ofNullable(scope.getAny(atom4, atom5))
		);
		dictionary.put(atom3, StringReaderTerms.character('*'), scope -> Unit.INSTANCE);
		dictionary.put(atom4, new ComponentPredicateParser.ElementLookupRule<>(atom13, context));
		dictionary.put(atom5, new ComponentPredicateParser.TagLookupRule<>(atom13, context));
		dictionary.put(atom6, Term.sequence(Term.named(atom7), Term.optional(Term.sequence(StringReaderTerms.character(','), Term.named(atom6)))), scope -> {
			T object = context.anyOf(scope.getOrThrow(atom7));
			return (List<T>)Optional.ofNullable(scope.get(atom6)).map(list -> Util.copyAndAdd(object, list)).orElse(List.of(object));
		});
		dictionary.put(atom7, Term.sequence(Term.named(atom8), Term.optional(Term.sequence(StringReaderTerms.character('|'), Term.named(atom7)))), scope -> {
			T object = scope.getOrThrow(atom8);
			return (List<T>)Optional.ofNullable(scope.get(atom7)).map(list -> Util.copyAndAdd(object, list)).orElse(List.of(object));
		});
		dictionary.put(
			atom8, Term.alternative(Term.named(atom10), Term.sequence(StringReaderTerms.character('!'), Term.named(atom9))), scope -> scope.getAnyOrThrow(atom10, atom9)
		);
		dictionary.put(atom9, Term.named(atom10), scope -> context.negate(scope.getOrThrow(atom10)));
		dictionary.put(
			atom10,
			Term.alternative(
				Term.sequence(Term.named(atom11), StringReaderTerms.character('='), Term.cut(), Term.named(atom14)),
				Term.sequence(Term.named(atom12), StringReaderTerms.character('~'), Term.cut(), Term.named(atom14)),
				Term.named(atom11)
			),
			(parseState, scope) -> {
				P object = scope.get(atom12);

				try {
					if (object != null) {
						Tag tag = scope.getOrThrow(atom14);
						return Optional.of(context.createPredicateTest(parseState.input(), object, tag));
					} else {
						C object2 = scope.getOrThrow(atom11);
						Tag tag2 = scope.get(atom14);
						return Optional.of(
							tag2 != null ? context.createComponentTest(parseState.input(), object2, tag2) : context.createComponentTest(parseState.input(), object2)
						);
					}
				} catch (CommandSyntaxException var9x) {
					parseState.errorCollector().store(parseState.mark(), var9x);
					return Optional.empty();
				}
			}
		);
		dictionary.put(atom11, new ComponentPredicateParser.ComponentLookupRule<>(atom13, context));
		dictionary.put(atom12, new ComponentPredicateParser.PredicateLookupRule<>(atom13, context));
		dictionary.put(atom14, TagParseRule.INSTANCE);
		dictionary.put(atom13, ResourceLocationParseRule.INSTANCE);
		return new Grammar<>(dictionary, atom);
	}

	static class ComponentLookupRule<T, C, P> extends ResourceLookupRule<ComponentPredicateParser.Context<T, C, P>, C> {
		ComponentLookupRule(Atom<ResourceLocation> atom, ComponentPredicateParser.Context<T, C, P> context) {
			super(atom, context);
		}

		@Override
		protected C validateElement(ImmutableStringReader immutableStringReader, ResourceLocation resourceLocation) throws Exception {
			return this.context.lookupComponentType(immutableStringReader, resourceLocation);
		}

		@Override
		public Stream<ResourceLocation> possibleResources() {
			return this.context.listComponentTypes();
		}
	}

	public interface Context<T, C, P> {
		T forElementType(ImmutableStringReader immutableStringReader, ResourceLocation resourceLocation) throws CommandSyntaxException;

		Stream<ResourceLocation> listElementTypes();

		T forTagType(ImmutableStringReader immutableStringReader, ResourceLocation resourceLocation) throws CommandSyntaxException;

		Stream<ResourceLocation> listTagTypes();

		C lookupComponentType(ImmutableStringReader immutableStringReader, ResourceLocation resourceLocation) throws CommandSyntaxException;

		Stream<ResourceLocation> listComponentTypes();

		T createComponentTest(ImmutableStringReader immutableStringReader, C object, Tag tag) throws CommandSyntaxException;

		T createComponentTest(ImmutableStringReader immutableStringReader, C object);

		P lookupPredicateType(ImmutableStringReader immutableStringReader, ResourceLocation resourceLocation) throws CommandSyntaxException;

		Stream<ResourceLocation> listPredicateTypes();

		T createPredicateTest(ImmutableStringReader immutableStringReader, P object, Tag tag) throws CommandSyntaxException;

		T negate(T object);

		T anyOf(List<T> list);
	}

	static class ElementLookupRule<T, C, P> extends ResourceLookupRule<ComponentPredicateParser.Context<T, C, P>, T> {
		ElementLookupRule(Atom<ResourceLocation> atom, ComponentPredicateParser.Context<T, C, P> context) {
			super(atom, context);
		}

		@Override
		protected T validateElement(ImmutableStringReader immutableStringReader, ResourceLocation resourceLocation) throws Exception {
			return this.context.forElementType(immutableStringReader, resourceLocation);
		}

		@Override
		public Stream<ResourceLocation> possibleResources() {
			return this.context.listElementTypes();
		}
	}

	static class PredicateLookupRule<T, C, P> extends ResourceLookupRule<ComponentPredicateParser.Context<T, C, P>, P> {
		PredicateLookupRule(Atom<ResourceLocation> atom, ComponentPredicateParser.Context<T, C, P> context) {
			super(atom, context);
		}

		@Override
		protected P validateElement(ImmutableStringReader immutableStringReader, ResourceLocation resourceLocation) throws Exception {
			return this.context.lookupPredicateType(immutableStringReader, resourceLocation);
		}

		@Override
		public Stream<ResourceLocation> possibleResources() {
			return this.context.listPredicateTypes();
		}
	}

	static class TagLookupRule<T, C, P> extends ResourceLookupRule<ComponentPredicateParser.Context<T, C, P>, T> {
		TagLookupRule(Atom<ResourceLocation> atom, ComponentPredicateParser.Context<T, C, P> context) {
			super(atom, context);
		}

		@Override
		protected T validateElement(ImmutableStringReader immutableStringReader, ResourceLocation resourceLocation) throws Exception {
			return this.context.forTagType(immutableStringReader, resourceLocation);
		}

		@Override
		public Stream<ResourceLocation> possibleResources() {
			return this.context.listTagTypes();
		}
	}
}
