import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import { authApi } from '../../api/authApi'

const storedToken = localStorage.getItem('cricket_auth_token')
const storedUser = localStorage.getItem('cricket_auth_user')

const initialState = {
  token: storedToken || null,
  user: storedUser ? JSON.parse(storedUser) : null, // { userId, username, role }
  status: 'idle', // idle | loading | failed
  error: null
}

export const login = createAsyncThunk('auth/login', async (credentials, { rejectWithValue }) => {
  try {
    return await authApi.login(credentials)
  } catch (err) {
    return rejectWithValue(err.response?.data?.message || 'Login failed')
  }
})

export const register = createAsyncThunk('auth/register', async (payload, { rejectWithValue }) => {
  try {
    return await authApi.register(payload)
  } catch (err) {
    return rejectWithValue(err.response?.data?.message || 'Registration failed')
  }
})

function persistSession(state, response) {
  state.token = response.token
  state.user = { userId: response.userId, username: response.username, role: response.role }
  localStorage.setItem('cricket_auth_token', response.token)
  localStorage.setItem('cricket_auth_user', JSON.stringify(state.user))
}

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    logout(state) {
      state.token = null
      state.user = null
      localStorage.removeItem('cricket_auth_token')
      localStorage.removeItem('cricket_auth_user')
    },
    clearAuthError(state) {
      state.error = null
    }
  },
  extraReducers: (builder) => {
    builder
      .addCase(login.pending, (state) => {
        state.status = 'loading'
        state.error = null
      })
      .addCase(login.fulfilled, (state, action) => {
        state.status = 'idle'
        persistSession(state, action.payload)
      })
      .addCase(login.rejected, (state, action) => {
        state.status = 'failed'
        state.error = action.payload
      })
      .addCase(register.pending, (state) => {
        state.status = 'loading'
        state.error = null
      })
      .addCase(register.fulfilled, (state, action) => {
        state.status = 'idle'
        persistSession(state, action.payload)
      })
      .addCase(register.rejected, (state, action) => {
        state.status = 'failed'
        state.error = action.payload
      })
  }
})

export const { logout, clearAuthError } = authSlice.actions
export default authSlice.reducer

// Selectors
export const selectIsAuthenticated = (state) => Boolean(state.auth.token)
export const selectCurrentUser = (state) => state.auth.user
export const selectHasRole = (state, ...roles) => roles.includes(state.auth.user?.role)
