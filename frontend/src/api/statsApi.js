import { apiClient } from './client'

export const statsApi = {
  headToHead: (team1, team2) =>
    apiClient.get('/api/v1/stats/head-to-head', { params: { team1, team2 } }).then((r) => r.data),
  winProbability: (payload) =>
    apiClient.post('/api/v1/stats/win-probability', payload).then((r) => r.data),
  liveState: (matchId, innings = 1) =>
    apiClient
      .get(`/api/v1/stats/matches/${matchId}/live`, { params: { innings } })
      .then((r) => r.data),
  recordResult: (payload) => apiClient.post('/api/v1/stats/results', payload).then((r) => r.data)
}
