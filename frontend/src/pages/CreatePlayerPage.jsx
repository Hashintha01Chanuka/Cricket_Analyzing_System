import { useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { useNavigate } from 'react-router-dom'
import { createPlayer, clearActionError } from '../store/slices/playerSlice'

const ROLES = ['BATSMAN', 'BOWLER', 'ALL_ROUNDER', 'WICKET_KEEPER']

export default function CreatePlayerPage() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { actionStatus, actionError } = useSelector((state) => state.players)

  const [form, setForm] = useState({
    name: '',
    country: '',
    role: 'BATSMAN',
    battingStyle: '',
    bowlingStyle: '',
    dateOfBirth: ''
  })

  function update(field) {
    return (e) => {
      dispatch(clearActionError())
      setForm((f) => ({ ...f, [field]: e.target.value }))
    }
  }

  async function handleSubmit(e) {
    e.preventDefault()
    const payload = { ...form, dateOfBirth: form.dateOfBirth || null }
    const result = await dispatch(createPlayer(payload))
    if (createPlayer.fulfilled.match(result)) {
      navigate(`/players/${result.payload.id}`)
    }
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>Register a player</h1>
      </div>

      <div className="card" style={{ maxWidth: 520 }}>
        {actionError && <div className="alert alert-error">{actionError}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="field">
              <label htmlFor="name">Name</label>
              <input id="name" value={form.name} onChange={update('name')} required />
            </div>
            <div className="field">
              <label htmlFor="country">Country</label>
              <input id="country" value={form.country} onChange={update('country')} required />
            </div>
          </div>

          <div className="field">
            <label htmlFor="role">Role</label>
            <select id="role" value={form.role} onChange={update('role')}>
              {ROLES.map((r) => (
                <option key={r} value={r}>
                  {r.replace('_', ' ')}
                </option>
              ))}
            </select>
          </div>

          <div className="form-row">
            <div className="field">
              <label htmlFor="battingStyle">Batting style</label>
              <input
                id="battingStyle"
                value={form.battingStyle}
                onChange={update('battingStyle')}
                placeholder="Right-hand bat"
              />
            </div>
            <div className="field">
              <label htmlFor="bowlingStyle">Bowling style</label>
              <input
                id="bowlingStyle"
                value={form.bowlingStyle}
                onChange={update('bowlingStyle')}
                placeholder="Right-arm fast"
              />
            </div>
          </div>

          <div className="field">
            <label htmlFor="dateOfBirth">Date of birth</label>
            <input
              id="dateOfBirth"
              type="date"
              value={form.dateOfBirth}
              onChange={update('dateOfBirth')}
            />
          </div>

          <button className="btn btn-primary" type="submit" disabled={actionStatus === 'loading'}>
            {actionStatus === 'loading' ? 'Registering…' : 'Register player'}
          </button>
        </form>
      </div>
    </div>
  )
}
