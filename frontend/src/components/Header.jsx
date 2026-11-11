
import { useState } from 'react'
import { Link } from 'react-router-dom'
import './Header.css'

function Header() {
    const [menuAberto, setMenuAberto] = useState(false)

    function fecharMenu() {
        setMenuAberto(false)
    }

    return (
        <header className="header">
            <Link to="/" className="logo" onClick={fecharMenu}>
                GUSTAVO<span>BARBER</span>
            </Link>

            <nav className={`navigation ${menuAberto ? 'aberta' : ''}`}>
                <Link to="/" onClick={fecharMenu}>Início</Link>
                <Link to="/servicos" onClick={fecharMenu}>Serviços</Link>
                <Link to="/agendamento" onClick={fecharMenu}>Agendamento</Link>
            </nav>

            <Link
                to="/agendamento"
                className="header-button"
            >
                Agendar horário
            </Link>

            <button
                className="menu-toggle"
                type="button"
                onClick={() => setMenuAberto(!menuAberto)}
                aria-label={menuAberto ? 'Fechar menu' : 'Abrir menu'}
                aria-expanded={menuAberto}
            >
                {menuAberto ? '✕' : '☰'}
            </button>
        </header>
    )
}

export default Header
