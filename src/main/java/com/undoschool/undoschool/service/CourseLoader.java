package com.undoschool.undoschool.service;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;


import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.undoschool.undoschool.Pojos.CourseDocument;
import com.undoschool.undoschool.repo.CourseRepo;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CourseLoader {
    private final CourseRepo courseRepo;
    private final ObjectMapper objectMapper;

   

    public CourseLoader(
            CourseRepo courseRepo,
            ObjectMapper objectMapper
    )
     {
            this.courseRepo = courseRepo;
            this.objectMapper = objectMapper;
     }

     @PostConstruct
     public void load_data_json() {
            try (
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("sample-course.json")
            ){
                    if(inputStream == null) {
                        log.error("sample-courses.json not found");
                        return;
                    }
                    List<CourseDocument> courseDocuments = Arrays.asList(objectMapper.readValue(inputStream,CourseDocument[].class));
                    courseRepo.saveAll(courseDocuments);
                    log.info("Courses loaded into Elasticsearch: {} " ,  courseDocuments.size());
            } catch (Exception e) {
                // TODO: handle exception
            }
     }
}
