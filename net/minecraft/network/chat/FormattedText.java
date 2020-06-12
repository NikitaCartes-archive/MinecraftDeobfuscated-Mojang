/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Unit;

public interface FormattedText {
    public static final Optional<Unit> STOP_ITERATION = Optional.of(Unit.INSTANCE);
    public static final FormattedText EMPTY = new FormattedText(){

        @Override
        public <T> Optional<T> visit(ContentConsumer<T> contentConsumer) {
            return Optional.empty();
        }

        @Override
        @Environment(value=EnvType.CLIENT)
        public <T> Optional<T> visit(StyledContentConsumer<T> styledContentConsumer, Style style) {
            return Optional.empty();
        }
    };

    public <T> Optional<T> visit(ContentConsumer<T> var1);

    @Environment(value=EnvType.CLIENT)
    public <T> Optional<T> visit(StyledContentConsumer<T> var1, Style var2);

    public static FormattedText of(final String string) {
        return new FormattedText(){

            @Override
            public <T> Optional<T> visit(ContentConsumer<T> contentConsumer) {
                return contentConsumer.accept(string);
            }

            @Override
            @Environment(value=EnvType.CLIENT)
            public <T> Optional<T> visit(StyledContentConsumer<T> styledContentConsumer, Style style) {
                return styledContentConsumer.accept(style, string);
            }
        };
    }

    @Environment(value=EnvType.CLIENT)
    public static FormattedText of(final String string, final Style style) {
        return new FormattedText(){

            @Override
            public <T> Optional<T> visit(ContentConsumer<T> contentConsumer) {
                return contentConsumer.accept(string);
            }

            @Override
            public <T> Optional<T> visit(StyledContentConsumer<T> styledContentConsumer, Style style2) {
                return styledContentConsumer.accept(style.applyTo(style2), string);
            }
        };
    }

    @Environment(value=EnvType.CLIENT)
    public static FormattedText composite(FormattedText ... formattedTexts) {
        return FormattedText.composite(ImmutableList.copyOf(formattedTexts));
    }

    @Environment(value=EnvType.CLIENT)
    public static FormattedText composite(final List<FormattedText> list) {
        return new FormattedText(){

            @Override
            public <T> Optional<T> visit(ContentConsumer<T> contentConsumer) {
                for (FormattedText formattedText : list) {
                    Optional<T> optional = formattedText.visit(contentConsumer);
                    if (!optional.isPresent()) continue;
                    return optional;
                }
                return Optional.empty();
            }

            @Override
            public <T> Optional<T> visit(StyledContentConsumer<T> styledContentConsumer, Style style) {
                for (FormattedText formattedText : list) {
                    Optional<T> optional = formattedText.visit(styledContentConsumer, style);
                    if (!optional.isPresent()) continue;
                    return optional;
                }
                return Optional.empty();
            }
        };
    }

    default public String getString() {
        StringBuilder stringBuilder = new StringBuilder();
        this.visit(string -> {
            stringBuilder.append(string);
            return Optional.empty();
        });
        return stringBuilder.toString();
    }

    public static interface ContentConsumer<T> {
        public Optional<T> accept(String var1);
    }

    @Environment(value=EnvType.CLIENT)
    public static interface StyledContentConsumer<T> {
        public Optional<T> accept(Style var1, String var2);
    }
}

