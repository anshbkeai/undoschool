package com.undoschool.undoschool.repo;

import java.util.List;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.undoschool.undoschool.Pojos.CourseDocument;

@Repository
public interface CourseRepo extends ElasticsearchRepository<CourseDocument,String> {


    @Query("""
        {
        "multi_match": {
            "query": "?0",
            "fields": ["title", "description"]
        }
        }
    """)
    List<CourseDocument> FullMatchTitleAndDescription(String title, String category);

}
