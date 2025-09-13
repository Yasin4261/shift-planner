export interface Employee {
  id?: number
  firstName: string
  lastName: string
  email: string
  phoneNumber?: string
  role: EmployeeRole
  active: boolean
}

export interface Shift {
  id?: number
  startTime: string
  endTime: string
  employee: Employee
  type: ShiftType
  notes?: string
}

export type ShiftType = 'MORNING' | 'AFTERNOON' | 'EVENING' | 'NIGHT'

export type EmployeeRole = 'MANAGER' | 'EMPLOYEE'