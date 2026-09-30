import { configureStore } from '@reduxjs/toolkit'
import authReducer from './slices/authSlice'
import matchReducer from './slices/matchSlice'
import playerReducer from './slices/playerSlice'
import statsReducer from './slices/statsSlice'
import notificationReducer from './slices/notificationSlice'

export const store = configureStore({
  reducer: {
    auth: authReducer,
    matches: matchReducer,
    players: playerReducer,
    stats: statsReducer,
    notifications: notificationReducer
  }
})
