/**
 * DataManagerTest.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.datamanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import it.unipd.dei.softplat.datamanager.controller.DataManagerController;
import it.unipd.dei.softplat.datamanager.model.Article;
import it.unipd.dei.softplat.http.service.HttpClientService;

/**
 * This class contains unit tests for the DataManagerController.
 * It tests the getArticles method and the validation of the Article object.
 */
@SpringBootTest
public class DataManagerTest {

    @MockBean
    private HttpClientService httpClientService;
    
    @Autowired @InjectMocks
    private DataManagerController controller_test;

    /**
     * This test method is intended to test the saveArticles method of the DataManagerController.
     * It creates a valid and an invalid ArticleTopics object and calls the saveArticles method.
     * It asserts that the response is not null and has the expected status code.
     */
    @Test
    public void testSaveArticles() {
        // Mock configuration for MongoDB and ElasticSearch services
        when(httpClientService.postRequest(
                eq("http://mongodb-service:8085/mongodb/save/"),
                anyString()
            )
        ).thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));
        when(httpClientService.postRequest(
                eq("http://elasticsearch-service:8083/elastic/index/"),
                anyString()
            )
        ).thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));

        // Example of a valid ArticleTopics object
        Article test_article = new Article();
        // Set the properties of the test_article object
        test_article.setId("test-id");
        test_article.setissueString("test issue query");
        test_article.setLabel("test label");
        test_article.setType("test type");
        test_article.setSectionId("test section id");
        test_article.setSectionName("test section name");
        Calendar cal = Calendar.getInstance();
        cal.set(2023, Calendar.OCTOBER, 1, 12, 0, 0);
        Date webPublicationDate = cal.getTime();
        test_article.setWebPublicationDate(webPublicationDate);
        test_article.setWebTitle("Test Web Title");
        test_article.setWebUrl("https://example.com/test-article");
        test_article.setBodyText("This is a test body text for the article.");
    
        // Call saveArticles method
        ResponseEntity <?> response = controller_test.saveArticles(List.of(test_article));
        // Assert that the response is not null and has a status code of 200 OK
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Response should have status code 200 OK");

        // Verify that the postRequest methods of DataManagerService was called with the correct parameters
        verify(httpClientService).postRequest(eq("http://mongodb-service:8085/mongodb/save/"), anyString());
        verify(httpClientService).postRequest(eq("http://elasticsearch-service:8083/elastic/index/"), anyString());

        // Example of an invalid ArticleTopics object
        List<Article> invalid_article_list = new ArrayList<>();

        // Call saveArticles method with an empty list of articles
        ResponseEntity <?> invalid_response = controller_test.saveArticles(invalid_article_list);
        // Assert that the response is not null and has a status code of 400 Bad Request
        assertNotNull(invalid_response, "Response should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, invalid_response.getStatusCode(), "Response should have status code 400 Bad Request");

        // Call saveArticles with null
        ResponseEntity <?> null_response = controller_test.saveArticles(null);
        // Assert that the response is not null and has a status code of 400 Bad Request
        assertNotNull(null_response, "Response should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, null_response.getStatusCode(), "Response should have status code 400 Bad Request");
    }

    /**
     * This test method is intended to test the validation of the Article object.
     * It checks all the getters and setters of the Article class.
     * It asserts that the getters return the expected values after setting them.
     */
     @Test
     public void testArticleGetterSetter() {
            Article article = new Article();
    
            // Set the properties of the article object
            article.setId("test-id");
            article.setissueString("test issue query");
            article.setLabel("test label");
            article.setType("test type");
            article.setSectionId("test section id");
            article.setSectionName("test section name");
            Calendar cal = Calendar.getInstance();
            cal.set(2023, Calendar.OCTOBER, 1, 12, 0, 0);
            Date webPublicationDate = cal.getTime();
            article.setWebPublicationDate(webPublicationDate);
            article.setWebTitle("Test Web Title");
            article.setWebUrl("https://example.com/test-article");
            article.setBodyText("This is a test body text for the article.");
    
            // Assert that the getters return the expected values
            assertEquals("test-id", article.getId());
            assertEquals("test issue query", article.getissueString());
            assertEquals("test label", article.getLabel());
            assertEquals("test type", article.getType());
            assertEquals("test section id", article.getSectionId());
            assertEquals("test section name", article.getSectionName());
            assertEquals(webPublicationDate, article.getWebPublicationDate());
            assertEquals("Test Web Title", article.getWebTitle());
            assertEquals("https://example.com/test-article", article.getWebUrl());
            assertEquals("This is a test body text for the article.", article.getBodyText());
     }

     /**
      * This test method is intended to test the validation of the Article object for null values.
      * It checks if the id, issue query, label, type, section id, section name, web publication date,
      * web title, web url, and body text are not null.
      * If any of these values are null, it should throw an IllegalArgumentException.
      */
     @Test
     public void testArticleNullValues() {
        Article article = new Article();
        
        // Assert that the null values are not accepted
        try {
        article.setId(null);
        }
        catch (IllegalArgumentException e) {
            assertEquals("Id cannot be null", e.getMessage(), "Expected exception for null ID");
        }
        try {
            article.setissueString(null);
        }
        catch (IllegalArgumentException e) {
            assertEquals("Issue query cannot be null", e.getMessage(), "Expected exception for null issue query");
        }
        try {
            article.setLabel(null);
        }
        catch (IllegalArgumentException e) {
            assertEquals("Label cannot be null", e.getMessage(), "Expected exception for null label");
        }
        try {
            article.setType(null);
        }
        catch (IllegalArgumentException e) {
            assertEquals("Type cannot be null", e.getMessage(), "Expected exception for null type");
        }
        try {
            article.setSectionId(null);
        }
        catch (IllegalArgumentException e) {
            assertEquals("Section ID cannot be null", e.getMessage(), "Expected exception for null section ID");
        }
        try {
            article.setSectionName(null);
        }
        catch (IllegalArgumentException e) {
            assertEquals("Section name cannot be null", e.getMessage(), "Expected exception for null section name");
        }
        try {
            article.setWebPublicationDate(null);
        }
        catch (IllegalArgumentException e) {
            assertEquals("Web publication date cannot be null", e.getMessage(), "Expected exception for null web publication date");
        }
        try {
            article.setWebTitle(null);
        }
        catch (IllegalArgumentException e) {
            assertEquals("Web title cannot be null", e.getMessage(), "Expected exception for null web title");
        }
        try {
            article.setWebUrl(null);
        }
        catch (IllegalArgumentException e) {
            assertEquals("Web URL cannot be null", e.getMessage(), "Expected exception for null web URL");
        }
        try {
            article.setBodyText(null);
        }
        catch (IllegalArgumentException e) {
            assertEquals("Body text cannot be null", e.getMessage(), "Expected exception for null body text");
        }        
    }

    /**
     * This test method is intended to test the validation of the Article object for empty values.
     * It checks if the id, issue query, label, type, section id, section name, web publication date,
     * web title, web url, and body text are not empty strings.
     * If any of these values are empty, it should throw an IllegalArgumentException.
     */
    @Test
    public void testArticleEmptyValues() {
        Article article = new Article();
        
        // Assert that the empty strings are not accepted
        try {
            article.setId("");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Id cannot be empty", e.getMessage(), "Expected exception for empty ID");
        }
        try {
            article.setissueString("");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Issue query cannot be empty", e.getMessage(), "Expected exception for empty issue query");
        }
        try {
            article.setLabel("");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Label cannot be empty", e.getMessage(), "Expected exception for empty label");
        }
        try {
            article.setType("");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Type cannot be empty", e.getMessage(), "Expected exception for empty type");
        }
        try {
            article.setSectionId("");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Section ID cannot be empty", e.getMessage(), "Expected exception for empty section ID");
        }
        try {
            article.setSectionName("");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Section name cannot be empty", e.getMessage(), "Expected exception for empty section name");
        }
        try {
            article.setWebTitle("");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Web title cannot be empty", e.getMessage(), "Expected exception for empty web title");
        }
        try {
            article.setWebUrl("");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Web URL cannot be empty", e.getMessage(), "Expected exception for empty web URL");
        }
        try {
            article.setBodyText("");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Body text cannot be empty", e.getMessage(), "Expected exception for empty body text");
        }
    }   
}   
