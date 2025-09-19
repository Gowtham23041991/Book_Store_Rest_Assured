package org.test.bookStore;

import api.BooksApi;
import api.UserApi;
import data.BookStoreData;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class UpdateApiTest {

    private final BookStoreData bookStoreData = new BookStoreData();
    private final Map<String, Object> bookDetails = new HashMap<>();
    private final Map<String, Object> updateBookData = new HashMap<>();
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeClass
    public void setup() throws IOException {
        // Load update data from JSON file
        loadUpdateBookDataFromJson();
    }

    /**
     * Load book update data from UpdateBooksApi.json file
     */
    @SuppressWarnings("unchecked")
    private void loadUpdateBookDataFromJson() throws IOException {
        try {
            String filePath = "src/test/resources/test.bookStore/UpdateBooksApi.json";
            File jsonFile = new File(filePath);
            Map<String, Object> jsonData = (Map<String, Object>) objectMapper.readValue(jsonFile, Map.class);
            updateBookData.putAll(jsonData);
        } catch (IOException e) {
            throw new IOException("Failed to load UpdateBooksApi.json: " + e.getMessage());
        }
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
    public void testLoginWithValidCredentials() {
        Response response = UserApi.login(bookStoreData.getValidEmailUsed(), bookStoreData.getValidPasswordUsed());
        bookStoreData.setLogInResponse(response);

        Assert.assertEquals(response.getStatusCode(), 200, "The response code is not 200");
        bookStoreData.setAccessToken("Bearer " + response.jsonPath().get("access_token"));
        Assert.assertNotNull(response.jsonPath().get("access_token"), "Token is not generated after login");
        Assert.assertEquals(response.jsonPath().get("token_type"), "bearer", "Token generated type is not bearer");
    }

    @Test(priority = 3)
    public void testAddNewBookForUpdate() {
        Long uniqueId = System.nanoTime();
        bookDetails.put("bookName", "Original Book Title " + uniqueId);
        bookDetails.put("author", "Original Author " + uniqueId);
        bookDetails.put("published_year", uniqueId);
        bookDetails.put("book_summary", "Original book summary " + uniqueId);

        Response response = BooksApi.addNewBook((HashMap<String, Object>) bookDetails, bookStoreData.getAccessToken());
        bookStoreData.setAddBookResponse(response);

        Assert.assertEquals(response.getStatusCode(), 200, "Add book expected status code mismatch");
        Assert.assertNotNull(response.getBody().jsonPath().get("id"), "Unique id is not generated");
        bookDetails.put("createdBookId", response.getBody().jsonPath().get("id"));
        Assert.assertEquals(response.getBody().jsonPath().get("name"), bookDetails.get("bookName"), "Book name mismatch");
        Assert.assertEquals(response.getBody().jsonPath().get("author"), bookDetails.get("author"), "Author name mismatch");
        Assert.assertEquals(response.getBody().jsonPath().get("published_year"), bookDetails.get("published_year"), "Published year mismatch");
        Assert.assertEquals(response.getBody().jsonPath().get("book_summary"), bookDetails.get("book_summary"), "Book summary mismatch");
    }

    @Test(priority = 4)
    public void testUpdateBookWithJsonData() {
        // Update book details with data from JSON file
        bookDetails.put("bookName", updateBookData.get("name"));
        bookDetails.put("author", updateBookData.get("author"));
        bookDetails.put("published_year", updateBookData.get("published_year"));
        bookDetails.put("book_summary", updateBookData.get("book_summary"));

        Response response = BooksApi.editTheBook((HashMap<String, Object>) bookDetails, bookStoreData.getAccessToken());
        bookStoreData.setEditBookResponse(response);

        Assert.assertEquals(response.getStatusCode(), 200, "The response code is not 200");
        Assert.assertEquals(response.getStatusLine(), "HTTP/1.1 200 OK", "Response line is not as expected for 200");

        // Verify updated data matches JSON data
        Assert.assertEquals(response.getBody().jsonPath().get("name"), updateBookData.get("name"), "Updated book name mismatch with JSON data");
        Assert.assertEquals(response.getBody().jsonPath().get("author"), updateBookData.get("author"), "Updated author name mismatch with JSON data");
        Assert.assertEquals(response.getBody().jsonPath().get("published_year"), updateBookData.get("published_year"), "Updated published year mismatch with JSON data");
        Assert.assertEquals(response.getBody().jsonPath().get("book_summary"), updateBookData.get("book_summary"), "Updated book summary mismatch with JSON data");
        Assert.assertEquals(response.getBody().jsonPath().get("id"), bookDetails.get("createdBookId"), "Book id should remain unchanged");
    }

    @Test(priority = 5)
    public void testGetUpdatedBookDetailsById() {
        Response response = BooksApi.getBookDetailsById((HashMap<String, Object>) bookDetails, bookStoreData.getAccessToken());
        bookStoreData.setGetBookDetailsById(response);

        Assert.assertEquals(response.getStatusCode(), 200, "Get book details expected status code mismatch");
        
        // Verify retrieved data matches the updated JSON data
        Assert.assertEquals(response.getBody().jsonPath().get("name"), updateBookData.get("name"), "Retrieved book name mismatch with JSON data");
        Assert.assertEquals(response.getBody().jsonPath().get("author"), updateBookData.get("author"), "Retrieved author name mismatch with JSON data");
        Assert.assertEquals(response.getBody().jsonPath().get("published_year"), updateBookData.get("published_year"), "Retrieved published year mismatch with JSON data");
        Assert.assertEquals(response.getBody().jsonPath().get("book_summary"), updateBookData.get("book_summary"), "Retrieved book summary mismatch with JSON data");
        Assert.assertEquals(response.getBody().jsonPath().get("id"), bookDetails.get("createdBookId"), "Book id mismatch");
    }

    @Test(priority = 6)
    public void testUpdateBookWithPartialJsonData() {
        // Test partial update - only update name and author from JSON, keep other fields
        Map<String, Object> partialUpdateDetails = new HashMap<>(bookDetails);
        partialUpdateDetails.put("bookName", updateBookData.get("name") + " - Partial Update");
        partialUpdateDetails.put("author", updateBookData.get("author") + " - Updated");

        Response response = BooksApi.editTheBook((HashMap<String, Object>) partialUpdateDetails, bookStoreData.getAccessToken());

        Assert.assertEquals(response.getStatusCode(), 200, "Partial update expected status code mismatch");
        Assert.assertEquals(response.getBody().jsonPath().get("name"), partialUpdateDetails.get("bookName"), "Partial updated book name mismatch");
        Assert.assertEquals(response.getBody().jsonPath().get("author"), partialUpdateDetails.get("author"), "Partial updated author name mismatch");
        Assert.assertEquals(response.getBody().jsonPath().get("published_year"), partialUpdateDetails.get("published_year"), "Published year should remain unchanged");
        Assert.assertEquals(response.getBody().jsonPath().get("book_summary"), partialUpdateDetails.get("book_summary"), "Book summary should remain unchanged");

        // Update bookDetails for subsequent tests
        bookDetails.putAll(partialUpdateDetails);
    }

    @Test(priority = 7)
    public void testUpdateBookWithInvalidData() {
        // Test update with invalid published year (string instead of number)
        Map<String, Object> invalidUpdateDetails = new HashMap<>(bookDetails);
        invalidUpdateDetails.put("published_year", "invalid_year");

        Response response = BooksApi.editTheBook((HashMap<String, Object>) invalidUpdateDetails, bookStoreData.getAccessToken());

        // Expecting either 400 Bad Request or the API to handle the conversion gracefully
        // Adjust assertion based on your API's behavior
        Assert.assertTrue(response.getStatusCode() == 400 || response.getStatusCode() == 200, 
            "Update with invalid data should return 400 or handle gracefully");
    }

    @Test(priority = 8)
    public void testFetchAllBooksContainsUpdatedBook() {
        Response response = (Response) BooksApi.getAllBooks(bookStoreData.getAccessToken());

        Assert.assertEquals(response.getStatusCode(), 200, "Get all books expected status code mismatch");
        Assert.assertTrue(response.getBody().asString().contains(bookDetails.get("bookName").toString()), 
            "Updated book name not found in all books");
        Assert.assertTrue(response.getBody().asString().contains(bookDetails.get("author").toString()), 
            "Updated author not found in all books");
    }

    @Test(priority = 9)
    public void testDeleteUpdatedBook() {
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

        Assert.assertEquals(response.getStatusCode(), 404, "Expected 404 for deleted book");
        Assert.assertEquals(response.getBody().jsonPath().get("detail"), "Book not found", "Book details should not be fetched for deleted");
    }

    @Test(priority = 11)
    public void testMultipleUpdatesWithJsonData() {
        // Create a new book for multiple update testing
        Long uniqueId = System.nanoTime();
        Map<String, Object> multiUpdateBookDetails = new HashMap<>();
        multiUpdateBookDetails.put("bookName", "Multi Update Book " + uniqueId);
        multiUpdateBookDetails.put("author", "Multi Update Author " + uniqueId);
        multiUpdateBookDetails.put("published_year", uniqueId);
        multiUpdateBookDetails.put("book_summary", "Multi update summary " + uniqueId);

        // Add new book
        Response addResponse = BooksApi.addNewBook((HashMap<String, Object>) multiUpdateBookDetails, bookStoreData.getAccessToken());
        Assert.assertEquals(addResponse.getStatusCode(), 200, "Add book for multi-update test failed");
        multiUpdateBookDetails.put("createdBookId", addResponse.getBody().jsonPath().get("id"));

        // First update with JSON data
        multiUpdateBookDetails.put("bookName", updateBookData.get("name"));
        multiUpdateBookDetails.put("author", updateBookData.get("author"));
        Response firstUpdateResponse = BooksApi.editTheBook((HashMap<String, Object>) multiUpdateBookDetails, bookStoreData.getAccessToken());
        Assert.assertEquals(firstUpdateResponse.getStatusCode(), 200, "First update failed");

        // Second update - modify summary from JSON data
        multiUpdateBookDetails.put("book_summary", updateBookData.get("book_summary") + " - Second Update");
        Response secondUpdateResponse = BooksApi.editTheBook((HashMap<String, Object>) multiUpdateBookDetails, bookStoreData.getAccessToken());
        Assert.assertEquals(secondUpdateResponse.getStatusCode(), 200, "Second update failed");
        Assert.assertEquals(secondUpdateResponse.getBody().jsonPath().get("book_summary"), 
            multiUpdateBookDetails.get("book_summary"), "Second update summary mismatch");

        // Clean up
        BooksApi.deleteTheBookById(multiUpdateBookDetails.get("createdBookId").toString(), bookStoreData.getAccessToken());
    }
}
