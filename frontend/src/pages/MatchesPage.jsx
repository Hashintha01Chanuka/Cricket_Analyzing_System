import { useEffect } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Link } from 'react-router-dom'
import { fetchMatches } from '../store/slices/matchSlice'
import RoleGuard from '../components/RoleGuard'

export default function MatchesPage() {
  const dispatch = useDispatch()
  const { list, listStatus } = useSelector((state) => state.matches)

  useEffect(() => {
    dispatch(fetchMatches())
  }, [dispatch])

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>Matches</h1>
          <p className="subtitle">Live, scheduled, and completed matches</p>
        </div>
        <RoleGuard allow={['ADMIN']}>
          <Link to="/matches/new" className="btn btn-primary">
            + New match
          </Link>
        </RoleGuard>
      </div>

      {listStatus === 'loading' && <div className="loading-state">Loading matches…</div>}

      {listStatus === 'idle' && list.length === 0 && (
        <div className="empty-state">No matches yet. Check back once one is scheduled.</div>
      )}

      <div className="grid grid-2">
        {list.map((match) => (
          <Link key={match.id} to={`/matches/${match.id}`} className="card card-link match-card">
            <div className="match-card-top">
              <span className={`badge badge-${match.status.toLowerCase()}`}>{match.status}</span>
              <span className="subtitle mono">{match.format}</span>
            </div>
            <h3 className="match-card-teams">
              {match.team1} <span className="match-card-vs">vs</span> {match.team2}
            </h3>
            {match.venue && <p className="subtitle">{match.venue}</p>}
          </Link>
        ))}
      </div>
    </div>
  )
}
