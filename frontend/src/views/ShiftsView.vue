<template>
  <div class="shifts-view">
    <div class="header">
      <h1>Vardiya Yönetimi</h1>
      <button @click="showAddModal = true" class="btn btn-primary">
        Yeni Vardiya Ekle
      </button>
    </div>

    <div class="filters">
      <div class="filter-group">
        <label>Çalışan:</label>
        <select v-model="selectedEmployeeId" @change="filterShifts" class="form-input">
          <option value="">Tüm Çalışanlar</option>
          <option v-for="employee in employees" :key="employee.id" :value="employee.id">
            {{ employee.firstName }} {{ employee.lastName }}
          </option>
        </select>
      </div>
      <div class="filter-group">
        <label>Vardiya Tipi:</label>
        <select v-model="selectedShiftType" @change="filterShifts" class="form-input">
          <option value="">Tüm Vardiyalar</option>
          <option value="MORNING">Sabah</option>
          <option value="AFTERNOON">Öğleden Sonra</option>
          <option value="EVENING">Akşam</option>
          <option value="NIGHT">Gece</option>
        </select>
      </div>
      <div class="filter-group">
        <label>Tarih Aralığı:</label>
        <div class="date-range">
          <input
            v-model="startDate"
            type="date"
            @change="filterShifts"
            class="form-input"
          />
          <span>-</span>
          <input
            v-model="endDate"
            type="date"
            @change="filterShifts"
            class="form-input"
          />
        </div>
      </div>
    </div>

    <div class="shifts-list">
      <div v-if="loading" class="loading">Yükleniyor...</div>
      <div v-else-if="shifts.length === 0" class="no-data">
        Vardiya bulunamadı.
      </div>
      <div v-else class="shifts-table">
        <table>
          <thead>
            <tr>
              <th>Çalışan</th>
              <th>Başlangıç</th>
              <th>Bitiş</th>
              <th>Tip</th>
              <th>Notlar</th>
              <th>İşlemler</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="shift in shifts" :key="shift.id">
              <td>{{ shift.employee.firstName }} {{ shift.employee.lastName }}</td>
              <td>{{ formatDateTime(shift.startTime) }}</td>
              <td>{{ formatDateTime(shift.endTime) }}</td>
              <td>
                <span class="shift-type" :class="shift.type.toLowerCase()">
                  {{ getShiftTypeName(shift.type) }}
                </span>
              </td>
              <td>{{ shift.notes || '-' }}</td>
              <td>
                <div class="actions">
                  <button @click="editShift(shift)" class="btn btn-sm btn-secondary">
                    Düzenle
                  </button>
                  <button @click="deleteShift(shift.id!)" class="btn btn-sm btn-danger">
                    Sil
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Add/Edit Shift Modal -->
    <div v-if="showAddModal || editingShift" class="modal-overlay">
      <div class="modal">
        <h2>{{ editingShift ? 'Vardiyayı Düzenle' : 'Yeni Vardiya Ekle' }}</h2>
        <form @submit.prevent="saveShift">
          <div class="form-group">
            <label>Çalışan:</label>
            <select v-model="shiftForm.employeeId" required class="form-input">
              <option value="">Çalışan Seçin</option>
              <option v-for="employee in employees" :key="employee.id" :value="employee.id">
                {{ employee.firstName }} {{ employee.lastName }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>Başlangıç Tarihi ve Saati:</label>
            <input
              v-model="shiftForm.startTime"
              type="datetime-local"
              required
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label>Bitiş Tarihi ve Saati:</label>
            <input
              v-model="shiftForm.endTime"
              type="datetime-local"
              required
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label>Vardiya Tipi:</label>
            <select v-model="shiftForm.type" required class="form-input">
              <option value="MORNING">Sabah</option>
              <option value="AFTERNOON">Öğleden Sonra</option>
              <option value="EVENING">Akşam</option>
              <option value="NIGHT">Gece</option>
            </select>
          </div>
          <div class="form-group">
            <label>Notlar:</label>
            <textarea
              v-model="shiftForm.notes"
              rows="3"
              class="form-input"
              placeholder="Vardiya ile ilgili notlar..."
            ></textarea>
          </div>
          <div v-if="conflictError" class="error-message">
            {{ conflictError }}
          </div>
          <div class="modal-actions">
            <button type="submit" class="btn btn-primary" :disabled="saving">
              {{ saving ? 'Kaydediliyor...' : (editingShift ? 'Güncelle' : 'Ekle') }}
            </button>
            <button type="button" @click="closeModal" class="btn btn-secondary">
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
const showAddModal = ref(false)
const editingShift = ref<Shift | null>(null)
const conflictError = ref('')

// Filters
const selectedEmployeeId = ref<number | ''>('')
const selectedShiftType = ref<ShiftType | ''>('')
const startDate = ref('')
const endDate = ref('')

const shiftForm = reactive({
  employeeId: null as number | null,
  startTime: '',
  endTime: '',
  type: 'MORNING' as ShiftType,
  notes: ''
})

const loadEmployees = async () => {
  try {
    employees.value = await employeeService.getActiveEmployees()
  } catch (error) {
    console.error('Error loading employees:', error)
  }
}

const loadShifts = async () => {
  loading.value = true
  try {
    shifts.value = await shiftService.getAllShifts()
  } catch (error) {
    console.error('Error loading shifts:', error)
  } finally {
    loading.value = false
  }
}

const filterShifts = async () => {
  loading.value = true
  try {
    if (startDate.value && endDate.value) {
      const start = new Date(startDate.value).toISOString()
      const end = new Date(endDate.value + 'T23:59:59').toISOString()
      shifts.value = await shiftService.getShiftsByDateRange(start, end)
    } else if (selectedEmployeeId.value) {
      shifts.value = await shiftService.getShiftsByEmployee(selectedEmployeeId.value as number)
    } else if (selectedShiftType.value) {
      shifts.value = await shiftService.getShiftsByType(selectedShiftType.value as ShiftType)
    } else {
      await loadShifts()
    }
  } catch (error) {
    console.error('Error filtering shifts:', error)
  } finally {
    loading.value = false
  }
}

const saveShift = async () => {
  conflictError.value = ''
  saving.value = true
  
  try {
    const selectedEmployee = employees.value.find(emp => emp.id === shiftForm.employeeId)!
    
    const shiftData = {
      startTime: new Date(shiftForm.startTime).toISOString(),
      endTime: new Date(shiftForm.endTime).toISOString(),
      employee: selectedEmployee,
      type: shiftForm.type,
      notes: shiftForm.notes
    }

    // Check for conflicts
    const hasConflicts = await shiftService.checkConflicts(
      shiftForm.employeeId!,
      shiftData.startTime,
      shiftData.endTime,
      editingShift.value?.id
    )

    if (hasConflicts) {
      conflictError.value = 'Bu çalışanın seçilen saatlerde başka bir vardiyası bulunmaktadır.'
      return
    }

    if (editingShift.value) {
      await shiftService.updateShift(editingShift.value.id!, {
        ...shiftData,
        id: editingShift.value.id
      })
    } else {
      await shiftService.createShift(shiftData)
    }
    
    await loadShifts()
    closeModal()
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
  editingShift.value = shift
  shiftForm.employeeId = shift.employee.id!
  shiftForm.startTime = new Date(shift.startTime).toISOString().slice(0, 16)
  shiftForm.endTime = new Date(shift.endTime).toISOString().slice(0, 16)
  shiftForm.type = shift.type
  shiftForm.notes = shift.notes || ''
}

const deleteShift = async (id: number) => {
  if (confirm('Bu vardiyayı silmek istediğinizden emin misiniz?')) {
    try {
      await shiftService.deleteShift(id)
      await loadShifts()
    } catch (error) {
      console.error('Error deleting shift:', error)
    }
  }
}

const closeModal = () => {
  showAddModal.value = false
  editingShift.value = null
  conflictError.value = ''
  Object.assign(shiftForm, {
    employeeId: null,
    startTime: '',
    endTime: '',
    type: 'MORNING' as ShiftType,
    notes: ''
  })
}

const formatDateTime = (dateTime: string) => {
  return new Date(dateTime).toLocaleString('tr-TR')
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
  loadEmployees()
  loadShifts()
})
</script>

<style scoped>
.shifts-view {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filters {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding: 16px;
  background: #f5f5f5;
  border-radius: 8px;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  flex-direction: column;
  min-width: 150px;
}

.filter-group label {
  margin-bottom: 4px;
  font-weight: 500;
  font-size: 14px;
}

.date-range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.shifts-table {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

th {
  background: #f8f9fa;
  font-weight: 600;
  color: #495057;
}

.shift-type {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
  text-transform: uppercase;
}

.shift-type.morning {
  background: #FFF3CD;
  color: #856404;
}

.shift-type.afternoon {
  background: #D1ECF1;
  color: #0C5460;
}

.shift-type.evening {
  background: #F8D7DA;
  color: #721C24;
}

.shift-type.night {
  background: #D4EDDA;
  color: #155724;
}

.actions {
  display: flex;
  gap: 8px;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
}

.btn-sm {
  padding: 4px 8px;
  font-size: 12px;
}

.btn-primary {
  background: #007bff;
  color: white;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-danger {
  background: #dc3545;
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
  max-width: 500px;
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
}

.loading, .no-data {
  text-align: center;
  padding: 40px;
  color: #666;
}
</style>