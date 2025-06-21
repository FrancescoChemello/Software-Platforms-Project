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
import java.util.Date;
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
    }

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
        article1.setissueString("issue_query_1");
        article1.setLabel("label_1");
        article1.setType("type_1");
        article1.setTopics(List.of("topic1", "topic2"));
        article1.setSectionId("section_id_1");
        article1.setSectionName("section_name_1");
        article1.setWebPublicationDate("2023-10-01T12:00:00Z");
        article1.setWebTitle("Wikipedia - Software Engineering");
        article1.setWebUrl("https://en.wikipedia.org/wiki/Software_engineering");
        article1.setBodyText("Software engineering is a branch of both computer science and engineering focused on designing, developing, testing, and maintaining software applications. It involves applying engineering principles and computer programming expertise to develop software systems that meet user needs. The terms programmer and coder overlap software engineer, but they imply only the construction aspect of a typical software engineer workload. A software engineer applies a software development process, which involves defining, implementing, testing, managing, and maintaining software systems, as well as developing the software development process itself.");
        MalletArticle article2 = new MalletArticle();
        article2.setId("2");
        article2.setissueString("issue_query_2");
        article2.setLabel("label_2");
        article2.setType("type_2");
        article2.setTopics(List.of("topic3", "topic4"));
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
    }

    /**
     * Test the MalletSearch model.
     * This test verifies that the MalletSearch class can be instantiated
     * and that its properties can be set and retrieved correctly.
     */
    @Test
    public void testMalletSearch() {
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
     * Test the MalletSearch model with empty values.
     * This test verifies that the MalletSearch class throws exceptions
     * when trying to set empty or null values for its properties.
     */
    @Test
    public void testMalletSearchWithEmptyValues() {
        MalletSearch malletSearch = new MalletSearch();

        try {
            malletSearch.setQuery("");
        } 
        catch (IllegalArgumentException e) {
            assertEquals("Query cannot be empty", e.getMessage(), "Expected exception for empty query");
        }
        try {
            malletSearch.setCorpus("");
        } 
        catch (IllegalArgumentException e) {
            assertEquals("Corpus cannot be empty", e.getMessage(), "Expected exception for empty corpus");
        }
        try {
            malletSearch.setStartDate(new Date());
        }
        catch (IllegalArgumentException e) {
            assertEquals("Start date cannot be null", e.getMessage(), "Expected exception for null start date");
        }
        try {
            malletSearch.setEndDate(new Date());
        }
        catch (IllegalArgumentException e) {
            assertEquals("End date cannot be null", e.getMessage(), "Expected exception for null end date");
        }
    }

    /**
     * Test the MalletSearch model with null values.
     * This test verifies that the MalletSearch class throws exceptions
     * when trying to set null values for its properties.
     */
    @Test
    public void testMalletSearchWithNullValues() {
        MalletSearch malletSearch = new MalletSearch();

        try {
            malletSearch.setQuery(null);
        } catch (IllegalArgumentException e) {
            assertEquals("Query cannot be null", e.getMessage(), "Expected exception for null query");
        }
        try {
            malletSearch.setCorpus(null);
        } catch (IllegalArgumentException e) {
            assertEquals("Corpus cannot be null", e.getMessage(), "Expected exception for null corpus");
        }
        try {
            malletSearch.setNumTopics(null);
        } catch (IllegalArgumentException e) {
            assertEquals("Number of topics cannot be null", e.getMessage(), "Expected exception for null number of topics");
        }
        try {
            malletSearch.setNumTopWordsPerTopic(null);
        } catch (IllegalArgumentException e) {
            assertEquals("Number of top words per topic cannot be null", e.getMessage(), "Expected exception for null number of top words per topic");
        }
        try {
            malletSearch.setStartDate(null);
        } catch (IllegalArgumentException e) {
            assertEquals("Start date cannot be null", e.getMessage(), "Expected exception for null start date");
        }
        try {
            malletSearch.setEndDate(null);
        } catch (IllegalArgumentException e) {
            assertEquals("End date cannot be null", e.getMessage(), "Expected exception for null end date");
        }
    }

    /**
     * Test the MalletArticle model.
     * This test verifies that the MalletArticle class can be instantiated
     * and that its properties can be set and retrieved correctly.
     */
    @Test
    public void testMalletArticle() {
        MalletArticle article = new MalletArticle();

        // Set the properties of the article object
        article.setId("test_id");
        article.setissueString("issue_query_test");
        article.setLabel("test_label");
        article.setType("test_type");
        article.setTopics(List.of("topic1", "topic2", "topic3"));
        article.setSectionId("section_id_test");
        article.setSectionName("section_name_test");
        article.setWebPublicationDate("2023-10-01T12:00:00Z");
        article.setWebTitle("Test Web Title");
        article.setWebUrl("https://example.com/test-web-url");
        article.setBodyText("This is a test body text for the MongoDB article.");

        // Assertions to verify the properties
        assertEquals("test_id", article.getId());
        assertEquals("issue_query_test", article.getissueString());
        assertEquals("test_label", article.getLabel());
        assertEquals("test_type", article.getType());
        assertEquals(List.of("topic1", "topic2", "topic3"), article.getTopics());
        assertEquals("section_id_test", article.getSectionId());
        assertEquals("section_name_test", article.getSectionName());
        assertEquals("2023-10-01T12:00:00Z", article.getWebPublicationDate());
        assertEquals("Test Web Title", article.getWebTitle());
        assertEquals("https://example.com/test-web-url", article.getWebUrl());
        assertEquals("This is a test body text for the MongoDB article.", article.getBodyText());
    }

    /**
     * Test the MalletArticle model with empty values.
     * This test verifies that the MalletArticle class throws exceptions
     * when trying to set empty or null values for its properties.
     */
    @Test
    public void testMalletArticleWithEmptyValues() {
       MalletArticle article = new MalletArticle();
    
       try {
        article.setId("");
        } 
        catch (IllegalArgumentException e) {
            assertEquals("ID cannot be empty", e.getMessage(), "Expected exception for empty ID");
        }
        try {
            article.setissueString("");
        } catch (IllegalArgumentException e) {
            assertEquals("Issue query cannot be empty", e.getMessage(), "Expected exception for empty issue query");
        }
        try {
            article.setLabel("");
        } catch (IllegalArgumentException e) {
            assertEquals("Label cannot be empty", e.getMessage(), "Expected exception for empty label");
        }
        try {
            article.setType("");
        } catch (IllegalArgumentException e) {
            assertEquals("Type cannot be empty", e.getMessage(), "Expected exception for empty type");
        }
        try {
            article.setSectionId("");
        } catch (IllegalArgumentException e) {
            assertEquals("Section ID cannot be empty", e.getMessage(), "Expected exception for empty section ID");
        }
        try {
            article.setSectionName("");
        } catch (IllegalArgumentException e) {
            assertEquals("Section name cannot be empty", e.getMessage(), "Expected exception for empty section name");
        }
        try {
            article.setWebPublicationDate("");
        } catch (IllegalArgumentException e) {
            assertEquals("Web publication date cannot be empty", e.getMessage(), "Expected exception for empty web publication date");
        }
        try {
            article.setWebTitle("");
        } catch (IllegalArgumentException e) {
            assertEquals("Web title cannot be empty", e.getMessage(), "Expected exception for empty web title");
        }
        try {
            article.setWebUrl("");
        } catch (IllegalArgumentException e) {
            assertEquals("Web URL cannot be empty", e.getMessage(), "Expected exception for empty web URL");
        }
        try {
            article.setBodyText("");
        } catch (IllegalArgumentException e) {
            assertEquals("Body text cannot be empty", e.getMessage(), "Expected exception for empty body text");
        }
    }

    /**
     * Test the MalletArticle model with null values.
     * This test verifies that the MalletArticle class throws exceptions
     * when trying to set null values for its properties.
     */
    @Test
    public void testMalletArticleWithNullValues() {
        MalletArticle article = new MalletArticle();

        try {
            article.setId(null);
        } catch (IllegalArgumentException e) {
            assertEquals("ID cannot be null", e.getMessage(), "Expected exception for null ID");
        }
        try {
            article.setissueString(null);
        } catch (IllegalArgumentException e) {
            assertEquals("Issue query cannot be null", e.getMessage(), "Expected exception for null issue query");
        }
        try {
            article.setLabel(null);
        } catch (IllegalArgumentException e) {
            assertEquals("Label cannot be null", e.getMessage(), "Expected exception for null label");
        }
        try {
            article.setType(null);
        } catch (IllegalArgumentException e) {
            assertEquals("Type cannot be null", e.getMessage(), "Expected exception for null type");
        }
        try {
            article.setTopics(null);
        } catch (IllegalArgumentException e) {
            assertEquals("Topics cannot be null", e.getMessage(), "Expected exception for null topics");
        }
        try {
            article.setSectionId(null);
        } catch (IllegalArgumentException e) {
            assertEquals("Section ID cannot be null", e.getMessage(), "Expected exception for null section ID");
        }
        try {
            article.setSectionName(null);
        } catch (IllegalArgumentException e) {
            assertEquals("Section name cannot be null", e.getMessage(), "Expected exception for null section name");
        }
        try {
            article.setWebPublicationDate(null);
        } catch (IllegalArgumentException e) {
            assertEquals("Web publication date cannot be null", e.getMessage(), "Expected exception for null web publication date");
        }
        try {
            article.setWebTitle(null);
        } catch (IllegalArgumentException e) {
            assertEquals("Web title cannot be null", e.getMessage(), "Expected exception for null web title");
        }
        try {
            article.setWebUrl(null);
        } catch (IllegalArgumentException e) {
            assertEquals("Web URL cannot be null", e.getMessage(), "Expected exception for null web URL");
        }
        try {
            article.setBodyText(null);
        } catch (IllegalArgumentException e) {
            assertEquals("Body text cannot be null", e.getMessage(), "Expected exception for null body text");
        }
    }
}
