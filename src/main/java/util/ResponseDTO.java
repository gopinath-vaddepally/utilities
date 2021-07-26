package util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.ToString;

import java.util.List;

/**
 * @author Gopinath Vaddepally
 * createdOn 25/07/21
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class ResponseDTO<T> {
    private boolean status;
    private T data;
    private String message;
    private List<String> details;

    public boolean isStatus() {
        return status;
    }

    public ResponseDTO<T> setStatus(boolean status) {
        this.status = status;
        return this;
    }

    public T getData() {
        return data;
    }

    public ResponseDTO<T> setData(T data) {
        this.data = data;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ResponseDTO<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public List<String> getDetails() {
        return details;
    }

    public ResponseDTO<T> setDetails(List<String> details) {
        this.details = details;
        return this;
    }
}
