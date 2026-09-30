import { useSelector } from 'react-redux'
import { selectCurrentUser } from '../store/slices/authSlice'

export default function RoleGuard({ allow, children }) {
  const user = useSelector(selectCurrentUser)
  if (!user || !allow.includes(user.role)) return null
  return children
}
