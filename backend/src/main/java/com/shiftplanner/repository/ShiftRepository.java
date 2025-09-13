package com.shiftplanner.repository;

import com.shiftplanner.entity.Shift;
import com.shiftplanner.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {
    
    List<Shift> findByEmployee(Employee employee);
    
    List<Shift> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT s FROM Shift s WHERE s.employee.id = :employeeId AND " +
           "((s.startTime BETWEEN :startTime AND :endTime) OR " +
           "(s.endTime BETWEEN :startTime AND :endTime) OR " +
           "(s.startTime <= :startTime AND s.endTime >= :endTime))")
    List<Shift> findConflictingShifts(@Param("employeeId") Long employeeId,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);
    
    List<Shift> findByType(Shift.ShiftType type);
}