import { useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { fetchWinProbability } from '../store/slices/statsSlice'

export default function WinProbabilityCalculator() {
  const dispatch = useDispatch()
  const { winProbability, winProbabilityStatus } = useSelector((state) => state.stats)

  const [form, setForm] = useState({
    target: '',
    currentScore: '',
    wicketsLost: 0,
    oversRemaining: '',
    totalOvers: 20
  })

  function update(field) {
    return (e) => setForm((f) => ({ ...f, [field]: Number(e.target.value) }))
  }

  function handleSubmit(e) {
    e.preventDefault()
    dispatch(fetchWinProbability(form))
  }

  return (
    <div className="card">
      <h3>Win probability</h3>
      <p className="subtitle">Chasing team's estimated chance of winning, based on the current situation</p>

      <form onSubmit={handleSubmit}>
        <div className="form-row">
          <div className="field">
            <label htmlFor="target">Target</label>
            <input id="target" type="number" min={0} value={form.target} onChange={update('target')} required />
          </div>
          <div className="field">
            <label htmlFor="currentScore">Current score</label>
            <input
              id="currentScore"
              type="number"
              min={0}
              value={form.currentScore}
              onChange={update('currentScore')}
              required
            />
          </div>
        </div>
        <div className="form-row">
          <div className="field">
            <label htmlFor="wicketsLost">Wickets lost</label>
            <input
              id="wicketsLost"
              type="number"
              min={0}
              max={10}
              value={form.wicketsLost}
              onChange={update('wicketsLost')}
            />
          </div>
          <div className="field">
            <label htmlFor="oversRemaining">Overs remaining</label>
            <input
              id="oversRemaining"
              type="number"
              min={0}
              step="0.1"
              value={form.oversRemaining}
              onChange={update('oversRemaining')}
              required
            />
          </div>
        </div>

        <button className="btn btn-secondary" type="submit" disabled={winProbabilityStatus === 'loading'}>
          {winProbabilityStatus === 'loading' ? 'Calculating…' : 'Calculate'}
        </button>
      </form>

      {winProbability && (
        <div className="win-prob-result">
          <div className="win-prob-bar">
            <div
              className="win-prob-bar-fill"
              style={{ width: `${winProbability.battingTeamWinProbability * 100}%` }}
            />
          </div>
          <div className="win-prob-numbers mono">
            <span>{Math.round(winProbability.battingTeamWinProbability * 100)}% chance</span>
            <span>RRR {winProbability.requiredRunRate === Infinity ? '—' : winProbability.requiredRunRate}</span>
          </div>
          <p className="subtitle">{winProbability.note}</p>
        </div>
      )}
    </div>
  )
}
