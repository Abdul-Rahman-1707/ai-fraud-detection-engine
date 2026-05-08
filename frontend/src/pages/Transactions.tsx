import { useEffect, useState } from 'react'
import { fetchTransactions, type TransactionResponse } from '../services/api'

const statusColors: Record<string, string> = {
  APPROVED: 'bg-green-500/20 text-green-400',
  FLAGGED: 'bg-yellow-500/20 text-yellow-400',
  BLOCKED: 'bg-red-500/20 text-red-400',
  PENDING: 'bg-gray-500/20 text-gray-400',
  UNDER_REVIEW: 'bg-purple-500/20 text-purple-400',
}

export default function Transactions() {
  const [transactions, setTransactions] = useState<TransactionResponse[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchTransactions()
      .then(data => setTransactions(data.content || []))
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [])

  if (loading) {
    return <div className="flex items-center justify-center h-full text-gray-500">Loading transactions...</div>
  }

  return (
    <div>
      <h2 className="text-2xl font-bold mb-6">Transactions</h2>

      {transactions.length === 0 ? (
        <div className="text-center text-gray-500 py-16 bg-gray-900 rounded-xl border border-gray-800">
          <p className="text-lg">No transactions yet</p>
          <p className="text-sm mt-2">Submit a transaction via the API to see it here</p>
        </div>
      ) : (
        <div className="bg-gray-900 border border-gray-800 rounded-xl overflow-hidden">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-gray-800 text-gray-400 text-left">
                <th className="px-4 py-3">ID</th>
                <th className="px-4 py-3">User</th>
                <th className="px-4 py-3">Amount</th>
                <th className="px-4 py-3">Merchant</th>
                <th className="px-4 py-3">Country</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3">Fraud Score</th>
                <th className="px-4 py-3">Time</th>
              </tr>
            </thead>
            <tbody>
              {transactions.map(txn => (
                <tr key={txn.id} className="border-b border-gray-800/50 hover:bg-gray-800/30">
                  <td className="px-4 py-3 font-mono text-xs">{txn.id.slice(0, 8)}...</td>
                  <td className="px-4 py-3">{txn.userId}</td>
                  <td className="px-4 py-3 font-medium">${txn.amount.toLocaleString()} {txn.currency}</td>
                  <td className="px-4 py-3">{txn.merchantName}</td>
                  <td className="px-4 py-3">{txn.country}</td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${statusColors[txn.status]}`}>
                      {txn.status}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    {txn.fraudScore != null ? (
                      <span className={txn.fraudScore > 0.7 ? 'text-red-400' : txn.fraudScore > 0.4 ? 'text-yellow-400' : 'text-green-400'}>
                        {(txn.fraudScore * 100).toFixed(1)}%
                      </span>
                    ) : '—'}
                  </td>
                  <td className="px-4 py-3 text-gray-500 text-xs">{new Date(txn.timestamp).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
