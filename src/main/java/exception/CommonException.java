package exception;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author gvaddepally on 18/05/20
 */

@Getter
@Setter
@ToString
public class CommonException extends RuntimeException {

    private String message;
    private int httpStatusCode;
    private int errorCode;
    private List<String> details;

    public CommonException(String message) {
        this(message,(Throwable) null);
    }

    public CommonException(Throwable cause){
        this(cause.getMessage(),cause);
    }

    public CommonException(String message, Throwable cause) {
        this(message,cause,null);
    }

    public CommonException(String message, Throwable cause, Integer errorCode){
        this(message,cause,errorCode,null);
    }

    public CommonException(String message, int errorCode){
        this(message,null,errorCode);
    }

    public CommonException(String message, int errorCode, int httpStatusCode){
        this(message,null,errorCode,httpStatusCode);
    }

    public CommonException(String message, Throwable cause, Integer errorCode, Integer httpStatusCode){
        super(message,cause);
        if(errorCode == null){
            errorCode = 1001;
        }
        if(httpStatusCode == null){
            httpStatusCode = 500;
        }
        if(message == null){
            message = "Something went wrong, please try again after some time or reach tech support.";
        }
        this.message = message;
        this.httpStatusCode = httpStatusCode;
        this.errorCode = errorCode;
    }

}
