import { useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { useNavigate } from 'react-router-dom'
import { createMatch, clearActionError } from '../store/slices/matchSlice'

const FORMATS = ['TEST', 'ODI', 'T20']

export default function CreateMatchPage() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const { actionStatus, actionError } = useSelector((state) => state.matches)

  const [form, setForm] = useState({
    team1: '',
    team2: '',
    venue: '',
    matchDate: '',
    format: 'ODI'
  })

  function update(field) {
    return (e) => {
      dispatch(clearActionError())
      setForm((f) => ({ ...f, [field]: e.target.value }))
    }
  }

  async function handleSubmit(e) {
    e.preventDefault()
    const payload = { ...form, matchDate: new Date(form.matchDate).toISOString() }
    const result = await dispatch(createMatch(payload))
    if (createMatch.fulfilled.match(result)) {
      navigate(`/matches/${result.payload.id}`)
    }
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>Schedule a match</h1>
      </div>

      <div className="card" style={{ maxWidth: 520 }}>
        {actionError && <div className="alert alert-error">{actionError}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="field">
              <label htmlFor="team1">Team 1</label>
              <input id="team1" value={form.team1} onChange={update('team1')} required />
            </div>
            <div className="field">
              <label htmlFor="team2">Team 2</label>
              <input id="team2" value={form.team2} onChange={update('team2')} required />
            </div>
          </div>

          <div className="field">
            <label htmlFor="venue">Venue</label>
            <input id="venue" value={form.venue} onChange={update('venue')} />
          </div>

          <div className="form-row">
            <div className="field">
              <label htmlFor="matchDate">Date &amp; time</label>
              <input
                id="matchDate"
                type="datetime-local"
                value={form.matchDate}
                onChange={update('matchDate')}
                required
              />
            </div>
            <div className="field">
              <label htmlFor="format">Format</label>
              <select id="format" value={form.format} onChange={update('format')}>
                {FORMATS.map((f) => (
                  <option key={f} value={f}>
                    {f}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <button className="btn btn-primary" type="submit" disabled={actionStatus === 'loading'}>
            {actionStatus === 'loading' ? 'Creating…' : 'Create match'}
          </button>
        </form>
      </div>
    </div>
  )
}
