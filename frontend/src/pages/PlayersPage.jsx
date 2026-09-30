import { useEffect } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Link } from 'react-router-dom'
import { fetchPlayers } from '../store/slices/playerSlice'
import RoleGuard from '../components/RoleGuard'

export default function PlayersPage() {
  const dispatch = useDispatch()
  const { list, listStatus } = useSelector((state) => state.players)

  useEffect(() => {
    dispatch(fetchPlayers())
  }, [dispatch])

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>Players</h1>
          <p className="subtitle">Profiles and career stats</p>
        </div>
        <RoleGuard allow={['ADMIN']}>
          <Link to="/players/new" className="btn btn-primary">
            + New player
          </Link>
        </RoleGuard>
      </div>

      {listStatus === 'loading' && <div className="loading-state">Loading players…</div>}

      {listStatus === 'idle' && list.length === 0 && (
        <div className="empty-state">No players registered yet.</div>
      )}

      <div className="card">
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Country</th>
              <th>Role</th>
              <th>Batting style</th>
            </tr>
          </thead>
          <tbody>
            {list.map((p) => (
              <tr key={p.id}>
                <td>
                  <Link to={`/players/${p.id}`}>{p.name}</Link>
                </td>
                <td>{p.country}</td>
                <td>{p.role.replace('_', ' ')}</td>
                <td>{p.battingStyle || '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
