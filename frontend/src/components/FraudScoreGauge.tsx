interface FraudScoreGaugeProps {
  score: number
  size?: number
}

export default function FraudScoreGauge({ score, size = 120 }: FraudScoreGaugeProps) {
  const percentage = Math.round(score * 100)
  const radius = (size - 16) / 2
  const circumference = 2 * Math.PI * radius
  const offset = circumference - (score * circumference)

  const getColor = () => {
    if (score >= 0.75) return '#ef4444'
    if (score >= 0.5) return '#f97316'
    if (score >= 0.25) return '#eab308'
    return '#22c55e'
  }

  const getLabel = () => {
    if (score >= 0.75) return 'CRITICAL'
    if (score >= 0.5) return 'HIGH'
    if (score >= 0.25) return 'MEDIUM'
    return 'LOW'
  }

  return (
    <div className="flex flex-col items-center gap-2">
      <svg width={size} height={size} className="-rotate-90">
        <circle
          cx={size / 2} cy={size / 2} r={radius}
          fill="none" stroke="#1f2937" strokeWidth={8}
        />
        <circle
          cx={size / 2} cy={size / 2} r={radius}
          fill="none" stroke={getColor()} strokeWidth={8}
          strokeDasharray={circumference}
          strokeDashoffset={offset}
          strokeLinecap="round"
          className="transition-all duration-700 ease-out"
        />
      </svg>
      <div className="absolute flex flex-col items-center" style={{ marginTop: size * 0.25 }}>
        <span className="text-2xl font-bold" style={{ color: getColor() }}>{percentage}%</span>
        <span className="text-xs text-gray-500 uppercase tracking-wider">{getLabel()}</span>
      </div>
    </div>
  )
}
