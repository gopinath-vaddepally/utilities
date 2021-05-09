package util;

import exception.CommonException;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author gvaddepally on 10/06/20
 */
public class ConditionCheck {

    public static void isBlank(String text, String message) {
        if (StringUtils.isBlank(text))
            throw new CommonException(message, 400, 400);
    }

    public static void isTrue(Boolean result, String message) {
        if (result)
            throw new CommonException(message, 400, 400);
    }

    public static void isFalse(Boolean result, String message) {
        if (!result)
            throw new CommonException(message, 400, 400);
    }

    public static <T> void isNull(T obj, String message) {
        if (Objects.isNull(obj))
            throw new CommonException(message, 400, 400);
    }

    public static void isValueZero(int value, String message) {
        if (value == 0)
            throw new CommonException(message, 400, 400);
    }
}
