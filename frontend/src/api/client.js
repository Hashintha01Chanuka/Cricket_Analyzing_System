import axios from "axios"

const GATEWAY_URL = import.meta.env.VITE_GATEWAY_URL || "http://localhost:8080"

export const apiClient = axios.create({
  baseURL: GATEWAY_URL,
  headers: { "Content-Type": "application/json" },
})

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("cricket_auth_token")
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})


let unauthorizedHandler = null

export function setUnauthorizedHandler(handler) {
  unauthorizedHandler = handler
}

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("cricket_auth_token")
      localStorage.removeItem("cricket_auth_user")
      unauthorizedHandler?.()
    }
    return Promise.reject(error)
  },
)
