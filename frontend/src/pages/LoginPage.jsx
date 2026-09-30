import { useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { login } from '../store/slices/authSlice'

export default function LoginPage() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const location = useLocation()
  const { status, error } = useSelector((state) => state.auth)

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')

  async function handleSubmit(e) {
    e.preventDefault()
    const result = await dispatch(login({ username, password }))
    if (login.fulfilled.match(result)) {
      const redirectTo = location.state?.from?.pathname || '/matches'
      navigate(redirectTo, { replace: true })
    }
  }

  return (
    <div className="page auth-page">
      <div className="card auth-card">
        <h1>Log in</h1>
        <p className="subtitle">Access your cricket analytics account</p>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="username">Username</label>
            <input
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              autoFocus
            />
          </div>
          <div className="field">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
          <button className="btn btn-primary" type="submit" disabled={status === 'loading'}>
            {status === 'loading' ? 'Logging in…' : 'Log in'}
          </button>
        </form>

        <p className="auth-switch">
          No account? <Link to="/register">Register</Link>
        </p>
      </div>
    </div>
  )
}
