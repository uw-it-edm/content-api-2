package edu.uw.edm.contentapi2.service.util;

import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StreamUtils {

    public static void streamCopy(InputStream input, OutputStream output) throws IOException {
        log.debug("Using stream copy");
        FileCopyUtils.copy(input, output);
        log.debug("Done copying");
    }

    public static void channelCopy(InputStream input, OutputStream output) throws IOException {
        ReadableByteChannel inputChannel = null;
        WritableByteChannel outputChannel = null;

        try {
            inputChannel = Channels.newChannel(input);
            outputChannel = Channels.newChannel(output);

            log.debug("Using channel copy");
            fastCopy(inputChannel, outputChannel);
            log.debug("Done copying");
        } finally {
            IOUtils.closeQuietly(outputChannel);
            IOUtils.closeQuietly(inputChannel);
        }
    }

    private static void fastCopy(final ReadableByteChannel src, final WritableByteChannel dest) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);

        while (src.read(buffer) != -1) {
            buffer.flip();
            dest.write(buffer);
            buffer.compact();
        }

        buffer.flip();

        while (buffer.hasRemaining()) {
            dest.write(buffer);
        }
    }
}
