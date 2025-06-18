/**
 * ElasticsearchTest.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.elasticsearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
// import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import it.unipd.dei.softplat.elasticsearch.controller.ElasticsearchController;
import it.unipd.dei.softplat.elasticsearch.dto.IndexArticleDTO;
import it.unipd.dei.softplat.elasticsearch.dto.SearchArticleDTO;
import it.unipd.dei.softplat.elasticsearch.model.ElasticArticle;
import it.unipd.dei.softplat.http.service.HttpClientService;

@Testcontainers
@SpringBootTest
public class ElasticsearchTest {

    /**
     * This container is used to run an instance of Elasticsearch for testing purposes.
     */
    @SuppressWarnings("resource")
    @Container
    private static final ElasticsearchContainer elasticsearchContainer =
        new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:8.7.1")
            .withEnv("xpack.security.enabled", "false")
            .withEnv("discovery.type", "single-node")
            .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m");


    @MockBean
    private HttpClientService httpClientService;

    @Autowired @InjectMocks
    private ElasticsearchController elasticsearchController;

    @DynamicPropertySource
    static void setElasticsearchProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", elasticsearchContainer::getHttpHostAddress);
    }

    /**
     * This test verifies that the ElasticsearchController's indexArticles method
     * correctly indexes articles in Elasticsearch.
     */
    @Test
    public void testElasticsearchIndexing() {
        // Create articles to be indexed
        ElasticArticle article1 = new ElasticArticle();
        article1.setId("1");
        article1.setissueString("issue_query_1");
        article1.setLabel("label_1");
        article1.setType("type_1");
        Calendar cal = Calendar.getInstance();
        cal.set(2023, Calendar.OCTOBER, 1, 12, 0, 0);
        Date date = cal.getTime();
        article1.setWebPublicationDate(date);
        article1.setWebTitle("Wikipedia - Software Engineering");
        article1.setBodyText("Software engineering is a branch of both computer science and engineering focused on designing, developing, testing, and maintaining software applications. It involves applying engineering principles and computer programming expertise to develop software systems that meet user needs. The terms programmer and coder overlap software engineer, but they imply only the construction aspect of a typical software engineer workload. A software engineer applies a software development process, which involves defining, implementing, testing, managing, and maintaining software systems, as well as developing the software development process itself.");
        ElasticArticle article2 = new ElasticArticle();
        article2.setId("2");
        article2.setissueString("issue_query_2");
        article2.setLabel("label_2");
        article2.setType("type_2");
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2023, Calendar.OCTOBER, 2, 12, 0, 0);
        Date date2 = cal2.getTime();
        article2.setWebPublicationDate(date2);
        article2.setWebTitle("Wikipedia - Computing Platform");
        article2.setBodyText("A computing platform, digital platform, or software platform is the infrastructure on which software is executed. While the individual components of a computing platform may be obfuscated under layers of abstraction, the summation of the required components comprise the computing platform. Sometimes, the most relevant layer for a specific software is called a computing platform in itself to facilitate the communication, referring to the whole using only one of its attributes - i.e. using a metonymy.");
        String collectionName = "test_collection";
        // DTO for indexing articles
        IndexArticleDTO indexArticleDTO = new IndexArticleDTO();
        indexArticleDTO.setArticles(List.of(article1, article2));
        indexArticleDTO.setCollectionName(collectionName);

        // Call the method to index articles
        ResponseEntity<?> response = elasticsearchController.indexArticles(indexArticleDTO);

        // Assert that the response is not null and has a status code of 200 OK
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Response should have status code 200 OK");

        IndexArticleDTO indexArticleDTO2 = new IndexArticleDTO();
        indexArticleDTO2.setArticles(List.of());
        indexArticleDTO2.setCollectionName("");

        // Call the method with empty articles and collection name
        ResponseEntity<?> response2 = elasticsearchController.indexArticles(indexArticleDTO2);

        // Assert that the response is not null and has a status code of 400 Bad Request
        assertNotNull(response2, "Response should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, response2.getStatusCode(), "Response should have status code 400 Bad Request");

        IndexArticleDTO indexArticleDTO3 = new IndexArticleDTO();
        indexArticleDTO3.setArticles(null);
        indexArticleDTO3.setCollectionName(null);

        // Call the method with null articles and collection name
        ResponseEntity<?> response3 = elasticsearchController.indexArticles(indexArticleDTO3);
        
        // Assert that the response is not null and has a status code of 400 Bad Request
        assertNotNull(response3, "Response should not be null");
        assertEquals(HttpStatus.BAD_REQUEST, response3.getStatusCode(), "Response should have status code 400 Bad Request");
    }

    @Test
    public void testGetArticlesByQuery() {
        // Mock configuration for Elasticsearch
        when(httpClientService.postRequest(
            eq("http://mongodb-service:8085/mongodb/get-articles/"),
            anyString()
            )
        ).thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));

        // Fill Elasticsearch with some data
        ElasticArticle article1 = new ElasticArticle();
        article1.setId("1");
        article1.setissueString("test_issue");
        article1.setLabel("label_1");
        article1.setType("type_1");
        Calendar cal = Calendar.getInstance();
        cal.set(2023, Calendar.OCTOBER,1, 12, 0, 0);
        Date date = cal.getTime();
        article1.setWebPublicationDate(date);
        article1.setWebTitle("Wikipedia - Software Engineering");
        article1.setBodyText("Software engineering is a branch of both computer science and engineering focused on designing, developing, testing, and maintaining software applications. It involves applying engineering principles and computer programming expertise to develop software systems that meet user needs. The terms programmer and coder overlap software engineer, but they imply only the construction aspect of a typical software engineer workload. A software engineer applies a software development process, which involves defining, implementing, testing, managing, and maintaining software systems, as well as developing the software development process itself.");
        ElasticArticle article2 = new ElasticArticle();
        article2.setId("2");
        article2.setissueString("test_issue");
        article2.setLabel("label_2");
        article2.setType("type_2");
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2024, Calendar.OCTOBER, 2, 12, 0, 0);
        Date date2 = cal2.getTime();
        article2.setWebPublicationDate(date2);
        article2.setWebTitle("Wikipedia - Computing Platform");
        article2.setBodyText("A computing platform, digital platform, or software platform is the infrastructure on which software is executed. While the individual components of a computing platform may be obfuscated under layers of abstraction, the summation of the required components comprise the computing platform. Sometimes, the most relevant layer for a specific software is called a computing platform in itself to facilitate the communication, referring to the whole using only one of its attributes - i.e. using a metonymy.");
        String collectionName = "get_test_collection";
        // DTO for indexing articles
        IndexArticleDTO indexArticleDTO = new IndexArticleDTO();
        indexArticleDTO.setArticles(List.of(article1, article2));
        indexArticleDTO.setCollectionName(collectionName);

        // Call the method with empty articles and collection name
        ResponseEntity<?> response = elasticsearchController.indexArticles(indexArticleDTO);

        // Assert that the response is not null and has a status code of 200 OK
        assertNotNull(response, "Response should not be null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Response should have status code 200 OK");

        // Example query and corpus
        String query = "software";
        String corpus = "get_test_collection";

        // Create a SearchArticleDTO object
        SearchArticleDTO searchArticleDTO = new SearchArticleDTO();
        searchArticleDTO.setQuery(query);
        searchArticleDTO.setCorpus(corpus);
        searchArticleDTO.setStartDate(null);
        searchArticleDTO.setEndDate(null);

        // Call the method to get articles by query
        ResponseEntity<?> responseQuery = elasticsearchController.getArticlesByQuery(searchArticleDTO);

        // Assert that the response is not null and has a status code of 200 OK
        assertNotNull(responseQuery, "Response should not be null");
        assertEquals(HttpStatus.OK, responseQuery.getStatusCode(), "Response should have status code 200 OK");
        
        // Test with a date range
        System.out.println("Testing with date range...");
        Calendar cal3 = Calendar.getInstance();
        cal3.set(2023, Calendar.JANUARY, 1, 0, 0, 0);
        Date startDate = cal3.getTime();
        cal3.set(2023, Calendar.DECEMBER, 31, 23, 59, 59);
        Date endDate = cal3.getTime();
        SearchArticleDTO searchArticleDTO2 = new SearchArticleDTO();
        searchArticleDTO2.setQuery(query);
        searchArticleDTO2.setCorpus(corpus);
        searchArticleDTO2.setStartDate(startDate);
        searchArticleDTO2.setEndDate(endDate);

        // Call the method to get articles by query with date range
        ResponseEntity<?> responseQuery2 = elasticsearchController.getArticlesByQuery(searchArticleDTO2);

        // Assert that the response is not null and has a status code of 200 OK
        assertNotNull(responseQuery2, "Response should not be null");
        assertEquals(HttpStatus.OK, responseQuery2.getStatusCode(), "Response should have status code 200 OK");
        
        // Verify that the HTTP client service was called with the correct URL and parameters
        verify(httpClientService, times(2)).postRequest(eq("http://mongodb-service:8085/mongodb/get-articles/"), anyString());
    }

    @Test
    public void testElasticArticle(){
        // Create an instance of ElasticArticle
        ElasticArticle article = new ElasticArticle();
        // Set properties
        article.setId("test_id");
        article.setissueString("test_issue_query");
        article.setLabel("test_label");
        article.setType("test_type");
        Calendar cal = Calendar.getInstance();
        cal.set(2025, Calendar.JANUARY, 1, 0, 0, 0);
        Date date = cal.getTime();
        article.setWebPublicationDate(date);
        article.setWebTitle("Test Web Title");
        article.setBodyText("This is a test body text for the ElasticArticle class.");
        // Assert that the properties are set correctly
        assertEquals("test_id", article.getId(), "ID should match");
        assertEquals("test_issue_query", article.getissueString(), "Issue query should match");
        assertEquals("test_label", article.getLabel(), "Label should match");
        assertEquals("test_type", article.getType(), "Type should match");
        assertEquals(date, article.getWebPublicationDate(), "Web publication date should match");
        assertEquals("Test Web Title", article.getWebTitle(), "Web title should match");
        assertEquals("This is a test body text for the ElasticArticle class.", article.getBodyText(), "Body text should match");
    }

    @Test
    public void testElasticArticleWithNullValues() {
        // Create an instance of ElasticArticle with null values
        ElasticArticle article = new ElasticArticle();
        // Assert that the properties are null
        try {
            article.setId(null);
        }
        catch (IllegalArgumentException e) {
            assertEquals("ID cannot be null", e.getMessage(), "Exception message should match");
        }
        try {
            article.setissueString(null);
        }
        catch (IllegalArgumentException e) {
            assertEquals("Issue query cannot be null", e.getMessage(), "Exception message should match");
        }
        try {
            article.setLabel(null);
        }
        catch (IllegalArgumentException e) {
            assertEquals("Label cannot be null", e.getMessage(), "Exception message should match");
        }
        try {
            article.setType(null);
        }
        catch (IllegalArgumentException e) {
            assertEquals("Type cannot be null", e.getMessage(), "Exception message should match");
        }
        try {
            article.setWebPublicationDate(null);
        }
        catch (IllegalArgumentException e) {
            assertEquals("Web publication date cannot be null", e.getMessage(), "Exception message should match");
        }
        try {
            article.setWebTitle(null);
        }
        catch (IllegalArgumentException e) {
            assertEquals("Web title cannot be null", e.getMessage(), "Exception message should match");
        }
        try {
            article.setBodyText(null);
        }
        catch (IllegalArgumentException e) {
            assertEquals("Body text cannot be null", e.getMessage(), "Exception message should match");
        }
    }

    @Test
    public void testElasticArticleWithEmptyValues() {
        // Create an instance of ElasticArticle with empty values
        ElasticArticle article = new ElasticArticle();
        // Assert that the properties are empty
        try {
            article.setId("");
        }
        catch (IllegalArgumentException e) {
            assertEquals("ID cannot be empty", e.getMessage(), "Exception message should match");
        }
        try {
            article.setissueString("");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Issue query cannot be empty", e.getMessage(), "Exception message should match");
        }
        try {
            article.setLabel("");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Label cannot be empty", e.getMessage(), "Exception message should match");
        }
        try {
            article.setType("");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Type cannot be empty", e.getMessage(), "Exception message should match");
        }
        try {
            article.setWebTitle("");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Web title cannot be empty", e.getMessage(), "Exception message should match");
        }
        try {
            article.setBodyText("");
        }
        catch (IllegalArgumentException e) {
            assertEquals("Body text cannot be empty", e.getMessage(), "Exception message should match");
        }
    }

}
