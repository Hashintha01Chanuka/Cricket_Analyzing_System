import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import { notificationApi } from '../../api/notificationApi'

export const fetchNotificationsByMatch = createAsyncThunk(
  'notifications/fetchByMatch',
  async (matchId) => notificationApi.byMatch(matchId)
)

export const markNotificationRead = createAsyncThunk(
  'notifications/markRead',
  async (notificationId) => notificationApi.markRead(notificationId)
)

const initialState = {
  byMatch: [],
  status: 'idle'
}

const notificationSlice = createSlice({
  name: 'notifications',
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchNotificationsByMatch.pending, (state) => {
        state.status = 'loading'
      })
      .addCase(fetchNotificationsByMatch.fulfilled, (state, action) => {
        state.status = 'idle'
        state.byMatch = action.payload
      })
      .addCase(fetchNotificationsByMatch.rejected, (state) => {
        state.status = 'failed'
      })
      .addCase(markNotificationRead.fulfilled, (state, action) => {
        const idx = state.byMatch.findIndex((n) => n.id === action.payload.id)
        if (idx !== -1) state.byMatch[idx] = action.payload
      })
  }
})

export default notificationSlice.reducer
