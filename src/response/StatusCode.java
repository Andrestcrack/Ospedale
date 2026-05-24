package response;

/**
 *
 * @author msand
 */
public enum StatusCode {

    // 2xx
    OK(200),
    CREATED(201),

    // 4xx
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    NOT_FOUND(404),

    // 5xx
    INTERNAL_ERROR(500);

    private final int code;

    StatusCode(int code){
        this.code = code;
    }

    public int getCode(){
        return code;
    }
}
