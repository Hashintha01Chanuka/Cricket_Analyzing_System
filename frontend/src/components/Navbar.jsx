import { Link, useNavigate } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { logout, selectCurrentUser, selectIsAuthenticated } from '../store/slices/authSlice'

export default function Navbar() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const isAuthenticated = useSelector(selectIsAuthenticated)
  const user = useSelector(selectCurrentUser)

  function handleLogout() {
    dispatch(logout())
    navigate('/login')
  }

  return (
    <nav className="navbar">
      <div className="navbar-inner">
        <Link to="/" className="navbar-brand">
          <span className="navbar-brand-mark">CA</span>
          Cricket Analytics
        </Link>

        <div className="navbar-links">
          <Link to="/matches">Matches</Link>
          <Link to="/players">Players</Link>
          <Link to="/leaderboard">Leaderboard</Link>
        </div>

        <div className="navbar-user">
          {isAuthenticated ? (
            <>
              <span className="navbar-username">
                {user.username} <span className="badge badge-role">{user.role}</span>
              </span>
              <button className="btn btn-ghost btn-sm" onClick={handleLogout}>
                Log out
              </button>
            </>
          ) : (
            <Link to="/login" className="btn btn-primary btn-sm">
              Log in
            </Link>
          )}
        </div>
      </div>
    </nav>
  )
}
