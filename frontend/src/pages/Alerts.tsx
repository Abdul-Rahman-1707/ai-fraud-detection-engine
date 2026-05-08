import { useEffect, useState } from 'react'
import { fetchAlerts, updateAlertStatus, type AlertResponse } from '../services/api'

const riskColors: Record<string, string> = {
  LOW: 'bg-green-500/20 text-green-400',
  MEDIUM: 'bg-yellow-500/20 text-yellow-400',
  HIGH: 'bg-orange-500/20 text-orange-400',
  CRITICAL: 'bg-red-500/20 text-red-400',
}

export default function Alerts() {
  const [alerts, setAlerts] = useState<AlertResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState('OPEN')

  useEffect(() => {
    setLoading(true)
    fetchAlerts(filter)
      .then(data => setAlerts(data.content || []))
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [filter])

  const handleStatusUpdate = async (id: string, status: string) => {
    const updated = await updateAlertStatus(id, status)
    setAlerts(prev => prev.map(a => (a.id === id ? updated : a)))
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-bold">Fraud Alerts</h2>
        <div className="flex gap-2">
          {['OPEN', 'INVESTIGATING', 'CONFIRMED_FRAUD', 'FALSE_POSITIVE', 'RESOLVED'].map(s => (
            <button
              key={s}
              onClick={() => setFilter(s)}
              className={`px-3 py-1 rounded-lg text-xs font-medium transition-colors ${
                filter === s ? 'bg-red-500 text-white' : 'bg-gray-800 text-gray-400 hover:bg-gray-700'
              }`}
            >
              {s.replace('_', ' ')}
            </button>
          ))}
        </div>
      </div>

      {loading ? (
        <div className="text-gray-500 text-center py-16">Loading alerts...</div>
      ) : alerts.length === 0 ? (
        <div className="text-center text-gray-500 py-16 bg-gray-900 rounded-xl border border-gray-800">
          <p className="text-lg">No {filter.toLowerCase().replace('_', ' ')} alerts</p>
        </div>
      ) : (
        <div className="space-y-4">
          {alerts.map(alert => (
            <div key={alert.id} className="bg-gray-900 border border-gray-800 rounded-xl p-5">
              <div className="flex items-start justify-between mb-3">
                <div>
                  <div className="flex items-center gap-3 mb-1">
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${riskColors[alert.riskLevel]}`}>
                      {alert.riskLevel}
                    </span>
                    <span className="text-sm text-gray-400">Score: {(alert.riskScore * 100).toFixed(1)}%</span>
                  </div>
                  <p className="text-xs text-gray-500">
                    Transaction: {alert.transactionId.slice(0, 12)}... | User: {alert.userId}
                  </p>
                </div>
                <div className="flex gap-2">
                  {alert.alertStatus === 'OPEN' && (
                    <>
                      <button onClick={() => handleStatusUpdate(alert.id, 'INVESTIGATING')}
                        className="px-3 py-1 bg-yellow-500/20 text-yellow-400 rounded-lg text-xs hover:bg-yellow-500/30">
                        Investigate
                      </button>
                      <button onClick={() => handleStatusUpdate(alert.id, 'FALSE_POSITIVE')}
                        className="px-3 py-1 bg-gray-700 text-gray-300 rounded-lg text-xs hover:bg-gray-600">
                        False Positive
                      </button>
                    </>
                  )}
                </div>
              </div>

              {alert.ruleViolations && (
                <div className="mt-3 p-3 bg-gray-800/50 rounded-lg">
                  <p className="text-xs text-gray-400 mb-1 font-medium">Rule Violations</p>
                  <p className="text-xs text-gray-300 whitespace-pre-line">{alert.ruleViolations}</p>
                </div>
              )}

              {alert.aiAnalysis && (
                <div className="mt-2 p-3 bg-gray-800/50 rounded-lg">
                  <p className="text-xs text-gray-400 mb-1 font-medium">AI Analysis</p>
                  <p className="text-xs text-gray-300 whitespace-pre-line">{alert.aiAnalysis.slice(0, 300)}</p>
                </div>
              )}

              <p className="text-xs text-gray-600 mt-3">{new Date(alert.createdAt).toLocaleString()}</p>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
