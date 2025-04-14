package com.example.abroad.respository;

import com.example.abroad.model.User.Course;
import com.example.abroad.model.User.Course.ID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, ID> {

  List<Course> findById_Username(String username);


}
