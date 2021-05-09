package util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jose4j.base64url.Base64;
import org.slf4j.helpers.MessageFormatter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * @author gvaddepally on 23/05/20
 */
public final class CommonUtils {

    public static <T> T convertJSONtoObject(String json, Class<T> className) {
        ObjectMapper objectMapper = new ObjectMapper();
        T result = objectMapper.convertValue(json, className);
        return result;
    }

    public static <T> T convertObjectTOObject(Object fromObject, Class<T> toObject) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        T result = objectMapper.convertValue(fromObject, toObject);
        return result;
    }

    public static <T> String convertObjectToJSON(T object) {
        ObjectMapper objectMapper = new ObjectMapper();
        String result = null;
        try {
            result = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String msg(String msg, Object... args) {
        return MessageFormatter.arrayFormat(msg, args).getMessage();
    }

    public static String createAndGetUniqueOrgId(String orgName) {
        String name = Arrays.stream(orgName.split(" ")).map(s -> String.valueOf(s.charAt(0)).toUpperCase()).distinct().collect(Collectors.joining());
        int n = 100000 + new Random().nextInt(900000);
        String result = String.join("", name, String.valueOf(n));
        return result;
    }

    public static String createAndGetUniquePatientId(String patientName) {
        String name = Arrays.stream(patientName.split(" ")).map(s -> String.valueOf(s.charAt(0)).toUpperCase()).distinct().collect(Collectors.joining());
        int n = 10000000 + new Random().nextInt(90000000);
        String result = String.join("", name, String.valueOf(n));
        return result;
    }

    public static String compressBytes(byte[] data) {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        try {
            outputStream.close();
        } catch (IOException e) {
        }
        System.out.println("Compressed Image Byte Size - " + outputStream.toByteArray().length);
        return Base64.encode(outputStream.toByteArray());
    }

    public static String decompressBytes(byte[] data) {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
        } catch (IOException | DataFormatException ioe) {

        }
        return Base64.encode(outputStream.toByteArray());
    }

    public static void main(String[] args) {
        byte[] encode = java.util.Base64.getEncoder().encode("hello world".getBytes());
        String s1 = compressBytes(encode);
        System.out.println(s1);
        String s2 = decompressBytes(Base64.decode(s1));
        System.out.println(s2);
        System.out.println("decode val " + Base64.decode(Base64.encode(s2.getBytes())));
    }
}
