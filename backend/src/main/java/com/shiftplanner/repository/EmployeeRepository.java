package com.shiftplanner.repository;

import com.shiftplanner.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    List<Employee> findByActiveTrue();
    
    Optional<Employee> findByEmail(String email);
    
    List<Employee> findByRole(Employee.Role role);
    
    List<Employee> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
        String firstName, String lastName);
}