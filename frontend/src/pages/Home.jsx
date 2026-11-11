
import { Link } from 'react-router-dom'
import barbearia from '../assets/barbearia.jpg'
import './Home.css'

function Home() {
    return (
        <main className="home">
            <section className="hero">
                <div className="hero-content">
                    <p className="hero-subtitle">BARBEARIA EM SUMÉ - PB</p>

                    <h1>
                        SEU ESTILO.
                        <span>SUA PERSONALIDADE.</span>
                    </h1>

                    <p className="hero-description">
                        Mais que um corte, uma experiência.
                        Venha conhecer a Gustavo Barber.
                    </p>

                    <Link to="/agendamento" className="hero-button">
                        AGENDAR MEU HORÁRIO
                    </Link>
                </div>

                <div className="hero-image">
                    <img src={barbearia} alt="Interior da Gustavo Barber" />
                </div>
            </section>

            <section className="barber-info">
                <p>Atendimentos de segunda a sábado</p>
                <p>Rua José Cavalcante de Queiroz</p>
                <p>Sumé - PB</p>
            </section>
        </main>
    )
}

export default Home
