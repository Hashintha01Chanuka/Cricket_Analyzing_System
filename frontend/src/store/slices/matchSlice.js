import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import { matchApi } from '../../api/matchApi'

export const fetchMatches = createAsyncThunk('matches/fetchAll', async () => matchApi.list())

export const fetchMatch = createAsyncThunk('matches/fetchOne', async (matchId) =>
  matchApi.get(matchId)
)

export const createMatch = createAsyncThunk(
  'matches/create',
  async (payload, { rejectWithValue }) => {
    try {
      return await matchApi.create(payload)
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Could not create match')
    }
  }
)

export const addBall = createAsyncThunk(
  'matches/addBall',
  async ({ matchId, payload }, { rejectWithValue }) => {
    try {
      return await matchApi.addBall(matchId, payload)
    } catch (err) {
      return rejectWithValue(err.response?.data?.message || 'Could not record ball')
    }
  }
)

export const fetchRunRate = createAsyncThunk(
  'matches/fetchRunRate',
  async ({ matchId, innings, overs }) => matchApi.runRate(matchId, innings, overs)
)

const initialState = {
  list: [],
  listStatus: 'idle',
  current: null,
  currentStatus: 'idle',
  runRate: null,
  actionError: null,
  actionStatus: 'idle'
}

const matchSlice = createSlice({
  name: 'matches',
  initialState,
  reducers: {
    clearActionError(state) {
      state.actionError = null
    }
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchMatches.pending, (state) => {
        state.listStatus = 'loading'
      })
      .addCase(fetchMatches.fulfilled, (state, action) => {
        state.listStatus = 'idle'
        state.list = action.payload
      })
      .addCase(fetchMatches.rejected, (state) => {
        state.listStatus = 'failed'
      })
      .addCase(fetchMatch.pending, (state) => {
        state.currentStatus = 'loading'
      })
      .addCase(fetchMatch.fulfilled, (state, action) => {
        state.currentStatus = 'idle'
        state.current = action.payload
      })
      .addCase(fetchMatch.rejected, (state) => {
        state.currentStatus = 'failed'
      })
      .addCase(createMatch.pending, (state) => {
        state.actionStatus = 'loading'
        state.actionError = null
      })
      .addCase(createMatch.fulfilled, (state, action) => {
        state.actionStatus = 'idle'
        state.list.unshift(action.payload)
      })
      .addCase(createMatch.rejected, (state, action) => {
        state.actionStatus = 'failed'
        state.actionError = action.payload
      })
      .addCase(addBall.pending, (state) => {
        state.actionStatus = 'loading'
        state.actionError = null
      })
      .addCase(addBall.fulfilled, (state, action) => {
        state.actionStatus = 'idle'
        state.current = action.payload
      })
      .addCase(addBall.rejected, (state, action) => {
        state.actionStatus = 'failed'
        state.actionError = action.payload
      })
      .addCase(fetchRunRate.fulfilled, (state, action) => {
        state.runRate = action.payload
      })
  }
})

export const { clearActionError } = matchSlice.actions
export default matchSlice.reducer
