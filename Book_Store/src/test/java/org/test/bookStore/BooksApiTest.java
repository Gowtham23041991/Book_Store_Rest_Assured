package org.test.bookStore;

import api.BooksApi;
import api.UserApi;
import data.BookStoreData;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.*;

public class BooksApiTest {

    private final BookStoreData bookStoreData = new BookStoreData();
    private final Map<String, Object> bookDetails = new HashMap<>();
    private final List<Map<String, Object>> allBooksList = new ArrayList<>();


    @BeforeClass
    public void setup() {
        // Any setup if required
    }

    @Test(priority = 1)
    public void testUserSignUpWithValidCredentials() {
        bookStoreData.setValidEmailUsed(UserApi.generateEmailAndPassword(10) + "@gmail.com");
        bookStoreData.setValidPasswordUsed(UserApi.generateEmailAndPassword(8));
        Response response = UserApi.signUp(bookStoreData.getValidEmailUsed(), bookStoreData.getValidPasswordUsed(), bookStoreData);
        bookStoreData.setSignUpResponse(response);

        Assert.assertEquals(response.getStatusCode(), 200, "Sign up expected status code mismatch");
        Assert.assertEquals(response.getBody().jsonPath().get("message"), "User created successfully", "User is not created");
    }

    @Test(priority = 2)
    public void testSignUpWithOldCredentials() {
        Response response = UserApi.signUp(bookStoreData.getValidEmailUsed(), bookStoreData.getValidPasswordUsed(), bookStoreData);
        bookStoreData.setSignUpResponse(response);

        Assert.assertEquals(response.getStatusCode(), 400, "Sign up expected status code mismatch");
        Assert.assertEquals(response.getBody().jsonPath().get("detail"), "Email already registered", "Expected error message mismatch");
    }

    @Test(priority = 3)
    public void testSignUpWithNewPasswordOnly() {
        bookStoreData.setValidPasswordUsed(UserApi.generateEmailAndPassword(8));
        Response response = UserApi.signUp(null, bookStoreData.getValidPasswordUsed(), bookStoreData);
        bookStoreData.setSignUpResponse(response);

        Assert.assertEquals(response.getStatusCode(), 400, "Sign up expected status code mismatch");
        Assert.assertEquals(response.getBody().jsonPath().get("detail"), "Email already registered", "Expected error message mismatch");
    }

    @Test(priority = 4)
    public void testLoginWithValidCredentials() {
        Response response = UserApi.login(bookStoreData.getValidEmailUsed(), bookStoreData.getValidPasswordUsed());
        bookStoreData.setLogInResponse(response);

        Assert.assertEquals(response.getStatusCode(), 200, "The response code is not 200");
        bookStoreData.setAccessToken("Bearer " + response.jsonPath().get("access_token"));
        Assert.assertNotNull(response.jsonPath().get("access_token"), "Token is not generated after login");
        Assert.assertEquals(response.jsonPath().get("token_type"), "bearer", "Token generated type is not bearer");
    }

    @Test(priority = 5)
    public void testAddNewBookAfterLogin() {

        Long uniqueId = System.nanoTime();
        bookDetails.put("bookName", "Book Title " + uniqueId);
        bookDetails.put("author", "Book Author " + uniqueId);
        bookDetails.put("published_year", uniqueId);
        bookDetails.put("book_summary", "Book summary for the book " + uniqueId);
        allBooksList.add(new HashMap<>(bookDetails));


        Response response = BooksApi.addNewBook((HashMap<String, Object>) bookDetails, bookStoreData.getAccessToken());
        bookStoreData.setAddBookResponse(response);

        Assert.assertNotNull(response.getBody().jsonPath().get("id"), "Unique id is not generated");
        bookDetails.put("createdBookId", response.getBody().jsonPath().get("id"));
        Assert.assertEquals(response.getBody().jsonPath().get("name"), bookDetails.get("bookName"), "Book name mismatch");
        Assert.assertEquals(response.getBody().jsonPath().get("author"), bookDetails.get("author"), "Author name mismatch");
        Assert.assertEquals(response.getBody().jsonPath().get("published_year"), bookDetails.get("published_year"), "Published year mismatch");
        Assert.assertEquals(response.getBody().jsonPath().get("book_summary"), bookDetails.get("book_summary"), "Book summary mismatch");
    }

    @Test(priority = 6)
    public void testEditBookNameAndVerify() {
        bookDetails.put("bookName", "Book name is edited now");

        Response response = BooksApi.editTheBook((HashMap<String, Object>) bookDetails, bookStoreData.getAccessToken());
        bookStoreData.setEditBookResponse(response);

        Assert.assertEquals(response.getStatusCode(), 200, "The response code is not 200");
        Assert.assertEquals(response.getStatusLine(), "HTTP/1.1 200 OK", "Response line is not as expected for 200");

        Assert.assertEquals(response.getBody().jsonPath().get("name"), bookDetails.get("bookName"), "Book name mismatch");
        Assert.assertEquals(response.getBody().jsonPath().get("author"), bookDetails.get("author"), "Author name mismatch");
        Assert.assertEquals(response.getBody().jsonPath().get("published_year"), bookDetails.get("published_year"), "Published year mismatch");
        Assert.assertEquals(response.getBody().jsonPath().get("book_summary"), bookDetails.get("book_summary"), "Book summary mismatch");
        Assert.assertEquals(response.getBody().jsonPath().get("id"), bookDetails.get("createdBookId"), "Book id mismatch");
    }

    @Test(priority = 7)
    public void testGetBookDetailsById() {
        Response response = BooksApi.getBookDetailsById((HashMap<String, Object>) bookDetails, bookStoreData.getAccessToken());
        bookStoreData.setGetBookDetailsById(response);

        Assert.assertEquals(response.getBody().jsonPath().get("name"), bookDetails.get("bookName"), "Book name mismatch");
        Assert.assertEquals(response.getBody().jsonPath().get("author"), bookDetails.get("author"), "Author name mismatch");
        Assert.assertEquals(response.getBody().jsonPath().get("published_year"), bookDetails.get("published_year"), "Published year mismatch");
        Assert.assertEquals(response.getBody().jsonPath().get("book_summary"), bookDetails.get("book_summary"), "Book summary mismatch");
        Assert.assertEquals(response.getBody().jsonPath().get("id"), bookDetails.get("createdBookId"), "Book id mismatch");
    }

    @Test(priority = 8)
    public void testFetchAllBooks() {
        Response response = (Response) BooksApi.getAllBooks(bookStoreData.getAccessToken());
        bookStoreData.setFetchAllBooks((List<Response>) response);

        for (Map<String, Object> eachData : allBooksList) {
            Assert.assertTrue(response.getBody().asString().contains(eachData.get("bookName").toString()), "Book name not found in all books");
        }
    }

    @Test(priority = 9)
    public void testDeleteBook() {
        Response response = BooksApi.deleteTheBookById(bookDetails.get("createdBookId").toString(), bookStoreData.getAccessToken());
        bookStoreData.setDeleteBookResponse(response);

        Assert.assertEquals(response.getStatusCode(), 200, "The response code is not 200");
        Assert.assertEquals(response.getStatusLine(), "HTTP/1.1 200 OK", "Response line is not as expected");
        Assert.assertEquals(response.getBody().jsonPath().get("message"), "Book deleted successfully", "Book not deleted yet");
    }

    @Test(priority = 10)
    public void testGetDeletedBookShouldFail() {
        Response response = BooksApi.getBookDetailsById((HashMap<String, Object>) bookDetails, bookStoreData.getAccessToken());
        bookStoreData.setGetBookDetailsById(response);

        Assert.assertEquals(response.getBody().jsonPath().get("detail"), "Book not found", "Book details should not be fetched for deleted");
    }
}

