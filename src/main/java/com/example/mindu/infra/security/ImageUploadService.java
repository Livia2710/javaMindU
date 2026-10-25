package com.example.mindu.infra.security;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

@Component
public class ImageUploadService {
    private static final long MAX_INPUT_BYTES = 5L * 1024 * 1024;
    private static final long MAX_PIXELS = 12_000_000L;

    public byte[] normalizar(MultipartFile file) {
        if (file == null) return null;
        if (file.isEmpty() || file.getSize() > MAX_INPUT_BYTES)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Imagem vazia ou maior que 5 MB.");
        try {
            byte[] input = file.getBytes();
            try (ImageInputStream stream = ImageIO.createImageInputStream(new ByteArrayInputStream(input))) {
                if (stream == null) throw imagemInvalida();
                Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
                if (!readers.hasNext()) throw imagemInvalida();
                ImageReader reader = readers.next();
                try {
                    reader.setInput(stream, true, true);
                    String format = reader.getFormatName();
                    if (!format.equalsIgnoreCase("png") && !format.equalsIgnoreCase("jpeg") && !format.equalsIgnoreCase("jpg"))
                        throw imagemInvalida();
                    int width = reader.getWidth(0), height = reader.getHeight(0);
                    if (width < 1 || height < 1 || (long) width * height > MAX_PIXELS)
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dimensões da imagem não permitidas.");
                    BufferedImage decoded = reader.read(0);
                    if (decoded == null) throw imagemInvalida();
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    if (!ImageIO.write(decoded, "png", output)) throw imagemInvalida();
                    if (output.size() > MAX_INPUT_BYTES)
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Imagem processada maior que 5 MB.");
                    return output.toByteArray();
                } finally {
                    reader.dispose();
                }
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            throw imagemInvalida();
        }
    }

    private ResponseStatusException imagemInvalida() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie uma imagem PNG ou JPEG válida.");
    }
}
