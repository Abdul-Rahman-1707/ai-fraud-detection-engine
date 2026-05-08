import { type LucideIcon } from 'lucide-react'

interface StatCardProps {
  title: string
  value: string | number
  icon: LucideIcon
  color: 'red' | 'green' | 'yellow' | 'blue'
  subtitle?: string
}

const colorMap = {
  red: 'bg-red-500/10 text-red-400 border-red-500/20',
  green: 'bg-green-500/10 text-green-400 border-green-500/20',
  yellow: 'bg-yellow-500/10 text-yellow-400 border-yellow-500/20',
  blue: 'bg-blue-500/10 text-blue-400 border-blue-500/20',
}

export default function StatCard({ title, value, icon: Icon, color, subtitle }: StatCardProps) {
  return (
    <div className={`rounded-xl border p-5 ${colorMap[color]}`}>
      <div className="flex items-center justify-between">
        <div>
          <p className="text-xs uppercase tracking-wider opacity-70">{title}</p>
          <p className="text-2xl font-bold mt-1">{value}</p>
          {subtitle && <p className="text-xs opacity-50 mt-1">{subtitle}</p>}
        </div>
        <Icon size={32} className="opacity-30" />
      </div>
    </div>
  )
}
