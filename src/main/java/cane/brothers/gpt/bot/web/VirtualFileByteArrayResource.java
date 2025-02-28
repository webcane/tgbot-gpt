package cane.brothers.gpt.bot.web;

import org.springframework.core.io.AbstractResource;
import org.springframework.util.Assert;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class VirtualFileByteArrayResource extends AbstractResource {

    private final InputStream is;
    private final String description;

    public VirtualFileByteArrayResource(InputStream is) {
        Assert.notNull(is, "InputStream must not be null");
        this.is = is;
        this.description = "Virtual file loaded from input stream";
    }

    @Override
    public String getFilename() {
        return "voice.oga";
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new BufferedInputStream(this.is);
    }
}
