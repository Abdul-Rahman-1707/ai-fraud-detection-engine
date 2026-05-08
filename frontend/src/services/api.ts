import axios from 'axios'

const api = axios.create({ baseURL: '/api' })

export interface TransactionResponse {
  id: string
  userId: string
  amount: number
  currency: string
  merchantName: string
  merchantCategory: string
  country: string
  timestamp: string
  status: 'PENDING' | 'APPROVED' | 'FLAGGED' | 'BLOCKED' | 'UNDER_REVIEW'
  fraudScore: number | null
  fraudReason: string | null
}

export interface DashboardStats {
  totalTransactions: number
  flaggedTransactions: number
  blockedTransactions: number
  approvedTransactions: number
  totalAmountProcessed: number
  totalAmountBlocked: number
  avgFraudScore: number
  openAlerts: number
}

export interface AlertResponse {
  id: string
  transactionId: string
  userId: string
  riskScore: number
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
  ruleViolations: string
  aiAnalysis: string
  alertStatus: string
  createdAt: string
}

export const fetchDashboardStats = () =>
  api.get<DashboardStats>('/transactions/dashboard/stats').then(r => r.data)

export const fetchTransactions = (page = 0, size = 20) =>
  api.get<{ content: TransactionResponse[] }>(`/transactions?page=${page}&size=${size}`).then(r => r.data)

export const fetchAlerts = (status = 'OPEN', page = 0) =>
  api.get<{ content: AlertResponse[] }>(`/alerts?status=${status}&page=${page}`).then(r => r.data)

export const updateAlertStatus = (id: string, status: string) =>
  api.patch<AlertResponse>(`/alerts/${id}/status?status=${status}`).then(r => r.data)
