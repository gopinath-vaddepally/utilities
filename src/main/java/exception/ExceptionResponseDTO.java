package exception;

import java.util.List;

/**
 * @author gvaddepally on 23/05/20
 */
public class ExceptionResponseDTO {
    private String message;
    private int errorCode;
    private List<String> details;
    private CommonException commonException;

    public ExceptionResponseDTO(CommonException commonException) {
        this.message = commonException.getMessage();
        this.errorCode = commonException.getErrorCode();
        this.details = commonException.getDetails();
    }

    public String getMessage() {
        return message;
    }

    public ExceptionResponseDTO setMessage(String message) {
        this.message = message;
        return this;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public ExceptionResponseDTO setErrorCode(int errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public List<String> getDetails() {
        return details;
    }

    public ExceptionResponseDTO setDetails(List<String> details) {
        this.details = details;
        return this;
    }

    @Override
    public String toString() {
        return "ExceptionResponseDTO{" +
                "message='" + message + '\'' +
                ", errorCode=" + errorCode +
                ", details=" + details +
                '}';
    }
}
