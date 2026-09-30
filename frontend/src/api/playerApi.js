import { apiClient } from './client'

export const playerApi = {
  list: () => apiClient.get('/api/v1/players').then((r) => r.data),
  get: (playerId) => apiClient.get(`/api/v1/players/${playerId}`).then((r) => r.data),
  create: (payload) => apiClient.post('/api/v1/players', payload).then((r) => r.data),
  stats: (playerId) => apiClient.get(`/api/v1/players/${playerId}/stats`).then((r) => r.data),
  updateStats: (playerId, payload) =>
    apiClient.patch(`/api/v1/players/${playerId}/stats`, payload).then((r) => r.data),
  leaderboardRuns: (limit = 10) =>
    apiClient.get('/api/v1/players/leaderboard/runs', { params: { limit } }).then((r) => r.data),
  leaderboardWickets: (limit = 10) =>
    apiClient.get('/api/v1/players/leaderboard/wickets', { params: { limit } }).then((r) => r.data)
}
