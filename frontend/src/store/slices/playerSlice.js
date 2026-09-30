import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import { playerApi } from '../../api/playerApi'

export const fetchPlayers = createAsyncThunk('players/fetchAll', async () => playerApi.list())

export const fetchPlayer = createAsyncThunk('players/fetchOne', async (playerId) => {
  const [profile, stats] = await Promise.all([
    playerApi.get(playerId),
    playerApi.stats(playerId)
  ])
  return { profile, stats }
})

export const createPlayer = createAsyncThunk(
  'players/create',
  async (payload, { rejectWithValue }) => {
    try {
      return await playerApi.create(payload)
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Could not create player')
    }
  }
)

export const fetchLeaderboards = createAsyncThunk('players/fetchLeaderboards', async () => {
  const [runs, wickets] = await Promise.all([
    playerApi.leaderboardRuns(10),
    playerApi.leaderboardWickets(10)
  ])
  return { runs, wickets }
})

const initialState = {
  list: [],
  listStatus: 'idle',
  current: null,
  currentStats: null,
  currentStatus: 'idle',
  leaderboardRuns: [],
  leaderboardWickets: [],
  leaderboardStatus: 'idle',
  actionError: null,
  actionStatus: 'idle'
}

const playerSlice = createSlice({
  name: 'players',
  initialState,
  reducers: {
    clearActionError(state) {
      state.actionError = null
    }
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchPlayers.pending, (state) => {
        state.listStatus = 'loading'
      })
      .addCase(fetchPlayers.fulfilled, (state, action) => {
        state.listStatus = 'idle'
        state.list = action.payload
      })
      .addCase(fetchPlayers.rejected, (state) => {
        state.listStatus = 'failed'
      })
      .addCase(fetchPlayer.pending, (state) => {
        state.currentStatus = 'loading'
      })
      .addCase(fetchPlayer.fulfilled, (state, action) => {
        state.currentStatus = 'idle'
        state.current = action.payload.profile
        state.currentStats = action.payload.stats
      })
      .addCase(fetchPlayer.rejected, (state) => {
        state.currentStatus = 'failed'
      })
      .addCase(createPlayer.pending, (state) => {
        state.actionStatus = 'loading'
        state.actionError = null
      })
      .addCase(createPlayer.fulfilled, (state, action) => {
        state.actionStatus = 'idle'
        state.list.unshift(action.payload)
      })
      .addCase(createPlayer.rejected, (state, action) => {
        state.actionStatus = 'failed'
        state.actionError = action.payload
      })
      .addCase(fetchLeaderboards.pending, (state) => {
        state.leaderboardStatus = 'loading'
      })
      .addCase(fetchLeaderboards.fulfilled, (state, action) => {
        state.leaderboardStatus = 'idle'
        state.leaderboardRuns = action.payload.runs
        state.leaderboardWickets = action.payload.wickets
      })
      .addCase(fetchLeaderboards.rejected, (state) => {
        state.leaderboardStatus = 'failed'
      })
  }
})

export const { clearActionError } = playerSlice.actions
export default playerSlice.reducer
