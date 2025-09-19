package org.test.bookStore;


import api.UserApi;
import data.BookStoreData;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class UserApiTest {

    private BookStoreData bookStoreData;

    @BeforeClass
    public void setup() {
        bookStoreData = new BookStoreData();
    }

    @Test(priority = 1)
    public void testSignUpWithValidCredentials() {
        String email = UserApi.generateEmailAndPassword(10) + "@gmail.com";
        String password = UserApi.generateEmailAndPassword(8);
        bookStoreData.setValidEmailUsed(email);
        bookStoreData.setValidPasswordUsed(password);

        Response response = UserApi.signUp(email, password, bookStoreData);
        bookStoreData.setSignUpResponse(response);

        Assert.assertEquals(response.getStatusCode(), 200, "Sign up expected status code mismatch");
        Assert.assertEquals(response.getBody().jsonPath().get("message").toString(), "User created successfully", "User is not created");
    }

    @Test(priority = 2)
    public void testSignUpWithExistingCredentials() {
        Response response = UserApi.signUp(bookStoreData.getValidEmailUsed(), bookStoreData.getValidPasswordUsed(), bookStoreData);
        bookStoreData.setSignUpResponse(response);

        Assert.assertEquals(response.getStatusCode(), 400, "Sign up expected status code mismatch");
        Assert.assertEquals(response.getBody().jsonPath().get("detail").toString(), "Email already registered", "There is no error thrown");
    }

    @Test(priority = 3)
    public void testSignUpWithNewPasswordOnly() {
        String newPassword = UserApi.generateEmailAndPassword(8);
        bookStoreData.setValidPasswordUsed(newPassword);

        Response response = UserApi.signUp(bookStoreData.getValidEmailUsed(), newPassword, bookStoreData);
        bookStoreData.setSignUpResponse(response);

        Assert.assertEquals(response.getStatusCode(), 400, "Sign up expected status code mismatch");
        Assert.assertEquals(response.getBody().jsonPath().get("detail").toString(), "Email already registered", "There is no error thrown");
    }

    @Test(priority = 4)
    public void testLoginAfterSignupWithValidCredentials() {
        Response response = UserApi.login(bookStoreData.getValidEmailUsed(), bookStoreData.getValidPasswordUsed());
        bookStoreData.setLogInResponse(response);

        Assert.assertEquals(response.getStatusCode(), 200, "Login expected status code mismatch");
        String token = response.jsonPath().get("access_token");
        Assert.assertNotNull(token, "Token is not generated after login");
        Assert.assertEquals(response.jsonPath().get("token_type"), "bearer", "Token type mismatch");
        bookStoreData.setAccessToken("Bearer " + token);
    }

    @Test(priority = 5)
    public void testLoginWithoutSignup() {
        String email = UserApi.generateEmailAndPassword(10) + "@gmail.com";
        String password = UserApi.generateEmailAndPassword(8);

        Response response = UserApi.login(email, password);
        bookStoreData.setLogInResponse(response);

        Assert.assertEquals(response.getStatusCode(), 400, "Login expected status code mismatch");
        Assert.assertEquals(response.getStatusLine(), "HTTP/1.1 400 Bad Request", "Response line mismatch");
        Assert.assertEquals(response.jsonPath().get("detail"), "Incorrect email or password", "Error detail mismatch");
    }

    @Test(priority = 6)
    public void testLoginWithMissingParams() {
        Response response = UserApi.login(null, null);
        bookStoreData.setLogInResponse(response);

        Assert.assertEquals(response.getStatusCode(), 422, "Login expected status code mismatch");
        Assert.assertEquals(response.getStatusLine(), "HTTP/1.1 422 Unprocessable Entity", "Response line mismatch");
        Assert.assertEquals(response.jsonPath().get("detail[0].type"), "missing", "Missing param error type mismatch");
        Assert.assertEquals(response.jsonPath().get("detail[0].msg"), "Field required", "Missing param message mismatch");
    }
}
