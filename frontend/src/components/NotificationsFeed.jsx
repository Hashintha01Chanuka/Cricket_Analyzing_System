import { useEffect } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { fetchNotificationsByMatch } from '../store/slices/notificationSlice'

export default function NotificationsFeed({ matchId }) {
  const dispatch = useDispatch()
  const { byMatch, status } = useSelector((state) => state.notifications)

  useEffect(() => {
    dispatch(fetchNotificationsByMatch(matchId))
  }, [dispatch, matchId])

  if (status === 'loading' && byMatch.length === 0) {
    return <div className="loading-state">Loading notifications…</div>
  }

  if (byMatch.length === 0) {
    return null
  }

  return (
    <div className="card">
      <h3>Milestones</h3>
      <ul className="notification-list">
        {byMatch.map((n) => (
          <li key={n.id} className={`notification-item notification-${n.type.toLowerCase()}`}>
            <span className="notification-type">{n.type.replace('_', ' ')}</span>
            <span>{n.message}</span>
          </li>
        ))}
      </ul>
    </div>
  )
}
