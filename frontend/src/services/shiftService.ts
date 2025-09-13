import api from './api'
import type { Shift, ShiftType } from '../types'

export const shiftService = {
  async getAllShifts(): Promise<Shift[]> {
    const response = await api.get('/shifts')
    return response.data
  },

  async getShiftById(id: number): Promise<Shift> {
    const response = await api.get(`/shifts/${id}`)
    return response.data
  },

  async createShift(shift: Omit<Shift, 'id'>): Promise<Shift> {
    const response = await api.post('/shifts', shift)
    return response.data
  },

  async updateShift(id: number, shift: Shift): Promise<Shift> {
    const response = await api.put(`/shifts/${id}`, shift)
    return response.data
  },

  async deleteShift(id: number): Promise<void> {
    await api.delete(`/shifts/${id}`)
  },

  async getShiftsByEmployee(employeeId: number): Promise<Shift[]> {
    const response = await api.get(`/shifts/employee/${employeeId}`)
    return response.data
  },

  async getShiftsByDateRange(start: string, end: string): Promise<Shift[]> {
    const response = await api.get(`/shifts/date-range?start=${start}&end=${end}`)
    return response.data
  },

  async getShiftsByType(type: ShiftType): Promise<Shift[]> {
    const response = await api.get(`/shifts/type/${type}`)
    return response.data
  },

  async checkConflicts(
    employeeId: number,
    startTime: string,
    endTime: string,
    excludeShiftId?: number
  ): Promise<boolean> {
    const params = new URLSearchParams({
      employeeId: employeeId.toString(),
      startTime,
      endTime
    })
    
    if (excludeShiftId) {
      params.append('excludeShiftId', excludeShiftId.toString())
    }
    
    const response = await api.get(`/shifts/conflicts?${params}`)
    return response.data
  }
}