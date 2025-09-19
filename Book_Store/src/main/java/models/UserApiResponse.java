package models;

import lombok.Data;

@Data
public class UserApiResponse {

    private String responseMessage;
    private String message;

    public UserApiResponse(String responseMessage, String message) {
        this.responseMessage = responseMessage;
        this.message = message;
    }
}
