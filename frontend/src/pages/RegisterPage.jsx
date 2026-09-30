import { useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Link, useNavigate } from 'react-router-dom'
import { register } from '../store/slices/authSlice'

export default function RegisterPage() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { status, error } = useSelector((state) => state.auth)

  const [form, setForm] = useState({ username: '', email: '', password: '' })

  function update(field) {
    return (e) => setForm((f) => ({ ...f, [field]: e.target.value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    const result = await dispatch(register(form))
    if (register.fulfilled.match(result)) {
      navigate('/matches', { replace: true })
    }
  }

  return (
    <div className="page auth-page">
      <div className="card auth-card">
        <h1>Create account</h1>
        <p className="subtitle">
          New accounts start as a Viewer. An admin can promote you to Scorer or Admin later.
        </p>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="username">Username</label>
            <input
              id="username"
              value={form.username}
              onChange={update('username')}
              minLength={3}
              required
              autoFocus
            />
          </div>
          <div className="field">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              value={form.email}
              onChange={update('email')}
              required
            />
          </div>
          <div className="field">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              value={form.password}
              onChange={update('password')}
              minLength={8}
              required
            />
            <span className="subtitle">At least 8 characters</span>
          </div>
          <button className="btn btn-primary" type="submit" disabled={status === 'loading'}>
            {status === 'loading' ? 'Creating account…' : 'Create account'}
          </button>
        </form>

        <p className="auth-switch">
          Already have an account? <Link to="/login">Log in</Link>
        </p>
      </div>
    </div>
  )
}
