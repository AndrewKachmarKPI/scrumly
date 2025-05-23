package com.scrumly.userservice.userservice.utils;

import com.scrumly.exceptions.types.ServiceErrorException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ImageUtils {

    public static final int BITE_SIZE = 4 * 1024;

    public static byte[] compressImage(byte[] data) {
        ByteArrayOutputStream outputStream = null;
        try {
            Deflater deflater = new Deflater();
            deflater.setLevel(Deflater.BEST_COMPRESSION);
            deflater.setInput(data);
            deflater.finish();
            outputStream = new ByteArrayOutputStream(data.length);
            byte[] tmp = new byte[BITE_SIZE];

            while (!deflater.finished()) {
                int size = deflater.deflate(tmp);
                outputStream.write(tmp, 0, size);
            }

            outputStream.close();
        } catch (IOException e) {
            throw new ServiceErrorException(e);
        }

        return outputStream.toByteArray();
    }

    public static byte[] decompressImage(byte[] data) {
        ByteArrayOutputStream outputStream = null;
        try {
            Inflater inflater = new Inflater();
            inflater.setInput(data);
            outputStream = new ByteArrayOutputStream(data.length);
            byte[] tmp = new byte[BITE_SIZE];

            while (!inflater.finished()) {
                int count = inflater.inflate(tmp);
                outputStream.write(tmp, 0, count);
            }

            outputStream.close();
        } catch (DataFormatException | IOException e) {
            throw new ServiceErrorException(e);
        }

        return outputStream.toByteArray();
    }
}
