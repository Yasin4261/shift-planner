import api from './api'
import type { Employee } from '../types'

export const employeeService = {
  async getAllEmployees(): Promise<Employee[]> {
    const response = await api.get('/employees')
    return response.data
  },

  async getActiveEmployees(): Promise<Employee[]> {
    const response = await api.get('/employees/active')
    return response.data
  },

  async getEmployeeById(id: number): Promise<Employee> {
    const response = await api.get(`/employees/${id}`)
    return response.data
  },

  async createEmployee(employee: Omit<Employee, 'id'>): Promise<Employee> {
    const response = await api.post('/employees', employee)
    return response.data
  },

  async updateEmployee(id: number, employee: Employee): Promise<Employee> {
    const response = await api.put(`/employees/${id}`, employee)
    return response.data
  },

  async deleteEmployee(id: number): Promise<void> {
    await api.delete(`/employees/${id}`)
  },

  async searchEmployees(term: string): Promise<Employee[]> {
    const response = await api.get(`/employees/search?term=${term}`)
    return response.data
  }
}