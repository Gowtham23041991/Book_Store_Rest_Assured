package api;

import constants.BookStoreEndPoints;
import data.BookStoreData;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Random;

import static io.restassured.RestAssured.*;

public class UserApi {

    public static String generateEmailAndPassword(int length)
    {
        String candidateChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder stringGenerated = new StringBuilder ();
        Random random = new Random();
        for (int i = 0; i < length; i ++) {
            stringGenerated.append (candidateChars.charAt (random.nextInt (candidateChars
                    .length ())));
        }
        return stringGenerated.toString ();
    }

    public static Response signUp(String email,String password,BookStoreData bookStoreData)
    {
        return given().contentType(ContentType.JSON)
                .body("{\"email\":\""+email+"\",\"password\":\""+password+"\"}")
                .log().all()
                .when().post(BookStoreEndPoints.SING_UP)
                .then().log().all().extract().response();
    }

    public static Response login(String email,String password)
    {
        RequestSpecification request = given().contentType(ContentType.JSON).log().all();
        if(email!=null && password!=null)
        {
            request.body("{\"email\":\""+email+"\",\"password\":\""+password+"\"}");
        }
        return request.when().post(BookStoreEndPoints.LOG_IN)
                .then().log().all().extract().response();
    }

}
