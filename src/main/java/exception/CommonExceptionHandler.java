package exception;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static util.CommonUtils.msg;

/**
 * @author gvaddepally on 23/05/20
 */
@ControllerAdvice
public class CommonExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        CommonException commonException = null;
        List<String> details = new ArrayList<>();
        details.add(ex.getLocalizedMessage());
        if (ex instanceof CommonException) {
            commonException = (CommonException) ex;
        } else {
            commonException = new CommonException(ex.getMessage(), ex);
        }
        commonException.setDetails(details);
        int httpStatusCode = commonException.getHttpStatusCode();
        ExceptionResponseDTO exceptionResponseDTO = new ExceptionResponseDTO(commonException);
        LOGGER.error("Error while performing operation due to ::: ", ex);
        return new ResponseEntity(exceptionResponseDTO, HttpStatus.valueOf(httpStatusCode));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<String> details = new ArrayList<>();
        for (ObjectError error : ex.getBindingResult().getAllErrors()) {
            details.add(error.getDefaultMessage());
        }
        CommonException commonException = new CommonException("Validation Failed");
        commonException.setDetails(details);
        ExceptionResponseDTO exceptionResponseDTO = new ExceptionResponseDTO(commonException);
        return new ResponseEntity(exceptionResponseDTO, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String message = msg("Validation failed");
        List<String> details = Collections.singletonList(ex.getLocalizedMessage());
        CommonException commonException = new CommonException(message);
        commonException.setDetails(details);
        ExceptionResponseDTO exceptionResponseDTO = new ExceptionResponseDTO(commonException);
        return new ResponseEntity(exceptionResponseDTO, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String name = ex.getParameterName();
        logger.error(name + " parameter is missing");
        List<String> details = Lists.newArrayList(name);
        CommonException commonException = new CommonException(" parameter is missing");
        commonException.setDetails(details);
        ExceptionResponseDTO exceptionResponseDTO = new ExceptionResponseDTO(commonException);
        return new ResponseEntity(exceptionResponseDTO, HttpStatus.BAD_REQUEST);
    }
}
