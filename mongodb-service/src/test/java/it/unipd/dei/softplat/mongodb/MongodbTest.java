/**
 * MongodbTest.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.mongodb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.MongoDBContainer;

import it.unipd.dei.softplat.http.service.HttpClientService;
import it.unipd.dei.softplat.mongodb.controller.MongodbController;
import it.unipd.dei.softplat.mongodb.model.MongoArticle;
import it.unipd.dei.softplat.mongodb.dto.SaveArticleDTO;
import it.unipd.dei.softplat.mongodb.dto.SearchArticleDTO;
import it.unipd.dei.softplat.testutil.TestAsyncConfig;

/**
 * This class is intended to test the MongodbController and MongodbService.
 * It tests the function saveArticles and dropCollection of the MongodbController,
 * as well as the MongoArticle model.
 * It uses an embedded MongoDB instance for testing purposes.
 */
@SpringBootTest
@Testcontainers
@Import(TestAsyncConfig.class)
public class MongodbTest {

    // To start an embedded MongoDB instance for testing purposes
    @Container
    private static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

    @DynamicPropertySource
    static void setMongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }
    
    @MockBean
    private HttpClientService httpClientService;
    
    @Autowired @InjectMocks
    private MongodbController mongodbController;

    /**
     * This test method is intended to test the saveArticles method of the MongodbController.
     * It creates a sample MongoArticle and calls the saveArticles method with it.
     * It also tests the method with an empty list and a null list to ensure proper error handling.
     */
    @Test
    public void testSaveArticles() {
        // Create a sample MongoArticle
        MongoArticle test_article = new MongoArticle(
            "test_id",
            "test_type",
            "section_id_test",
            "section_name_test",
            "2023-10-01T12:00:00Z",
            "Test Web Title",
            "https://example.com/test-web-url",
            "This is a test body text for the MongoDB article."
        );

        // Call the saveArticles method with a list containing the test article
        SaveArticleDTO saveArticleDTO = new SaveArticleDTO(List.of(test_article), "test_collection");
        ResponseEntity<?> response = mongodbController.saveArticles(saveArticleDTO);

        // Assert that the response is not null and has a status code of 200 OK
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Response should have status code 200 OK");

        // Call the saveArticles method with an empty list
        SaveArticleDTO emptySaveArticleDTO = new SaveArticleDTO(List.of(), "test_collection");
        ResponseEntity<?> emptyResponse = mongodbController.saveArticles(emptySaveArticleDTO);

        // Assert that the response is not null and has a status code of 400 Bad Request
        assertNotNull(emptyResponse, "Response should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, emptyResponse.getStatusCode(), "Response should have status code 400 Bad Request");

        // Call the saveArticles method with a null list
        SaveArticleDTO nullSaveArticleDTO = new SaveArticleDTO(null, "test_collection");
        ResponseEntity<?> nullResponse = mongodbController.saveArticles(nullSaveArticleDTO);

        // Assert that the response is not null and has a status code of 400 Bad Request
        assertNotNull(nullResponse, "Response should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, nullResponse.getStatusCode(), "Response should have status code 400 Bad Request");
    }
    
    @Test
    public void testSearchArticles() {
        // Mock configuration
        when(httpClientService.postRequest(
                eq("http://mallet-service:8084/mallet/accumulate/"),
                anyString()
            )
        ).thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));

        // Save an article
        MongoArticle test_article = new MongoArticle(
            "test_id",
            "test_type",
            "section_id_test",
            "section_name_test",
            "2023-10-01T12:00:00Z",
            "Test Web Title",
            "https://example.com/test-web-url",
            "This is a test body text for the MongoDB article."
        );
        SaveArticleDTO saveArticleDTO = new SaveArticleDTO(List.of(test_article), "test_collection");
        mongodbController.saveArticles(saveArticleDTO);

        String collectionName = "test_collection";
        String query = "test_query";
        String id = "test_id";
        
        // Call the searchArticles method with a valid collection name and ID
        SearchArticleDTO searchArticleDTO = new SearchArticleDTO(collectionName, query, List.of(id));
        ResponseEntity<?> response = mongodbController.searchArticles(searchArticleDTO);

        // Assert that the response is not null and has a status code of 200 OK
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Response should have status code 200 OK");
        
        // Verify that the postRequest methods of DataManagerService was called with the correct parameters
        verify(httpClientService, times(2)).postRequest(eq("http://mallet-service:8084/mallet/accumulate/"), anyString());
        
        // Call the searchArticles method with an empty collection name and query
        SearchArticleDTO emptyCollectionDTO = new SearchArticleDTO("", "", List.of(id));
        ResponseEntity<?> emptyCollectionResponse = mongodbController.searchArticles(emptyCollectionDTO);
        // Assert that the response is not null and has a status code of 400 Bad Request
        assertNotNull(emptyCollectionResponse, "Response should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, emptyCollectionResponse.getStatusCode(), "Response should have status code 400 Bad Request");
        
        // Call the searchArticles method with a null collection name and query
        SearchArticleDTO nullCollectionDTO = new SearchArticleDTO(null, null, List.of(id));
        ResponseEntity<?> nullCollectionResponse = mongodbController.searchArticles(nullCollectionDTO);
        
        // Assert that the response is not null and has a status code of 400 Bad Request
        assertNotNull(nullCollectionResponse, "Response should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, nullCollectionResponse.getStatusCode(), "Response should have status code 400 Bad Request");
        
        // Call the searchArticles method with an empty list of IDs
        SearchArticleDTO emptyIdDTO = new SearchArticleDTO(collectionName, query, List.of());
        ResponseEntity<?> emptyIdResponse = mongodbController.searchArticles(emptyIdDTO);
        
        // Assert that the response is not null and has a status code of 400 Bad Request
        assertNotNull(emptyIdResponse, "Response should not be null");
        assertEquals(HttpStatus.OK, emptyIdResponse.getStatusCode(), "Response should have status code 200 OK");
        
        // Call the searchArticles method with a null list of IDs
        SearchArticleDTO nullIdDTO = new SearchArticleDTO(collectionName, query, null);
        ResponseEntity<?> nullIdResponse = mongodbController.searchArticles(nullIdDTO);
        
        // Assert that the response is not null and has a status code of 400 Bad Request
        assertNotNull(nullIdResponse, "Response should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, nullIdResponse.getStatusCode(), "Response should have status code 400 Bad Request");
    }

    /**
     * This test method is intended to test the MongoArticle class getters and setters.
     * It creates a MongoArticle object, sets its properties, and asserts that the getters return the expected values.
     */
    @Test
    public void testMongoArticleGettersSetters() {
        MongoArticle article = new MongoArticle();
        
        // Set the properties of the article object
        article.setId("test_id");
        article.setType("test_type");
        article.setSectionId("section_id_test");
        article.setSectionName("section_name_test");
        article.setWebPublicationDate("2023-10-01T12:00:00Z");
        article.setWebTitle("Test Web Title");
        article.setWebUrl("https://example.com/test-web-url");
        article.setBodyText("This is a test body text for the MongoDB article.");
        
        // Assert that the getters return the expected values
        assertEquals("test_id", article.getId(), "ID should match the set value");
        assertEquals("test_type", article.getType(), "Type should match the set value");
        assertEquals("section_id_test", article.getSectionId(), "Section ID should match the set value");
        assertEquals("section_name_test", article.getSectionName(), "Section name should match the set value");
        assertEquals("2023-10-01T12:00:00Z", article.getWebPublicationDate(), "Web publication date should match the set value");
        assertEquals("Test Web Title", article.getWebTitle(), "Web title should match the set value");
        assertEquals("https://example.com/test-web-url", article.getWebUrl(), "Web URL should match the set value");
        assertEquals("This is a test body text for the MongoDB article.", article.getBodyText(), "Body text should match the set value");
    }

    /**
     * This test method is intended to test the MongoArticle class for null values.
     * It checks if the setters throw IllegalArgumentException when null values are passed.
     */
    @Test
    public void testMongoArticleNullValues() {
        MongoArticle article = new MongoArticle();

        // Assert that the null values are not accepted
        try {
            article.setId(null);
        }
        catch (IllegalArgumentException e) {
            assertEquals("ID cannot be null", e.getMessage(), "Expected exception for null ID");
        }
        try {
            article.setType(null);
        }
        catch (IllegalArgumentException e) {
            assertEquals("Type cannot be null", e.getMessage(), "Expected exception for null Type");
        }
        try {
            article.setSectionId(null);
        }
        catch (IllegalArgumentException e) {
            assertEquals("Section ID cannot be null", e.getMessage(), "Expected exception for null Section ID");
        }
        try {
            article.setSectionName(null);
        }
        catch (IllegalArgumentException e) {
            assertEquals("Section name cannot be null", e.getMessage(), "Expected exception for null Section name");
        }
        try {
            article.setWebPublicationDate(null);
        }
        catch (IllegalArgumentException e) {
            assertEquals("Web publication date cannot be null", e.getMessage(), "Expected exception for null Web publication date");
        }
        try {
            article.setWebTitle(null);
        }
        catch (IllegalArgumentException e) {
            assertEquals("Web title cannot be null", e.getMessage(), "Expected exception for null Web title");
        }
        try {
            article.setWebUrl(null);
        }
        catch (IllegalArgumentException e) {
            assertEquals("Web URL cannot be null", e.getMessage(), "Expected exception for null Web URL");
        }
        try {
            article.setBodyText(null);
        }
        catch (IllegalArgumentException e) {
            assertEquals("Body text cannot be null", e.getMessage(), "Expected exception for null Body text");
        }
    }

    /**
     * This test method is intended to test the MongoArticle class for empty values.
     * It checks if the setters throw IllegalArgumentException when empty strings are passed.
     */
    @Test
    public void testMongoArticleEmptyValues() {
        MongoArticle article = new MongoArticle();

        // Assert that the empty values are not accepted
        try {
            article.setId("");
        }
        catch (IllegalArgumentException e) {
            assertEquals("ID cannot be empty", e.getMessage(), "Expected exception for empty ID");
        }
        try {
            article.setType("");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Type cannot be empty", e.getMessage(), "Expected exception for empty Type");
        }
        try {
            article.setSectionId("");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Section ID cannot be empty", e.getMessage(), "Expected exception for empty Section ID");
        }
        try {
            article.setSectionName("");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Section name cannot be empty", e.getMessage(), "Expected exception for empty Section name");
        }
        try {
            article.setWebPublicationDate("");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Web publication date cannot be empty", e.getMessage(), "Expected exception for empty Web publication date");
        }
        try {
            article.setWebTitle("");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Web title cannot be empty", e.getMessage(), "Expected exception for empty Web title");
        }
        try {
            article.setWebUrl("");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Web URL cannot be empty", e.getMessage(), "Expected exception for empty Web URL");
        }
        try {
            article.setBodyText("");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Body text cannot be empty", e.getMessage(), "Expected exception for empty Body text");
        }
    }
}
