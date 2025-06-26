/**
 * MalletTest.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.mallet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import it.unipd.dei.softplat.http.service.HttpClientService;
import it.unipd.dei.softplat.mallet.controller.MalletController;
import it.unipd.dei.softplat.mallet.dto.AccumulateMalletArticlesDTO;
import it.unipd.dei.softplat.mallet.model.MalletArticle;
import it.unipd.dei.softplat.mallet.model.MalletSearch;
import it.unipd.dei.softplat.testutil.TestAsyncConfig;

@SpringBootTest
@Import(TestAsyncConfig.class)
public class MalletTest {

    @MockBean
    private HttpClientService httpClientService;
    
    @Autowired @InjectMocks
    private MalletController malletController;

    /**
     * Test the search functionality of MalletController.
     * This test verifies that the search method of MalletController
     * correctly handles valid and invalid search queries.
     */
    @Test
    public void testSearchArticles() {
        // Mock configuration
        when(httpClientService.postRequest(
            eq("http://elasticsearch-service:8083/elastic/search/"),
            anyString()
            )
        ).thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));

        MalletSearch searchQuery = new MalletSearch();
        searchQuery.setQuery("software application development");
        searchQuery.setCorpus("test_corpus");
        searchQuery.setNumTopics(5);
        searchQuery.setNumTopWordsPerTopic(10);
        Calendar cal = Calendar.getInstance();
        cal.set(2023, Calendar.OCTOBER, 1, 0, 0, 0);
        searchQuery.setStartDate(cal.getTime());
        cal.set(2023, Calendar.OCTOBER, 31, 23, 59, 59);
        searchQuery.setEndDate(cal.getTime());

        ResponseEntity<?> response = malletController.search(searchQuery);

        // Check if the response is not null
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Response should have status code 200 OK");

        // Verify that the postRequest method of MalletService was called with the correct URL and parameters
        verify(httpClientService).postRequest(eq("http://elasticsearch-service:8083/elastic/search/"), anyString());

        // Example of invalid request (numTopics and numTopWordsPerTopic are null)
        MalletSearch invalidSearchQuery = new MalletSearch("software application development", "test_corpus", null, null, null, null);
    
        // Call the search method of MalletController with invalid parameters
        ResponseEntity<?> invalidResponse = malletController.search(invalidSearchQuery);

        // Check if the response is not null and has status code 400 (Bad Request)
        assertNotNull(invalidResponse, "Response should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, invalidResponse.getStatusCode(), "Response should have status code 400 Bad Request");
    
        // Example of invalid request (query and corpus are null)
        MalletSearch anotherInvalidSearchQuery = new MalletSearch(null, null, 5, 10, null, null);

        // Call the search method of MalletController with another set of invalid parameters
        ResponseEntity<?> anotherInvalidResponse = malletController.search(anotherInvalidSearchQuery);

        // Check if the response is not null and has status code 400 (Bad Request)
        assertNotNull(anotherInvalidResponse, "Response should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, anotherInvalidResponse.getStatusCode(), "Response should have status code 400 Bad Request");

        // Example of invalid request (query and coprus are empty)
        MalletSearch emptySearchQuery = new MalletSearch("", "", 5, 10, null, null);

        // Call the search method of MalletController with empty parameters
        ResponseEntity<?> emptyResponse = malletController.search(emptySearchQuery);

        // Check if the response is not null and has status code 400 (Bad Request)
        assertNotNull(emptyResponse, "Response should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, emptyResponse.getStatusCode(), "Response should have status code 400 Bad Request");
    }

    /**
     * Test the accumulation and processing of Mallet articles.
     * This test verifies that the accumulate method of MalletController
     * correctly accumulates articles and processes the result.
     */
    @Test
    public void testAccumulationAndProcessResult() {
        // Mock configuration
        when(httpClientService.postRequest(
                eq("http://client-service:8080/client/query-result/"),
                anyString()
            )
        ).thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));

        MalletArticle article1 = new MalletArticle();
        article1.setId("1");
        article1.setType("type_1");
        article1.setSectionId("section_id_1");
        article1.setSectionName("section_name_1");
        article1.setWebPublicationDate("2023-10-01T12:00:00Z");
        article1.setWebTitle("Wikipedia - Software Engineering");
        article1.setWebUrl("https://en.wikipedia.org/wiki/Software_engineering");
        article1.setBodyText("Software engineering is a branch of both computer science and engineering focused on designing, developing, testing, and maintaining software applications. It involves applying engineering principles and computer programming expertise to develop software systems that meet user needs. The terms programmer and coder overlap software engineer, but they imply only the construction aspect of a typical software engineer workload. A software engineer applies a software development process, which involves defining, implementing, testing, managing, and maintaining software systems, as well as developing the software development process itself.");
        MalletArticle article2 = new MalletArticle();
        article2.setId("2");
        article2.setType("type_2");
        article2.setSectionId("section_id_2");
        article2.setSectionName("section_name_2");
        article2.setWebPublicationDate("2023-10-02T12:00:00Z");
        article2.setWebTitle("Wikipedia - Computing Platform");
        article2.setWebUrl("https://en.wikipedia.org/wiki/Computing_platform");
        article2.setBodyText("A computing platform, digital platform, or software platform is the infrastructure on which software is executed. While the individual components of a computing platform may be obfuscated under layers of abstraction, the summation of the required components comprise the computing platform. Sometimes, the most relevant layer for a specific software is called a computing platform in itself to facilitate the communication, referring to the whole using only one of its attributes - i.e. using a metonymy.");

        AccumulateMalletArticlesDTO accumulateMalletArticlesDTO = new AccumulateMalletArticlesDTO();
        accumulateMalletArticlesDTO.setArticles(List.of(article1, article2));
        accumulateMalletArticlesDTO.setCollectionName("test_collection");
        accumulateMalletArticlesDTO.setQuery("software application development");
        accumulateMalletArticlesDTO.setEndOfStream(true);

        ResponseEntity<?> response = malletController.accumulate(accumulateMalletArticlesDTO);

        // Check if the response is not null
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Response should have status code 200 OK");

        // Verify that the postRequest method of MalletService was called with the correct URL and parameters
        verify(httpClientService).postRequest(eq("http://client-service:8080/client/query-result/"), anyString());

        // Example of invalid request (articles list is null)
        AccumulateMalletArticlesDTO invalidAccumulateMalletArticlesDTO = new AccumulateMalletArticlesDTO(null, "test_collection", "software application development", true);

        // Call the accumulate method of MalletController with invalid parameters
        ResponseEntity<?> invalidResponse = malletController.accumulate(invalidAccumulateMalletArticlesDTO);

        // Check if the response is not null and has status code 400 (Bad Request)
        assertNotNull(invalidResponse, "Response should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, invalidResponse.getStatusCode(), "Response should have status code 400 Bad Request");

        // Example of invalid request (collection name and query are null)
        AccumulateMalletArticlesDTO anotherInvalidAccumulateMalletArticlesDTO = new AccumulateMalletArticlesDTO(List.of(article1, article2), null, null, true);

        // Call the accumulate method of MalletController with another set of invalid parameters
        ResponseEntity<?> anotherInvalidResponse = malletController.accumulate(anotherInvalidAccumulateMalletArticlesDTO);

        // Check if the response is not null and has status code 400 (Bad Request)
        assertNotNull(anotherInvalidResponse, "Response should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, anotherInvalidResponse.getStatusCode(), "Response should have status code 400 Bad Request");

        // Example of invalid request (collection name and query are empty)
        AccumulateMalletArticlesDTO emptyAccumulateMalletArticlesDTO = new AccumulateMalletArticlesDTO(List.of(article1, article2), "", "", true);

        // Call the accumulate method of MalletController with empty parameters
        ResponseEntity<?> emptyResponse = malletController.accumulate(emptyAccumulateMalletArticlesDTO);
        
        // Check if the response is not null and has status code 400 (Bad Request)
        assertNotNull(emptyResponse, "Response should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, emptyResponse.getStatusCode(), "Response should have status code 400 Bad Request");
    }

    /**
     * Test the MalletSearch model.
     * This test verifies that the MalletSearch class can be instantiated
     * and that its properties can be set and retrieved correctly.
     */
    @Test
    public void testMalletSearchGetterSetter() {
        MalletSearch malletSearch = new MalletSearch();

        malletSearch.setQuery("test query");
        malletSearch.setCorpus("test_corpus");
        malletSearch.setNumTopics(5);
        malletSearch.setNumTopWordsPerTopic(10);
        Calendar cal_01_10_2023 = Calendar.getInstance();
        cal_01_10_2023.set(2023, Calendar.OCTOBER, 1, 0, 0, 0);
        malletSearch.setStartDate(cal_01_10_2023.getTime());
        Calendar cal_31_10_2023 = Calendar.getInstance();
        cal_31_10_2023.set(2023, Calendar.OCTOBER, 31, 23, 59, 59);
        malletSearch.setEndDate(cal_31_10_2023.getTime());
        
        // Assertions to verify the properties
        assertEquals("test query", malletSearch.getQuery());
        assertEquals("test_corpus", malletSearch.getCorpus());
        assertEquals(5, malletSearch.getNumTopics());
        assertEquals(10, malletSearch.getNumTopWordsPerTopic());
        assertEquals(cal_01_10_2023.getTime(), malletSearch.getStartDate());
        assertEquals(cal_31_10_2023.getTime(), malletSearch.getEndDate());
    }

    /**
     * Test the MalletArticle model.
     * This test verifies that the MalletArticle class can be instantiated
     * and that its properties can be set and retrieved correctly.
     */
    @Test
    public void testMalletArticleGetterSetter() {
        MalletArticle article = new MalletArticle();

        // Set the properties of the article object
        article.setId("test_id");
        article.setType("test_type");
        article.setSectionId("section_id_test");
        article.setSectionName("section_name_test");
        article.setWebPublicationDate("2023-10-01T12:00:00Z");
        article.setWebTitle("Test Web Title");
        article.setWebUrl("https://example.com/test-web-url");
        article.setBodyText("This is a test body text for the MongoDB article.");

        // Assertions to verify the properties
        assertEquals("test_id", article.getId());
        assertEquals("test_type", article.getType());
        assertEquals("section_id_test", article.getSectionId());
        assertEquals("section_name_test", article.getSectionName());
        assertEquals("2023-10-01T12:00:00Z", article.getWebPublicationDate());
        assertEquals("Test Web Title", article.getWebTitle());
        assertEquals("https://example.com/test-web-url", article.getWebUrl());
        assertEquals("This is a test body text for the MongoDB article.", article.getBodyText());
    }

    /**
     * Test the AccumulateMalletArticlesDTO model.
     * This test verifies that the AccumulateMalletArticlesDTO class can be instantiated
     * and that its properties can be set and retrieved correctly.
     */
    @Test
    public void testAccumulateMalletArticlesDTOGetterSetter() {
        // Create an instance of AccumulateMalletArticlesDTO
        AccumulateMalletArticlesDTO malletArticleDTO = new AccumulateMalletArticlesDTO();

        MalletArticle article1 = new MalletArticle();
        article1.setId("test_id_1");
        article1.setType("test_type_1");
        article1.setSectionId("section_id_test_1");
        article1.setSectionName("section_name_test_1");
        article1.setWebPublicationDate("2023-10-01T12:00:00Z");
        article1.setWebTitle("Test Web Title");
        article1.setWebUrl("https://example.com/test-web-url");
        article1.setBodyText("This is a test body text for the MongoDB article.");

        MalletArticle article2 = new MalletArticle();
        article2.setId("test_id_2");
        article2.setType("test_type_2");
        article2.setSectionId("section_id_test_2");
        article2.setSectionName("section_name_test_2");
        article2.setWebPublicationDate("2023-10-02T12:00:00Z");
        article2.setWebTitle("Test Web Title 2");
        article2.setWebUrl("https://example.com/test-web-url-2");
        article2.setBodyText("This is another test body text for the MongoDB article.");

        // Set the articles, collection name, query, and end of stream flag
        List<MalletArticle> articles = List.of(article1, article2);
        malletArticleDTO.setArticles(articles);
        malletArticleDTO.setCollectionName("test_collection");
        malletArticleDTO.setQuery("test query");
        malletArticleDTO.setEndOfStream(true);

        // Assertions to verify the properties
        assertEquals(articles, malletArticleDTO.getArticles(), "Expected articles to match");
        assertEquals("test_collection", malletArticleDTO.getCollectionName(), "Expected collection name to match");
        assertEquals("test query", malletArticleDTO.getQuery(), "Expected query to match");
        assertEquals(true, malletArticleDTO.isEndOfStream(), "Expected end of stream flag to be true");
    }
}
