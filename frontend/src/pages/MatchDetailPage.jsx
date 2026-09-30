import { useCallback, useEffect } from 'react'
import { useParams } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { fetchMatch } from '../store/slices/matchSlice'
import { fetchLiveState } from '../store/slices/statsSlice'
import Scoreboard from '../components/Scoreboard'
import AddBallForm from '../components/AddBallForm'
import WinProbabilityCalculator from '../components/WinProbabilityCalculator'
import NotificationsFeed from '../components/NotificationsFeed'
import RoleGuard from '../components/RoleGuard'

const LIVE_POLL_INTERVAL_MS = 5000

export default function MatchDetailPage() {
  const { matchId } = useParams()
  const dispatch = useDispatch()
  const { current: match, currentStatus } = useSelector((state) => state.matches)
  const { liveState } = useSelector((state) => state.stats)

  const refresh = useCallback(() => {
    dispatch(fetchMatch(matchId))
    dispatch(fetchLiveState({ matchId, innings: 1 }))
  }, [dispatch, matchId])

  useEffect(() => {
    refresh()
  }, [refresh])

  useEffect(() => {
    if (match?.status !== 'LIVE') return undefined
    const id = setInterval(() => {
      dispatch(fetchLiveState({ matchId, innings: 1 }))
    }, LIVE_POLL_INTERVAL_MS)
    return () => clearInterval(id)
  }, [dispatch, matchId, match?.status])

  if (currentStatus === 'loading' && !match) {
    return (
      <div className="page">
        <div className="loading-state">Loading match…</div>
      </div>
    )
  }

  if (!match) {
    return (
      <div className="page">
        <div className="empty-state">Match not found.</div>
      </div>
    )
  }

  return (
    <div className="page">
      <Scoreboard match={match} liveState={liveState} />

      <div className="grid grid-2 match-detail-grid">
        <div className="match-detail-column">
          <RoleGuard allow={['ADMIN', 'SCORER']}>
            <AddBallForm matchId={matchId} onBallAdded={refresh} />
          </RoleGuard>
          <NotificationsFeed matchId={matchId} />
        </div>
        <div className="match-detail-column">
          <WinProbabilityCalculator />
        </div>
      </div>
    </div>
  )
}
