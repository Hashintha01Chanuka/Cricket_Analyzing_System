import { apiClient } from './client'

export const authApi = {
  register: (payload) => apiClient.post('/api/v1/auth/register', payload).then((r) => r.data),
  login: (payload) => apiClient.post('/api/v1/auth/login', payload).then((r) => r.data),
  me: () => apiClient.get('/api/v1/users/me').then((r) => r.data)
}
