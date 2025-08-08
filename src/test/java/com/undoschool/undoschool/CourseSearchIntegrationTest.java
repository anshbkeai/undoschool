package com.undoschool.undoschool;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;

import com.undoschool.undoschool.Pojos.CourseDocument;
import com.undoschool.undoschool.Pojos.CourseResponsedto;
import com.undoschool.undoschool.repo.CourseRepo;
import com.undoschool.undoschool.service.CourseService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CourseSearchIntegrationTest {

    @Container
    private static final ElasticsearchContainer elasticsearch =
            new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:8.15.0")
                    .withEnv("discovery.type", "single-node")
                    .withEnv("xpack.security.enabled", "false");

    @Autowired
    private ElasticsearchClient esClient;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseRepo courseRepo;

    @BeforeAll
    void setUp() throws IOException {
        elasticsearch.start();

        List<CourseDocument> courseDocuments = new ArrayList<>();
        CourseDocument course1 = CourseDocument.builder()
                .id("1")
                .title("Algebra Basics")
                .description("Introduction to Algebra")
                .category("Math")
                .type("COURSE")
                .minAge(12)
                .maxAge(15)
                .price(100.0)
                .nextSessionDate(Instant.now().toString())
                .build();
        CourseDocument course2 = CourseDocument.builder()
                .id("2")
                .title("Painting for Kids")
                .description("Fun painting classes")
                .category("Art")
                .type("ONE_TIME")
                .minAge(9)
                .maxAge(12)
                .price(50.0)
                .nextSessionDate(new Date().toInstant().toString())
                .build();       
                
        CourseDocument course3 = CourseDocument.builder()
                .id("3")
                .title("Robotics Club")
                .description("Join our robotics club")
                .category("Technology")
                .type("CLUB")
                .minAge(10)
                .maxAge(16)
                .price(200.0)
                .nextSessionDate(new Date().toInstant().toString())
                .build();
        courseDocuments.add(course1);
        courseDocuments.add(course2);
        courseDocuments.add(course3);
        esClient.index(i -> i
                .index("courses")
                .id(course1.getId())
                .document(course1));
        esClient.index(i -> i
                .index("courses")
                .id(course2.getId())
                .document(course2));
        esClient.index(i -> i 
                .index("courses")
                .id(course3.getId())
                .document(course3));                     
        esClient.indices().refresh(r -> r.index("courses"));

        courseRepo.saveAll(courseDocuments);
        System.out.println("Elasticsearch version: " + org.springframework.data.elasticsearch.core.ElasticsearchOperations.class.getPackage().getImplementationVersion());
        System.out.println("Courses loaded into Elasticsearch: " + courseDocuments.size());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/test.csv", numLinesToSkip = 1)
    void testCourseSearch(String q,
                          Integer minAge,
                          Integer maxAge,
                          String category,
                          String type,
                          Double minPrice,
                          Double maxPrice,
                          String sort,
                          int page,
                          int size,
                          int expectedCount) throws Exception {

      //  Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(startDateStr);

       CourseResponsedto result = courseService.searchResponse(
                q, minAge, maxAge, category, type, minPrice, maxPrice, null, sort, page, size
        );

        assertEquals(expectedCount, result.getCourses().size(), "Unexpected number of courses returned");
    }
}
