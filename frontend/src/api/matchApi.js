import { apiClient } from './client'

export const matchApi = {
  list: () => apiClient.get('/api/v1/matches').then((r) => r.data),
  get: (matchId) => apiClient.get(`/api/v1/matches/${matchId}`).then((r) => r.data),
  create: (payload) => apiClient.post('/api/v1/matches', payload).then((r) => r.data),
  addBall: (matchId, payload) =>
    apiClient.post(`/api/v1/matches/${matchId}/balls`, payload).then((r) => r.data),
  runRate: (matchId, innings = 1, overs = 6) =>
    apiClient
      .get(`/api/v1/matches/${matchId}/run-rate`, { params: { innings, overs } })
      .then((r) => r.data)
}
