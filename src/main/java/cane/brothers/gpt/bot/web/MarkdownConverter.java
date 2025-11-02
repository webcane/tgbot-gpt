package cane.brothers.gpt.bot.web;

import cane.brothers.tg.md.convert.MarkdownToTelegramConverter;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;


@Component
public class MarkdownConverter implements Converter<String, String> {

    final MarkdownToTelegramConverter mdConverter = new MarkdownToTelegramConverter();

    @Override
    public String convert(@NotNull String source) {
        return mdConverter.convert(source);
    }
}
