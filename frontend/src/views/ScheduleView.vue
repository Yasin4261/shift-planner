<template>
  <div class="schedule-view">
    <div class="header">
      <h1>Vardiya Programı</h1>
      <div class="date-navigation">
        <button @click="previousWeek" class="btn btn-secondary">← Önceki Hafta</button>
        <span class="current-week">{{ formatWeekRange() }}</span>
        <button @click="nextWeek" class="btn btn-secondary">Sonraki Hafta →</button>
      </div>
    </div>

    <div v-if="loading" class="loading">Yükleniyor...</div>
    <div v-else class="schedule-grid">
      <div class="schedule-header">
        <div class="employee-column">Çalışan</div>
        <div v-for="day in weekDays" :key="day.date" class="day-column">
          <div class="day-name">{{ day.name }}</div>
          <div class="day-date">{{ day.date }}</div>
        </div>
      </div>
      
      <div v-for="employee in employees" :key="employee.id" class="employee-row">
        <div class="employee-info">
          <div class="employee-name">{{ employee.firstName }} {{ employee.lastName }}</div>
          <div class="employee-role">{{ employee.role === 'MANAGER' ? 'Yönetici' : 'Çalışan' }}</div>
        </div>
        
        <div v-for="day in weekDays" :key="`${employee.id}-${day.date}`" class="day-cell">
          <div 
            v-for="shift in getShiftsForEmployeeAndDay(employee.id!, day.fullDate)" 
            :key="shift.id"
            class="shift-item"
            :class="shift.type.toLowerCase()"
            @click="editShift(shift)"
          >
            <div class="shift-time">
              {{ formatTime(shift.startTime) }} - {{ formatTime(shift.endTime) }}
            </div>
            <div class="shift-type">{{ getShiftTypeName(shift.type) }}</div>
          </div>
          
          <button 
            @click="addShiftForEmployee(employee, day.fullDate)"
            class="add-shift-btn"
            :title="`${employee.firstName} ${employee.lastName} için ${day.name} günü vardiya ekle`"
          >
            +
          </button>
        </div>
      </div>
    </div>

    <!-- Quick Add Shift Modal -->
    <div v-if="showQuickAdd" class="modal-overlay">
      <div class="modal">
        <h2>Hızlı Vardiya Ekle</h2>
        <p>{{ quickAddEmployee?.firstName }} {{ quickAddEmployee?.lastName }} - {{ quickAddDate }}</p>
        <form @submit.prevent="saveQuickShift">
          <div class="form-group">
            <label>Başlangıç Saati:</label>
            <input
              v-model="quickShiftForm.startTime"
              type="time"
              required
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label>Bitiş Saati:</label>
            <input
              v-model="quickShiftForm.endTime"
              type="time"
              required
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label>Vardiya Tipi:</label>
            <select v-model="quickShiftForm.type" required class="form-input">
              <option value="MORNING">Sabah</option>
              <option value="AFTERNOON">Öğleden Sonra</option>
              <option value="EVENING">Akşam</option>
              <option value="NIGHT">Gece</option>
            </select>
          </div>
          <div class="form-group">
            <label>Notlar:</label>
            <input
              v-model="quickShiftForm.notes"
              type="text"
              class="form-input"
              placeholder="Opsiyonel notlar..."
            />
          </div>
          <div v-if="conflictError" class="error-message">
            {{ conflictError }}
          </div>
          <div class="modal-actions">
            <button type="submit" class="btn btn-primary" :disabled="saving">
              {{ saving ? 'Kaydediliyor...' : 'Ekle' }}
            </button>
            <button type="button" @click="closeQuickAdd" class="btn btn-secondary">
              İptal
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { shiftService } from '../services/shiftService'
import { employeeService } from '../services/employeeService'
import type { Shift, Employee, ShiftType } from '../types'

const shifts = ref<Shift[]>([])
const employees = ref<Employee[]>([])
const loading = ref(false)
const saving = ref(false)
const currentWeekStart = ref(new Date())
const showQuickAdd = ref(false)
const quickAddEmployee = ref<Employee | null>(null)
const quickAddDate = ref('')
const conflictError = ref('')

const quickShiftForm = reactive({
  startTime: '',
  endTime: '',
  type: 'MORNING' as ShiftType,
  notes: ''
})

// Set current week to start of Monday
const setCurrentWeekStart = () => {
  const today = new Date()
  const dayOfWeek = today.getDay()
  const diff = dayOfWeek === 0 ? -6 : 1 - dayOfWeek // Adjust for Monday start
  currentWeekStart.value = new Date(today.setDate(today.getDate() + diff))
  currentWeekStart.value.setHours(0, 0, 0, 0)
}

const weekDays = computed(() => {
  const days = []
  const dayNames = ['Pazartesi', 'Salı', 'Çarşamba', 'Perşembe', 'Cuma', 'Cumartesi', 'Pazar']
  
  for (let i = 0; i < 7; i++) {
    const date = new Date(currentWeekStart.value)
    date.setDate(date.getDate() + i)
    
    days.push({
      name: dayNames[i],
      date: date.getDate().toString().padStart(2, '0'),
      fullDate: date.toISOString().split('T')[0]
    })
  }
  
  return days
})

const loadEmployees = async () => {
  try {
    employees.value = await employeeService.getActiveEmployees()
  } catch (error) {
    console.error('Error loading employees:', error)
  }
}

const loadShiftsForWeek = async () => {
  loading.value = true
  try {
    const startDate = new Date(currentWeekStart.value).toISOString()
    const endDate = new Date(currentWeekStart.value)
    endDate.setDate(endDate.getDate() + 6)
    endDate.setHours(23, 59, 59, 999)
    
    shifts.value = await shiftService.getShiftsByDateRange(startDate, endDate.toISOString())
  } catch (error) {
    console.error('Error loading shifts:', error)
  } finally {
    loading.value = false
  }
}

const getShiftsForEmployeeAndDay = (employeeId: number, date: string) => {
  return shifts.value.filter(shift => {
    const shiftDate = new Date(shift.startTime).toISOString().split('T')[0]
    return shift.employee.id === employeeId && shiftDate === date
  })
}

const addShiftForEmployee = (employee: Employee, date: string) => {
  quickAddEmployee.value = employee
  quickAddDate.value = date
  showQuickAdd.value = true
  
  // Reset form
  Object.assign(quickShiftForm, {
    startTime: '',
    endTime: '',
    type: 'MORNING' as ShiftType,
    notes: ''
  })
}

const saveQuickShift = async () => {
  conflictError.value = ''
  saving.value = true
  
  try {
    const shiftDateTime = (time: string) => {
      const [hours, minutes] = time.split(':')
      const date = new Date(quickAddDate.value)
      date.setHours(parseInt(hours), parseInt(minutes), 0, 0)
      return date.toISOString()
    }

    const startDateTime = shiftDateTime(quickShiftForm.startTime)
    const endDateTime = shiftDateTime(quickShiftForm.endTime)

    // Check for conflicts
    const hasConflicts = await shiftService.checkConflicts(
      quickAddEmployee.value!.id!,
      startDateTime,
      endDateTime
    )

    if (hasConflicts) {
      conflictError.value = 'Bu çalışanın seçilen saatlerde başka bir vardiyası bulunmaktadır.'
      return
    }

    const shiftData = {
      startTime: startDateTime,
      endTime: endDateTime,
      employee: quickAddEmployee.value!,
      type: quickShiftForm.type,
      notes: quickShiftForm.notes
    }

    await shiftService.createShift(shiftData)
    await loadShiftsForWeek()
    closeQuickAdd()
  } catch (error: any) {
    console.error('Error saving shift:', error)
    if (error.response?.data) {
      conflictError.value = error.response.data
    } else {
      conflictError.value = 'Vardiya kaydedilirken bir hata oluştu.'
    }
  } finally {
    saving.value = false
  }
}

const editShift = (shift: Shift) => {
  // Navigate to shifts page with edit mode
  // This could be implemented as a router push or emit to parent
  console.log('Edit shift:', shift)
}

const closeQuickAdd = () => {
  showQuickAdd.value = false
  quickAddEmployee.value = null
  quickAddDate.value = ''
  conflictError.value = ''
}

const previousWeek = () => {
  currentWeekStart.value = new Date(currentWeekStart.value.getTime() - 7 * 24 * 60 * 60 * 1000)
  loadShiftsForWeek()
}

const nextWeek = () => {
  currentWeekStart.value = new Date(currentWeekStart.value.getTime() + 7 * 24 * 60 * 60 * 1000)
  loadShiftsForWeek()
}

const formatWeekRange = () => {
  const start = new Date(currentWeekStart.value)
  const end = new Date(start.getTime() + 6 * 24 * 60 * 60 * 1000)
  
  return `${start.toLocaleDateString('tr-TR')} - ${end.toLocaleDateString('tr-TR')}`
}

const formatTime = (dateTime: string) => {
  return new Date(dateTime).toLocaleTimeString('tr-TR', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

const getShiftTypeName = (type: ShiftType) => {
  const names = {
    MORNING: 'Sabah',
    AFTERNOON: 'Öğleden Sonra',
    EVENING: 'Akşam',
    NIGHT: 'Gece'
  }
  return names[type]
}

onMounted(() => {
  setCurrentWeekStart()
  loadEmployees()
  loadShiftsForWeek()
})
</script>

<style scoped>
.schedule-view {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.date-navigation {
  display: flex;
  align-items: center;
  gap: 16px;
}

.current-week {
  font-weight: 600;
  font-size: 16px;
}

.schedule-grid {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.schedule-header {
  display: grid;
  grid-template-columns: 200px repeat(7, 1fr);
  background: #f8f9fa;
  border-bottom: 2px solid #dee2e6;
}

.employee-column {
  padding: 16px;
  font-weight: 600;
  color: #495057;
  border-right: 1px solid #dee2e6;
}

.day-column {
  padding: 12px;
  text-align: center;
  border-right: 1px solid #dee2e6;
}

.day-name {
  font-weight: 600;
  color: #495057;
}

.day-date {
  font-size: 14px;
  color: #6c757d;
  margin-top: 4px;
}

.employee-row {
  display: grid;
  grid-template-columns: 200px repeat(7, 1fr);
  border-bottom: 1px solid #eee;
}

.employee-info {
  padding: 16px;
  border-right: 1px solid #dee2e6;
  background: #fafafa;
}

.employee-name {
  font-weight: 500;
  color: #333;
}

.employee-role {
  font-size: 12px;
  color: #666;
  margin-top: 2px;
}

.day-cell {
  padding: 8px;
  border-right: 1px solid #dee2e6;
  min-height: 80px;
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.shift-item {
  background: #e9ecef;
  border-radius: 4px;
  padding: 6px 8px;
  font-size: 12px;
  cursor: pointer;
  border-left: 4px solid #6c757d;
}

.shift-item.morning {
  background: #fff3cd;
  border-left-color: #ffc107;
}

.shift-item.afternoon {
  background: #d1ecf1;
  border-left-color: #17a2b8;
}

.shift-item.evening {
  background: #f8d7da;
  border-left-color: #dc3545;
}

.shift-item.night {
  background: #d4edda;
  border-left-color: #28a745;
}

.shift-time {
  font-weight: 500;
  line-height: 1.2;
}

.shift-type {
  color: #666;
  font-size: 10px;
  text-transform: uppercase;
}

.add-shift-btn {
  position: absolute;
  bottom: 4px;
  right: 4px;
  width: 24px;
  height: 24px;
  border: 1px dashed #ccc;
  background: white;
  border-radius: 50%;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  display: flex;
  align-items: center;
  justify-content: center;
}

.add-shift-btn:hover {
  background: #f8f9fa;
  border-color: #007bff;
  color: #007bff;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
}

.btn-primary {
  background: #007bff;
  color: white;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  background: white;
  padding: 24px;
  border-radius: 8px;
  width: 90%;
  max-width: 400px;
  max-height: 90vh;
  overflow-y: auto;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 4px;
  font-weight: 500;
}

.form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}

.modal-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 20px;
}

.error-message {
  color: #dc3545;
  background: #f8d7da;
  border: 1px solid #f5c6cb;
  padding: 8px 12px;
  border-radius: 4px;
  margin-bottom: 16px;
  font-size: 14px;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #666;
}

@media (max-width: 768px) {
  .schedule-grid {
    overflow-x: auto;
  }
  
  .schedule-header,
  .employee-row {
    min-width: 800px;
  }
}
</style>