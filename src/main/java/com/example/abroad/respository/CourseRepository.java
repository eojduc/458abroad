package com.example.abroad.respository;

import com.example.abroad.model.User.Course;
import com.example.abroad.model.User.Course.ID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, ID> {

}
