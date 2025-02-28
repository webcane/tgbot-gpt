package cane.brothers.gpt.bot.web;

import org.springframework.core.io.InputStreamResource;

import java.io.InputStream;

/**
 * Works with {@link VirtualFileResourceHttpMessageConverter} to forward input stream from
 * file-uploads without reading everything into memory.
 */
public class VirtualFileByteArrayResource extends InputStreamResource {

    public VirtualFileByteArrayResource(InputStream is) {
        super(is);
    }

    @Override
    public String getFilename() {
//        return "voice.oga";
        return "audio.webm";
    }

    @Override
    public long contentLength() {
        return -1; // we do not want to generally read the whole stream into memory ...
    }
}
