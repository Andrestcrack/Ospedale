package utils; 
public class Response {
    
    private int statusCode;
    private String message;
    private String serializedData; // Aquí enviaremos la info en texto plano (ej. JSON) para cumplir la regla

    // Constructor para respuestas simples (ej. un registro exitoso o un error)
    public Response(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
        this.serializedData = "";
    }

    // Constructor para cuando necesitamos devolver datos a la vista (ej. buscar un paciente)
    public Response(int statusCode, String message, String serializedData) {
        this.statusCode = statusCode;
        this.message = message;
        this.serializedData = serializedData;
    }

    // Getters para que la vista pueda leer la respuesta
    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public String getSerializedData() {
        return serializedData;
    }
}