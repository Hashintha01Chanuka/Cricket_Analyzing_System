import { apiClient } from './client'

export const notificationApi = {
  byMatch: (matchId) =>
    apiClient.get(`/api/v1/notifications/match/${matchId}`).then((r) => r.data),
  byPlayer: (playerId) =>
    apiClient.get(`/api/v1/notifications/player/${playerId}`).then((r) => r.data),
  markRead: (notificationId) =>
    apiClient.patch(`/api/v1/notifications/${notificationId}/read`).then((r) => r.data)
}
