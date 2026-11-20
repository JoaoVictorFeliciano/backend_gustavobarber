
import { Link } from 'react-router-dom'
import './Servicos.css'

function Servicos() {
    const servicos = [
        {
            nome: 'Corte + Sobrancelha',
            descricao: 'Um corte para renovar seu visual.'
        },
        {
            nome: 'Barba',
            descricao: 'Barba e sobrancelha alinhados para um visual completo.'
        },
        {
            nome: 'Completo',
            descricao: 'Corte, barba e sobrancelha em um só atendimento.'
        }
    ]

    return (
        <main className="servicos">
            <section className="servicos-header">
                <p>NOSSOS SERVIÇOS</p>
                <h1>ESCOLHA SEU CORTE</h1>
                <span>Confira nossos serviços e agende seu horário.</span>
            </section>

            <section className="servicos-lista">
                {servicos.map((servico) => (
                    <article className="servico-card" key={servico.nome}>
                        <h2>{servico.nome}</h2>
                        <p>{servico.descricao}</p>

                        <Link to="/agendamento">
                            AGENDAR
                        </Link>
                    </article>
                ))}
            </section>
        </main>
    )
}

export default Servicos
