import './Scoreboard.css'

export default function Scoreboard({ match, liveState, runRate }) {
  const isLive = match.status === 'LIVE'
  const runs = liveState?.totalRuns ?? 0
  const wickets = liveState?.wicketsLost ?? 0
  const overs = liveState?.oversBowled?.toFixed(1) ?? '0.0'
  const currentRunRate = liveState?.currentRunRate ?? runRate?.runRate ?? 0

  return (
    <div className="scoreboard">
      <div className="scoreboard-top">
        <span className={`badge badge-${match.status.toLowerCase()}`}>{match.status}</span>
        <span className="scoreboard-format">{match.format}</span>
      </div>

      <div className="scoreboard-teams">
        {match.team1} <span className="scoreboard-vs">vs</span> {match.team2}
      </div>

      <div className="scoreboard-main mono">
        <div className="scoreboard-score">
          {runs}
          <span className="scoreboard-wickets">/{wickets}</span>
        </div>
        <div className="scoreboard-overs">
          <span className="scoreboard-overs-value">{overs}</span>
          <span className="scoreboard-overs-label">overs</span>
        </div>
      </div>

      <div className="scoreboard-footer mono">
        <div className="scoreboard-stat">
          <span className="scoreboard-stat-label">Run rate</span>
          <span className="scoreboard-stat-value">{currentRunRate}</span>
        </div>
        {match.venue && (
          <div className="scoreboard-stat">
            <span className="scoreboard-stat-label">Venue</span>
            <span className="scoreboard-stat-value scoreboard-venue">{match.venue}</span>
          </div>
        )}
      </div>

      {!isLive && !liveState && (
        <div className="scoreboard-waiting">
          {match.status === 'SCHEDULED' ? 'Match not yet started' : 'No live data for this innings'}
        </div>
      )}
    </div>
  )
}
