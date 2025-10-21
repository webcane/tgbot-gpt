package cane.brothers.gpt.bot.telegram;

import cane.brothers.gpt.bot.telegram.commands.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Represents an answer to be sent via Telegram, composed of multiple parts.
 * Each part can specify whether it should be escaped for Telegram formatting.
 */
public record TgAnswer(List<TgAnswerPart> parts) implements Utils {

    public static Builder builder() {
        return new Builder();
    }

    public int length() {
        return parts.stream().mapToInt(part -> part.value().length()).sum();
    }

    /**
     * Combines all parts into a single text string, applying the provided escaper function
     * to parts that require escaping.
     */
    public String toText(Function<String, String> escaper) {
        StringBuilder sb = new StringBuilder();
        for (TgAnswerPart part : parts) {
            sb.append(part.isEscape ? escaper.apply(part.value) : part.value);
        }
        return sb.toString();
    }

    public static class Builder {
        private final List<TgAnswerPart> parts = new ArrayList<>();

        public Builder addPart(String value) {
            parts.add(new TgAnswerPart(value, true));
            return this;
        }

        public Builder addPart(String value, boolean isEscape) {
            parts.add(new TgAnswerPart(value, isEscape));
            return this;
        }

        public Builder addPart(TgAnswerPart part) {
            parts.add(part);
            return this;
        }

        public TgAnswer build() {
            return new TgAnswer(List.copyOf(parts));
        }
    }

    public record TgAnswerPart(String value, boolean isEscape) {
    }
}
