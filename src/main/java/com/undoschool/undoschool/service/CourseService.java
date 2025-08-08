package com.undoschool.undoschool.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.undoschool.undoschool.Pojos.CourseDocument;
import com.undoschool.undoschool.Pojos.CourseResponsedto;
import com.undoschool.undoschool.repo.CourseRepo;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.Suggestion;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CourseService {

    private final ElasticsearchClient esClient;
    private final CourseRepo courseRepo;

    @Value("${courses-v2}")
    private  String INDEX_NAME_v2 ;

    @Value("${app.elasticsearch.index-name}")
    private  String INDEX_NAME ;

    public CourseService(
        ElasticsearchClient esClient,
        CourseRepo courseRepo
    ) 
    {
        this.esClient = esClient;
        this.courseRepo = courseRepo;
    }

    public CourseResponsedto searchResponse(String q,
                                    Integer minAge,
                                    Integer maxAge,
                                    String category,
                                    String type,
                                    Double minPrice,
                                    Double maxPrice,
                                     Date startDate,
                                     String sort,
                                     int page,
                                     int size
                                    

    ) throws ElasticsearchException, IOException {
        // creoetr 
       SearchResponse<CourseDocument> searchResponse = esClient.search(s -> s
                                .index(INDEX_NAME)
                                
                                .query(qu -> qu
                                    .bool(b -> {
                                        log.info("Query: {}", q);
                                        // noraml Seach Logic 
                                        //  if(q!=null && !q.isEmpty()) {
                                        //       b.must(m -> m
                                        //         .multiMatch(mm -> mm
                                        //             .query(q) 
                                        //             .fields("title", "description") // now 
                                        //         )
                                        //     );
                                        //     log.info(" {} " , b.toString());
                                        //  }


                                        // Search by title with Fuzzines and description
                                        if(q!=null && !q.isEmpty()) {
                                            b.must(m -> 
                                            m.bool(b1 -> b1
                                                .should(sh -> sh
                                                    .match(m1 -> m1
                                                        .field("title")
                                                        .query(q)
                                                        .fuzziness("AUTO")
                                                        .prefixLength(0)
                                                        
                                                    )
                                                )
                                                .should(sh -> sh
                                                    .match(m2 -> m2
                                                        .field("description")
                                                        .query(q)
                                                    )
                                                )
                                            ));
                                            log.info("Query with fuzziness: {}", b.toString());

                                            
                                        }
                                            
                                        

                                         if (minAge != null) {
                                           b.filter(f -> f.range(r -> r.number(n -> n.field("minAge").gte((double)(minAge)))));
                                         }

                                         if (maxAge != null) {
                                           b.filter(f -> f.range(r -> r.number(n -> n.field("maxAge").lte((double)(maxAge)))));
                                         }
                                         if (minPrice != null) {
                                           b.filter(f -> f.range(r -> r.number(n -> n.field("price").gte(minPrice))));
                                         }
                                         if (maxPrice != null) {
                                           b.filter(f -> f.range(r -> r.number(n -> n.field("price").lte(maxPrice))));
                                         }

                                        if (category != null && !category.isEmpty()) {
                                            b.filter(f -> f.term(t -> t.field("category").value(category)));
                                        }
                                        if (type != null && !type.isEmpty()) {
                                            b.filter(f -> f.term(t -> t.field("type").value(type)));
                                        }
                                        
                                        if(sort.equals("upcoming")) {
                                            s.sort(so -> so
                                                        .field(f -> f.field("nextSessionDate").order(SortOrder.Asc))
                                            
                                            );
                                        }
                                        else {
                                            if(sort.equals("priceAsc")) {
                                                s.sort(so -> so
                                                        .field(f -> f.field("price").order(SortOrder.Asc))
                                            
                                                );
                                            }
                                            else if(sort.equals("priceDesc")) {
                                                s.sort(so -> so
                                                        .field(f -> f.field("price").order(SortOrder.Desc))
                                            
                                                );
                                            }
                                        }

                                      
                                        
                                      return b;
                                    }
                                        
                                    )

                                ).size(size).from(page*size),
    CourseDocument.class
                );
                                    log.info("Search Response: {}", searchResponse.toString());


        long total_hits = searchResponse.hits().total().value();

        List<CourseDocument> courseDocuments = searchResponse.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());

        CourseResponsedto courseResponsedto = new CourseResponsedto();
        courseResponsedto.setCourses(courseDocuments);
        courseResponsedto.setTotal(total_hits);
        courseResponsedto.setPage(page);
        courseResponsedto.setSize(size);
        // Extract the documents from the response hits
        return  courseResponsedto;
    
    }

    public List<CourseDocument> test(String q) {
        try {
            return courseRepo.FullMatchTitleAndDescription(q, q);
        } catch (Exception e) {
            // TODO: handle exception
            return null;
        }
    }

    public List<String> suggestCourses(String prefix) throws IOException {
        SearchResponse<CourseDocument> searchResponse = esClient.search(s -> s
                .index(INDEX_NAME_v2)
                .suggest(su -> su
                        .suggesters("course_suggestion", sug -> sug
                                .prefix(prefix)
                                .completion(c -> c.field("suggest"))
                        )
                ).from(0).size(10),
                CourseDocument.class
        );

        Suggestion<CourseDocument> suggestion = searchResponse.suggest().get("course_suggestion").stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No suggestions found"));

        return suggestion.completion().options().stream()
                .map(option -> option.text().toString())
                .collect(Collectors.toList());
    }
}
