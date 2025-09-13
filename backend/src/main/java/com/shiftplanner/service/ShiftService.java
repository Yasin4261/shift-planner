package com.shiftplanner.service;

import com.shiftplanner.entity.Shift;
import com.shiftplanner.entity.Employee;
import com.shiftplanner.repository.ShiftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ShiftService {
    
    @Autowired
    private ShiftRepository shiftRepository;
    
    public List<Shift> getAllShifts() {
        return shiftRepository.findAll();
    }
    
    public Optional<Shift> getShiftById(Long id) {
        return shiftRepository.findById(id);
    }
    
    public Shift saveShift(Shift shift) throws Exception {
        // Check for conflicts before saving
        List<Shift> conflicts = shiftRepository.findConflictingShifts(
            shift.getEmployee().getId(),
            shift.getStartTime(),
            shift.getEndTime()
        );
        
        // If updating existing shift, exclude it from conflict check
        if (shift.getId() != null) {
            conflicts.removeIf(s -> s.getId().equals(shift.getId()));
        }
        
        if (!conflicts.isEmpty()) {
            throw new Exception("Shift conflicts with existing shift(s)");
        }
        
        return shiftRepository.save(shift);
    }
    
    public void deleteShift(Long id) {
        shiftRepository.deleteById(id);
    }
    
    public List<Shift> getShiftsByEmployee(Employee employee) {
        return shiftRepository.findByEmployee(employee);
    }
    
    public List<Shift> getShiftsByDateRange(LocalDateTime start, LocalDateTime end) {
        return shiftRepository.findByStartTimeBetween(start, end);
    }
    
    public List<Shift> getShiftsByType(Shift.ShiftType type) {
        return shiftRepository.findByType(type);
    }
    
    public boolean hasConflicts(Long employeeId, LocalDateTime startTime, LocalDateTime endTime, Long excludeShiftId) {
        List<Shift> conflicts = shiftRepository.findConflictingShifts(employeeId, startTime, endTime);
        
        if (excludeShiftId != null) {
            conflicts.removeIf(s -> s.getId().equals(excludeShiftId));
        }
        
        return !conflicts.isEmpty();
    }
}