import { useEffect, useState } from 'react'
import { Activity, ShieldOff, ShieldCheck, AlertTriangle } from 'lucide-react'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts'
import StatCard from '../components/StatCard'
import { fetchDashboardStats, type DashboardStats } from '../services/api'

const COLORS = ['#22c55e', '#eab308', '#ef4444', '#6366f1']

export default function Dashboard() {
  const [stats, setStats] = useState<DashboardStats | null>(null)

  useEffect(() => {
    fetchDashboardStats().then(setStats).catch(console.error)
  }, [])

  if (!stats) {
    return <div className="flex items-center justify-center h-full text-gray-500">Loading dashboard...</div>
  }

  const pieData = [
    { name: 'Approved', value: stats.approvedTransactions },
    { name: 'Flagged', value: stats.flaggedTransactions },
    { name: 'Blocked', value: stats.blockedTransactions },
    { name: 'Under Review', value: stats.totalTransactions - stats.approvedTransactions - stats.flaggedTransactions - stats.blockedTransactions },
  ].filter(d => d.value > 0)

  const barData = [
    { name: 'Processed', amount: stats.totalAmountProcessed },
    { name: 'Blocked', amount: stats.totalAmountBlocked },
  ]

  return (
    <div>
      <h2 className="text-2xl font-bold mb-6">Fraud Detection Dashboard</h2>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <StatCard title="Total Transactions" value={stats.totalTransactions.toLocaleString()} icon={Activity} color="blue" />
        <StatCard title="Approved" value={stats.approvedTransactions.toLocaleString()} icon={ShieldCheck} color="green" />
        <StatCard title="Blocked" value={stats.blockedTransactions.toLocaleString()} icon={ShieldOff} color="red" />
        <StatCard title="Open Alerts" value={stats.openAlerts} icon={AlertTriangle} color="yellow"
          subtitle={`Avg Score: ${(stats.avgFraudScore * 100).toFixed(1)}%`} />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-gray-900 border border-gray-800 rounded-xl p-6">
          <h3 className="text-sm font-medium text-gray-400 mb-4">Transaction Status Distribution</h3>
          <ResponsiveContainer width="100%" height={250}>
            <PieChart>
              <Pie data={pieData} cx="50%" cy="50%" innerRadius={60} outerRadius={90} dataKey="value" label={({ name, value }) => `${name}: ${value}`}>
                {pieData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
              </Pie>
              <Tooltip contentStyle={{ backgroundColor: '#1f2937', border: 'none', borderRadius: '8px' }} />
            </PieChart>
          </ResponsiveContainer>
        </div>

        <div className="bg-gray-900 border border-gray-800 rounded-xl p-6">
          <h3 className="text-sm font-medium text-gray-400 mb-4">Amount Processed vs Blocked</h3>
          <ResponsiveContainer width="100%" height={250}>
            <BarChart data={barData}>
              <XAxis dataKey="name" stroke="#6b7280" />
              <YAxis stroke="#6b7280" tickFormatter={(v) => `$${(v / 1000).toFixed(0)}k`} />
              <Tooltip contentStyle={{ backgroundColor: '#1f2937', border: 'none', borderRadius: '8px' }}
                formatter={(v: number) => [`$${v.toLocaleString()}`, 'Amount']} />
              <Bar dataKey="amount" fill="#3b82f6" radius={[6, 6, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  )
}
