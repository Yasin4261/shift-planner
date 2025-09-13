<template>
  <div class="employees-view">
    <div class="header">
      <h1>Çalışan Yönetimi</h1>
      <button @click="showAddModal = true" class="btn btn-primary">
        Yeni Çalışan Ekle
      </button>
    </div>

    <div class="search-bar">
      <input
        v-model="searchTerm"
        type="text"
        placeholder="Çalışan ara..."
        @input="searchEmployees"
        class="search-input"
      />
    </div>

    <div class="employees-list">
      <div v-if="loading" class="loading">Yükleniyor...</div>
      <div v-else-if="employees.length === 0" class="no-data">
        Çalışan bulunamadı.
      </div>
      <div v-else class="employees-grid">
        <div
          v-for="employee in employees"
          :key="employee.id"
          class="employee-card"
        >
          <div class="employee-info">
            <h3>{{ employee.firstName }} {{ employee.lastName }}</h3>
            <p class="email">{{ employee.email }}</p>
            <p class="phone">{{ employee.phoneNumber }}</p>
            <span class="role" :class="employee.role.toLowerCase()">
              {{ employee.role === 'MANAGER' ? 'Yönetici' : 'Çalışan' }}
            </span>
            <span class="status" :class="{ active: employee.active }">
              {{ employee.active ? 'Aktif' : 'Pasif' }}
            </span>
          </div>
          <div class="employee-actions">
            <button @click="editEmployee(employee)" class="btn btn-secondary">
              Düzenle
            </button>
            <button 
              @click="deleteEmployee(employee.id!)" 
              class="btn btn-danger"
              :disabled="employee.role === 'MANAGER'"
            >
              Sil
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Add/Edit Employee Modal -->
    <div v-if="showAddModal || editingEmployee" class="modal-overlay">
      <div class="modal">
        <h2>{{ editingEmployee ? 'Çalışanı Düzenle' : 'Yeni Çalışan Ekle' }}</h2>
        <form @submit.prevent="saveEmployee">
          <div class="form-group">
            <label>Ad:</label>
            <input
              v-model="employeeForm.firstName"
              type="text"
              required
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label>Soyad:</label>
            <input
              v-model="employeeForm.lastName"
              type="text"
              required
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label>E-posta:</label>
            <input
              v-model="employeeForm.email"
              type="email"
              required
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label>Telefon:</label>
            <input
              v-model="employeeForm.phoneNumber"
              type="tel"
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label>Rol:</label>
            <select v-model="employeeForm.role" required class="form-input">
              <option value="EMPLOYEE">Çalışan</option>
              <option value="MANAGER">Yönetici</option>
            </select>
          </div>
          <div class="form-group">
            <label>
              <input v-model="employeeForm.active" type="checkbox" />
              Aktif
            </label>
          </div>
          <div class="modal-actions">
            <button type="submit" class="btn btn-primary">
              {{ editingEmployee ? 'Güncelle' : 'Ekle' }}
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
import { ref, onMounted, reactive } from 'vue'
import { employeeService } from '../services/employeeService'
import type { Employee, EmployeeRole } from '../types'

const employees = ref<Employee[]>([])
const loading = ref(false)
const searchTerm = ref('')
const showAddModal = ref(false)
const editingEmployee = ref<Employee | null>(null)

const employeeForm = reactive<Omit<Employee, 'id'>>({
  firstName: '',
  lastName: '',
  email: '',
  phoneNumber: '',
  role: 'EMPLOYEE' as EmployeeRole,
  active: true
})

const loadEmployees = async () => {
  loading.value = true
  try {
    employees.value = await employeeService.getAllEmployees()
  } catch (error) {
    console.error('Error loading employees:', error)
  } finally {
    loading.value = false
  }
}

const searchEmployees = async () => {
  if (searchTerm.value.trim()) {
    try {
      employees.value = await employeeService.searchEmployees(searchTerm.value)
    } catch (error) {
      console.error('Error searching employees:', error)
    }
  } else {
    await loadEmployees()
  }
}

const saveEmployee = async () => {
  try {
    if (editingEmployee.value) {
      await employeeService.updateEmployee(editingEmployee.value.id!, {
        ...employeeForm,
        id: editingEmployee.value.id
      })
    } else {
      await employeeService.createEmployee(employeeForm)
    }
    await loadEmployees()
    closeModal()
  } catch (error) {
    console.error('Error saving employee:', error)
  }
}

const editEmployee = (employee: Employee) => {
  editingEmployee.value = employee
  Object.assign(employeeForm, employee)
}

const deleteEmployee = async (id: number) => {
  if (confirm('Bu çalışanı silmek istediğinizden emin misiniz?')) {
    try {
      await employeeService.deleteEmployee(id)
      await loadEmployees()
    } catch (error) {
      console.error('Error deleting employee:', error)
    }
  }
}

const closeModal = () => {
  showAddModal.value = false
  editingEmployee.value = null
  Object.assign(employeeForm, {
    firstName: '',
    lastName: '',
    email: '',
    phoneNumber: '',
    role: 'EMPLOYEE' as EmployeeRole,
    active: true
  })
}

onMounted(() => {
  loadEmployees()
})
</script>

<style scoped>
.employees-view {
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

.search-bar {
  margin-bottom: 20px;
}

.search-input {
  width: 100%;
  max-width: 400px;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}

.employees-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.employee-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 16px;
  background: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.employee-info h3 {
  margin: 0 0 8px 0;
  color: #333;
}

.employee-info p {
  margin: 4px 0;
  color: #666;
}

.role {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
  text-transform: uppercase;
  margin-right: 8px;
}

.role.manager {
  background: #4CAF50;
  color: white;
}

.role.employee {
  background: #2196F3;
  color: white;
}

.status {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

.status.active {
  background: #E8F5E8;
  color: #2E7D32;
}

.status:not(.active) {
  background: #FFEBEE;
  color: #C62828;
}

.employee-actions {
  margin-top: 16px;
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

.loading, .no-data {
  text-align: center;
  padding: 40px;
  color: #666;
}
</style>