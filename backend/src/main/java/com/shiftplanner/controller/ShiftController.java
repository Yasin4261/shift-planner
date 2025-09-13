package com.shiftplanner.controller;

import com.shiftplanner.entity.Shift;
import com.shiftplanner.entity.Employee;
import com.shiftplanner.service.ShiftService;
import com.shiftplanner.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/shifts")
@CrossOrigin(origins = "*")
public class ShiftController {
    
    @Autowired
    private ShiftService shiftService;
    
    @Autowired
    private EmployeeService employeeService;
    
    @GetMapping
    public List<Shift> getAllShifts() {
        return shiftService.getAllShifts();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Shift> getShiftById(@PathVariable Long id) {
        Optional<Shift> shift = shiftService.getShiftById(id);
        return shift.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<?> createShift(@Valid @RequestBody Shift shift) {
        try {
            // Validate employee exists
            if (shift.getEmployee() != null && shift.getEmployee().getId() != null) {
                Optional<Employee> employee = employeeService.getEmployeeById(shift.getEmployee().getId());
                if (!employee.isPresent()) {
                    return ResponseEntity.badRequest().body("Employee not found");
                }
                shift.setEmployee(employee.get());
            }
            
            Shift savedShift = shiftService.saveShift(shift);
            return ResponseEntity.ok(savedShift);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateShift(@PathVariable Long id, @Valid @RequestBody Shift shift) {
        try {
            Optional<Shift> existingShift = shiftService.getShiftById(id);
            if (!existingShift.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            // Validate employee exists
            if (shift.getEmployee() != null && shift.getEmployee().getId() != null) {
                Optional<Employee> employee = employeeService.getEmployeeById(shift.getEmployee().getId());
                if (!employee.isPresent()) {
                    return ResponseEntity.badRequest().body("Employee not found");
                }
                shift.setEmployee(employee.get());
            }
            
            shift.setId(id);
            Shift updatedShift = shiftService.saveShift(shift);
            return ResponseEntity.ok(updatedShift);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteShift(@PathVariable Long id) {
        Optional<Shift> shift = shiftService.getShiftById(id);
        if (!shift.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        shiftService.deleteShift(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Shift>> getShiftsByEmployee(@PathVariable Long employeeId) {
        Optional<Employee> employee = employeeService.getEmployeeById(employeeId);
        if (!employee.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        List<Shift> shifts = shiftService.getShiftsByEmployee(employee.get());
        return ResponseEntity.ok(shifts);
    }
    
    @GetMapping("/date-range")
    public List<Shift> getShiftsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return shiftService.getShiftsByDateRange(start, end);
    }
    
    @GetMapping("/type/{type}")
    public List<Shift> getShiftsByType(@PathVariable Shift.ShiftType type) {
        return shiftService.getShiftsByType(type);
    }
    
    @GetMapping("/conflicts")
    public ResponseEntity<Boolean> checkConflicts(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) Long excludeShiftId) {
        
        boolean hasConflicts = shiftService.hasConflicts(employeeId, startTime, endTime, excludeShiftId);
        return ResponseEntity.ok(hasConflicts);
    }
}