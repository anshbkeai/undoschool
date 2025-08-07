package com.undoschool.undoschool.repo;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.undoschool.undoschool.Pojos.CourseDocument;

@Repository
public interface CourseRepo extends ElasticsearchRepository<CourseDocument,String> {

}
