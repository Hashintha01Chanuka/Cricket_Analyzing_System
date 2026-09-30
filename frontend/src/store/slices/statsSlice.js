import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import { statsApi } from '../../api/statsApi'

export const fetchLiveState = createAsyncThunk(
  'stats/fetchLiveState',
  async ({ matchId, innings }, { rejectWithValue }) => {
    try {
      return await statsApi.liveState(matchId, innings)
    } catch (err) {
      // 404 here just means no balls consumed yet for this innings — not a real error, so treat it as "no live state" rather than surfacing an alert to the user.
      if (err.response?.status === 404) return null
      return rejectWithValue(err.response?.data?.message || 'Could not load live state')
    }
  }
)

export const fetchWinProbability = createAsyncThunk(
  'stats/fetchWinProbability',
  async (payload, { rejectWithValue }) => {
    try {
      return await statsApi.winProbability(payload)
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Could not calculate win probability')
    }
  }
)

export const fetchHeadToHead = createAsyncThunk(
  'stats/fetchHeadToHead',
  async ({ team1, team2 }) => statsApi.headToHead(team1, team2)
)

const initialState = {
  liveState: null,
  liveStateStatus: 'idle',
  winProbability: null,
  winProbabilityStatus: 'idle',
  headToHead: null
}

const statsSlice = createSlice({
  name: 'stats',
  initialState,
  reducers: {
    clearWinProbability(state) {
      state.winProbability = null
    }
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchLiveState.pending, (state) => {
        state.liveStateStatus = 'loading'
      })
      .addCase(fetchLiveState.fulfilled, (state, action) => {
        state.liveStateStatus = 'idle'
        state.liveState = action.payload
      })
      .addCase(fetchLiveState.rejected, (state) => {
        state.liveStateStatus = 'failed'
      })
      .addCase(fetchWinProbability.pending, (state) => {
        state.winProbabilityStatus = 'loading'
      })
      .addCase(fetchWinProbability.fulfilled, (state, action) => {
        state.winProbabilityStatus = 'idle'
        state.winProbability = action.payload
      })
      .addCase(fetchWinProbability.rejected, (state) => {
        state.winProbabilityStatus = 'failed'
      })
      .addCase(fetchHeadToHead.fulfilled, (state, action) => {
        state.headToHead = action.payload
      })
  }
})

export const { clearWinProbability } = statsSlice.actions
export default statsSlice.reducer
