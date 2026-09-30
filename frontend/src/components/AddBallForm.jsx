import { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { addBall, clearActionError } from '../store/slices/matchSlice'
import { fetchPlayers } from '../store/slices/playerSlice'

const emptyForm = {
  inningsNumber: 1,
  overNumber: 0,
  ballNumber: 1,
  bowlerId: '',
  bowlerName: '',
  batsmanId: '',
  batsmanName: '',
  runs: 0,
  wicket: false,
  wide: false,
  noBall: false,
  extras: 0
}

export default function AddBallForm({ matchId, onBallAdded }) {
  const dispatch = useDispatch()
  const { list: players } = useSelector((state) => state.players)
  const { actionStatus, actionError } = useSelector((state) => state.matches)
  const [form, setForm] = useState(emptyForm)

  useEffect(() => {
    if (players.length === 0) dispatch(fetchPlayers())
  }, [dispatch, players.length])

  function update(field) {
    return (e) => {
      dispatch(clearActionError())
      const value =
        e.target.type === 'checkbox'
          ? e.target.checked
          : e.target.type === 'number'
            ? Number(e.target.value)
            : e.target.value
      setForm((f) => ({ ...f, [field]: value }))
    }
  }

  function selectPlayer(field, nameField) {
    return (e) => {
      const player = players.find((p) => p.id === e.target.value)
      setForm((f) => ({ ...f, [field]: e.target.value, [nameField]: player?.name || '' }))
    }
  }

  async function handleSubmit(e) {
    e.preventDefault()
    const result = await dispatch(addBall({ matchId, payload: form }))
    if (addBall.fulfilled.match(result)) {
      setForm((f) => ({
        ...f,
        ballNumber: f.ballNumber + 1,
        runs: 0,
        wicket: false,
        wide: false,
        noBall: false,
        extras: 0
      }))
      onBallAdded?.()
    }
  }

  return (
    <div className="card">
      <h3>Record a ball</h3>
      {actionError && <div className="alert alert-error">{actionError}</div>}

      <form onSubmit={handleSubmit}>
        <div className="form-row">
          <div className="field">
            <label htmlFor="inningsNumber">Innings</label>
            <input
              id="inningsNumber"
              type="number"
              min={1}
              value={form.inningsNumber}
              onChange={update('inningsNumber')}
            />
          </div>
          <div className="field">
            <label htmlFor="overNumber">Over</label>
            <input
              id="overNumber"
              type="number"
              min={0}
              value={form.overNumber}
              onChange={update('overNumber')}
            />
          </div>
          <div className="field">
            <label htmlFor="ballNumber">Ball</label>
            <input
              id="ballNumber"
              type="number"
              min={1}
              value={form.ballNumber}
              onChange={update('ballNumber')}
            />
          </div>
        </div>

        <div className="form-row">
          <div className="field">
            <label htmlFor="batsmanId">Batter</label>
            <select id="batsmanId" value={form.batsmanId} onChange={selectPlayer('batsmanId', 'batsmanName')} required>
              <option value="">Select batter…</option>
              {players.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name}
                </option>
              ))}
            </select>
          </div>
          <div className="field">
            <label htmlFor="bowlerId">Bowler</label>
            <select id="bowlerId" value={form.bowlerId} onChange={selectPlayer('bowlerId', 'bowlerName')} required>
              <option value="">Select bowler…</option>
              {players.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="form-row">
          <div className="field">
            <label htmlFor="runs">Runs off bat</label>
            <input id="runs" type="number" min={0} max={6} value={form.runs} onChange={update('runs')} />
          </div>
          <div className="field">
            <label htmlFor="extras">Extras</label>
            <input id="extras" type="number" min={0} value={form.extras} onChange={update('extras')} />
          </div>
        </div>

        <div className="ball-checkboxes">
          <label>
            <input type="checkbox" checked={form.wicket} onChange={update('wicket')} /> Wicket
          </label>
          <label>
            <input type="checkbox" checked={form.wide} onChange={update('wide')} /> Wide
          </label>
          <label>
            <input type="checkbox" checked={form.noBall} onChange={update('noBall')} /> No ball
          </label>
        </div>

        <button className="btn btn-primary" type="submit" disabled={actionStatus === 'loading'}>
          {actionStatus === 'loading' ? 'Recording…' : 'Record ball'}
        </button>
      </form>
    </div>
  )
}
