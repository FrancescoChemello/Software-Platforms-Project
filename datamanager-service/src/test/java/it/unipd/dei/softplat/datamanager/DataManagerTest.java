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
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import it.unipd.dei.softplat.datamanager.controller.DataManagerController;
import it.unipd.dei.softplat.datamanager.model.Article;
import it.unipd.dei.softplat.http.service.HttpClientService;
import it.unipd.dei.softplat.testutil.TestAsyncConfig;

/**
 * This class contains unit tests for the DataManagerController.
 * It tests the getArticles method and the validation of the Article object.
 */
@SpringBootTest
@Import(TestAsyncConfig.class)
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
        List<Article> emptyArticleList = new ArrayList<>();

        // Call saveArticles method with an empty list of articles
        ResponseEntity <?> invalid_response = controller_test.saveArticles(emptyArticleList);
        // Assert that the response is not null and has a status code of 400 Bad Request
        assertNotNull(invalid_response, "Response should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, invalid_response.getStatusCode(), "Response should have status code 400 Bad Request");

        List<Article> nullArticleList = null;

        // Call saveArticles with null
        ResponseEntity <?> null_response = controller_test.saveArticles(nullArticleList);
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
}   
