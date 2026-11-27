import { BrowserRouter, Routes, Route, Outlet } from 'react-router-dom'


import Header from './components/Header'
import Home from './pages/Home'
import Servicos from './pages/Servicos'
import Agendamento from './pages/Agendamento'
import Admin from './pages/Admin'

import './App.css'

function LayoutPublico() {
    return (
        <>
            <Header />
            <Outlet />
        </>
    )
}

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route element={<LayoutPublico />}>
                    <Route path="/" element={<Home />} />
                    <Route path="/servicos" element={<Servicos />} />
                    <Route path="/agendamento" element={<Agendamento />} />
                </Route>

                <Route path="/admin" element={<Admin />} />
            </Routes>
        </BrowserRouter>
    )
}

export default App