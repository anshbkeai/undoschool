package com.undoschool.undoschool.restcontroller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.undoschool.undoschool.Pojos.CourseResponsedto;
import com.undoschool.undoschool.service.CourseService;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/search")
public class CourseRestController {

    private final CourseService courseService;
    public CourseRestController(
        CourseService courseService
    ) {
        this.courseService = courseService;
    }
    @GetMapping("")
    public ResponseEntity<CourseResponsedto> getMethodName(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) Integer minAge,
        @RequestParam(required = false) Integer maxAge,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) Double minPrice,
        @RequestParam(required = false) Double maxPrice,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) String startDate,
        @RequestParam(defaultValue = "upcoming") String sort,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size

    ) throws ElasticsearchException, IOException, ParseException {
        Date date = null;
        if(startDate != null && !startDate.isEmpty()){
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            date = formatter.parse(startDate);
        }
       CourseResponsedto responsedto = courseService.searchResponse(q,minAge,maxAge,category,type,minPrice,maxPrice,date,sort,page,size);

        return new ResponseEntity<CourseResponsedto>(responsedto, org.springframework.http.HttpStatus.OK);
        
    }

    @GetMapping("/suggest")
    public List<String> getMethodName(@RequestParam String q) throws IOException {
         List<String> l =    courseService.suggestCourses(q);
        return l;
    }
    
    

}
