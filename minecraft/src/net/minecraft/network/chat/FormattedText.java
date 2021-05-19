package net.minecraft.network.chat;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Unit;

public interface FormattedText {
	Optional<Unit> STOP_ITERATION = Optional.of(Unit.INSTANCE);
	FormattedText EMPTY = new FormattedText() {
		@Override
		public <T> Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
			return Optional.empty();
		}

		@Override
		public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
			return Optional.empty();
		}
	};

	<T> Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer);

	<T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style);

	static FormattedText of(String string) {
		return new FormattedText() {
			@Override
			public <T> Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
				return contentConsumer.accept(string);
			}

			@Override
			public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
				return styledContentConsumer.accept(style, string);
			}
		};
	}

	static FormattedText of(String string, Style style) {
		return new FormattedText() {
			@Override
			public <T> Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
				return contentConsumer.accept(string);
			}

			@Override
			public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
				return styledContentConsumer.accept(style.applyTo(style), string);
			}
		};
	}

	static FormattedText composite(FormattedText... formattedTexts) {
		return composite(ImmutableList.copyOf(formattedTexts));
	}

	static FormattedText composite(List<? extends FormattedText> list) {
		return new FormattedText() {
			@Override
			public <T> Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
				for (FormattedText formattedText : list) {
					Optional<T> optional = formattedText.visit(contentConsumer);
					if (optional.isPresent()) {
						return optional;
					}
				}

				return Optional.empty();
			}

			@Override
			public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
				for (FormattedText formattedText : list) {
					Optional<T> optional = formattedText.visit(styledContentConsumer, style);
					if (optional.isPresent()) {
						return optional;
					}
				}

				return Optional.empty();
			}
		};
	}

	default String getString() {
		StringBuilder stringBuilder = new StringBuilder();
		this.visit(string -> {
			stringBuilder.append(string);
			return Optional.empty();
		});
		return stringBuilder.toString();
	}

	public interface ContentConsumer<T> {
		Optional<T> accept(String string);
	}

	public interface StyledContentConsumer<T> {
		Optional<T> accept(Style style, String string);
	}
}
