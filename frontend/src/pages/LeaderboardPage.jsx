import { useEffect } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Link } from 'react-router-dom'
import { fetchLeaderboards } from '../store/slices/playerSlice'

export default function LeaderboardPage() {
  const dispatch = useDispatch()
  const { leaderboardRuns, leaderboardWickets, leaderboardStatus } = useSelector(
    (state) => state.players
  )

  useEffect(() => {
    dispatch(fetchLeaderboards())
  }, [dispatch])

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>Leaderboard</h1>
          <p className="subtitle">Top performers across all matches</p>
        </div>
      </div>

      {leaderboardStatus === 'loading' && <div className="loading-state">Loading leaderboard…</div>}

      <div className="grid grid-2">
        <LeaderboardTable title="Top run scorers" entries={leaderboardRuns} unit="runs" />
        <LeaderboardTable title="Top wicket takers" entries={leaderboardWickets} unit="wickets" />
      </div>
    </div>
  )
}

function LeaderboardTable({ title, entries, unit }) {
  return (
    <div className="card">
      <h3>{title}</h3>
      {entries.length === 0 ? (
        <p className="subtitle">No data yet.</p>
      ) : (
        <table>
          <tbody>
            {entries.map((entry, i) => (
              <tr key={entry.playerId}>
                <td className="mono leaderboard-rank">{i + 1}</td>
                <td>
                  <Link to={`/players/${entry.playerId}`}>{entry.name}</Link>
                </td>
                <td className="mono leaderboard-value">
                  {entry.value} {unit}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
