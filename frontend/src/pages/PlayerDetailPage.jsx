import { useEffect } from 'react'
import { useParams } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { fetchPlayer } from '../store/slices/playerSlice'

export default function PlayerDetailPage() {
  const { playerId } = useParams()
  const dispatch = useDispatch()
  const { current: player, currentStats: stats, currentStatus } = useSelector((state) => state.players)

  useEffect(() => {
    dispatch(fetchPlayer(playerId))
  }, [dispatch, playerId])

  if (currentStatus === 'loading' && !player) {
    return (
      <div className="page">
        <div className="loading-state">Loading player…</div>
      </div>
    )
  }

  if (!player) {
    return (
      <div className="page">
        <div className="empty-state">Player not found.</div>
      </div>
    )
  }

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>{player.name}</h1>
          <p className="subtitle">
            {player.country} · {player.role.replace('_', ' ')}
            {player.battingStyle ? ` · ${player.battingStyle}` : ''}
          </p>
        </div>
      </div>

      {stats && (
        <div className="grid grid-3">
          <StatCard label="Matches" value={stats.matches} />
          <StatCard label="Innings" value={stats.innings} />
          <StatCard label="Runs" value={stats.runsScored} />
          <StatCard label="Highest score" value={stats.highestScore} />
          <StatCard label="Batting average" value={stats.battingAverage} />
          <StatCard label="Strike rate" value={stats.battingStrikeRate} />
          <StatCard label="Centuries" value={stats.centuries} />
          <StatCard label="Fifties" value={stats.fifties} />
          <StatCard label="Wickets" value={stats.wicketsTaken} />
          <StatCard label="Bowling average" value={stats.bowlingAverage} />
          <StatCard label="Economy" value={stats.bowlingEconomy} />
        </div>
      )}
    </div>
  )
}

function StatCard({ label, value }) {
  return (
    <div className="card stat-card">
      <span className="stat-card-label">{label}</span>
      <span className="stat-card-value mono">{value}</span>
    </div>
  )
}
