package response;

/**
 *
 * @author msand
 */
public class Response<T> {

    private final StatusCode status;
    private final String message;
    private final T data;

    public Response(StatusCode status,
                    String message,
                    T data) {

        this.status = status;
        this.message = message;
        this.data = data;
    }

    public StatusCode getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
