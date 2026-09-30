import React from 'react'
import ReactDOM from 'react-dom/client'
import { Provider } from 'react-redux'
import { BrowserRouter } from 'react-router-dom'
import App from './App.jsx'
import { store } from './store/store.js'
import { logout } from './store/slices/authSlice.js'
import { setUnauthorizedHandler } from './api/client.js'
import './styles/tokens.css'
import './styles/components.css'
import './components/Navbar.css'
import './styles/pages.css'

setUnauthorizedHandler(() => store.dispatch(logout()))

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <Provider store={store}>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </Provider>
  </React.StrictMode>
)
