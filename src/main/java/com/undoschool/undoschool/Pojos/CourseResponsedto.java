package com.undoschool.undoschool.Pojos;

import java.util.List;

import lombok.Data;

@Data
public class CourseResponsedto {

    private Long total;
    private Integer page;
    private Integer size;
    private List<CourseDocument> courses;

}
