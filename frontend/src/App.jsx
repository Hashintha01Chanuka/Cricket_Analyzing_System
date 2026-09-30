import { Routes, Route, Navigate } from 'react-router-dom'
import Navbar from './components/Navbar'
import ProtectedRoute from './components/ProtectedRoute'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import MatchesPage from './pages/MatchesPage'
import MatchDetailPage from './pages/MatchDetailPage'
import CreateMatchPage from './pages/CreateMatchPage'
import PlayersPage from './pages/PlayersPage'
import PlayerDetailPage from './pages/PlayerDetailPage'
import CreatePlayerPage from './pages/CreatePlayerPage'
import LeaderboardPage from './pages/LeaderboardPage'

export default function App() {
  return (
    <>
      <Navbar />
      <Routes>
        <Route path="/" element={<Navigate to="/matches" replace />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        <Route path="/matches" element={<MatchesPage />} />
        <Route
          path="/matches/new"
          element={
            <ProtectedRoute>
              <CreateMatchPage />
            </ProtectedRoute>
          }
        />
        <Route path="/matches/:matchId" element={<MatchDetailPage />} />

        <Route path="/players" element={<PlayersPage />} />
        <Route
          path="/players/new"
          element={
            <ProtectedRoute>
              <CreatePlayerPage />
            </ProtectedRoute>
          }
        />
        <Route path="/players/:playerId" element={<PlayerDetailPage />} />

        <Route path="/leaderboard" element={<LeaderboardPage />} />

        <Route path="*" element={<Navigate to="/matches" replace />} />
      </Routes>
    </>
  )
}
