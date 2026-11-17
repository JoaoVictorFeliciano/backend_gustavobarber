
import { useState, useEffect, useRef } from 'react'
import api from '../services/api'
import './Admin.css'

function Admin() {

    const [email, setEmail] = useState('')
    const [senha, setSenha] = useState('')
    const [erro, setErro] = useState('')
    const [carregando, setCarregando] = useState(false)
    const [autenticado, setAutenticado] = useState(false)
    const [verificando, setVerificando] = useState(true)

    const [agendamentos, setAgendamentos] = useState([])
    const [carregandoAgendamentos, setCarregandoAgendamentos] = useState(false)

    // Filtros
    const [filtroStatus, setFiltroStatus] = useState('TODOS')
    const [pesquisa, setPesquisa] = useState('')
    const [filtroPeriodo, setFiltroPeriodo] = useState('TODOS')
    const [dataInicio, setDataInicio] = useState('')
    const [dataFim, setDataFim] = useState('')

    // Paginação
    const [paginaAtual, setPaginaAtual] = useState(0)
    const [totalPaginas, setTotalPaginas] = useState(0)
    const [totalAgendamentos, setTotalAgendamentos] = useState(0)

    // Indicadores gerais
    const [resumo, setResumo] = useState({
        pendentes: 0,
        concluidos: 0,
        cancelados: 0
    })

    const buscarAgendamentosRef = useRef(buscarAgendamentos)

    // Formata uma data no padrão YYYY-MM-DD
    function formatarData(data) {
        const ano = data.getFullYear()
        const mes = String(data.getMonth() + 1).padStart(2, '0')
        const dia = String(data.getDate()).padStart(2, '0')

        return `${ano}-${mes}-${dia}`
    }

    // Calcula as datas que serão enviadas ao backend
    function obterPeriodo(periodo, inicio, fim) {

        if (periodo === 'HOJE') {
            const hoje = formatarData(new Date())

            return {
                dataInicio: hoje,
                dataFim: hoje
            }
        }

        if (periodo === 'SEMANA') {
            const inicioSemana = new Date()

            inicioSemana.setDate(
                inicioSemana.getDate() - ((inicioSemana.getDay() + 6) % 7)
            )

            const fimSemana = new Date(inicioSemana)
            fimSemana.setDate(fimSemana.getDate() + 6)

            return {
                dataInicio: formatarData(inicioSemana),
                dataFim: formatarData(fimSemana)
            }
        }

        if (periodo === 'PERSONALIZADO') {
            return {
                dataInicio: inicio || undefined,
                dataFim: fim || undefined
            }
        }

        return {}
    }

    // Busca os agendamentos no backend
    async function buscarAgendamentos(sobrescritas = {}) {

        const periodoFiltro = sobrescritas.filtroPeriodo ?? filtroPeriodo
        const inicioFiltro = sobrescritas.dataInicio ?? dataInicio
        const fimFiltro = sobrescritas.dataFim ?? dataFim
        const statusFiltro = sobrescritas.filtroStatus ?? filtroStatus
        const pesquisaFiltro = sobrescritas.pesquisa ?? pesquisa
        const paginaFiltro = sobrescritas.paginaAtual ?? paginaAtual

        setCarregandoAgendamentos(true)
        setErro('')

        try {

            const periodo = obterPeriodo(
                periodoFiltro,
                inicioFiltro,
                fimFiltro
            )

            const resposta = await api.get('/api/agendamentos', {
                params: {
                    pagina: paginaFiltro,
                    tamanho: 10,
                    pesquisa: pesquisaFiltro.trim() || undefined,
                    status: statusFiltro !== 'TODOS' ? statusFiltro : undefined,
                    ...periodo
                }
            })

            setAgendamentos(resposta.data.content)
            setTotalPaginas(resposta.data.totalPages)
            setTotalAgendamentos(resposta.data.totalElements)

            // Busca os totais gerais de cada status
            const [pendentes, concluidos, cancelados] = await Promise.all([
                api.get('/api/agendamentos', {
                    params: { pagina: 0, tamanho: 1, status: 'PENDENTE' }
                }),
                api.get('/api/agendamentos', {
                    params: { pagina: 0, tamanho: 1, status: 'CONCLUIDO' }
                }),
                api.get('/api/agendamentos', {
                    params: { pagina: 0, tamanho: 1, status: 'CANCELADO' }
                })
            ])

            setResumo({
                pendentes: pendentes.data.totalElements,
                concluidos: concluidos.data.totalElements,
                cancelados: cancelados.data.totalElements
            })

        } catch (error) {

            if (error.response?.status === 401) {
                sessionStorage.removeItem('tokenAdmin')
                setAutenticado(false)
                setErro('Sua sessão expirou. Faça login novamente.')
            } else if (error.response?.status === 403) {
                setErro('Você não tem permissão para acessar os agendamentos.')
            } else {
                setErro('Não foi possível carregar os agendamentos.')
            }

        } finally {
            setCarregandoAgendamentos(false)
        }
    }

    // Verifica a autenticação ao abrir a página
    useEffect(() => {

        async function verificarAutenticacao() {

            const token = sessionStorage.getItem('tokenAdmin')

            if (!token) {
                setVerificando(false)
                return
            }

            try {
                await api.get('/api/agendamentos')
                setAutenticado(true)
                await buscarAgendamentosRef.current()

            } catch (error) {
                if (error.response?.status === 401) {
                    sessionStorage.removeItem('tokenAdmin')
                    setAutenticado(false)
                } else {
                    setAutenticado(true)
                }

            } finally {
                setVerificando(false)
            }
        }

        verificarAutenticacao()

    }, [])

    // Realiza login
    async function realizarLogin(event) {

        event.preventDefault()
        setErro('')
        setCarregando(true)

        try {
            const resposta = await api.post('/api/admin/login', {
                email,
                senha
            })

            sessionStorage.setItem('tokenAdmin', resposta.data.token)
            setAutenticado(true)
            await buscarAgendamentos()

        } catch {
            setErro('E-mail ou senha inválidos.')

        } finally {
            setCarregando(false)
        }
    }

    // Concluir agendamento
    async function concluirAgendamento(id) {

        try {
            await api.patch(`/api/agendamentos/${id}/concluir`)
            await buscarAgendamentos()

        } catch {
            setErro('Não foi possível concluir o agendamento.')
        }
    }

    // Cancelar agendamento
    async function cancelarAgendamento(id) {

        const confirmar = window.confirm(
            'Tem certeza que deseja cancelar este agendamento?'
        )

        if (!confirmar) return

        try {
            await api.patch(`/api/agendamentos/${id}/cancelar`)
            await buscarAgendamentos()

        } catch {
            setErro('Não foi possível cancelar o agendamento.')
        }
    }

    // Encerra a sessão
    function realizarLogout() {

        sessionStorage.removeItem('tokenAdmin')

        setAutenticado(false)
        setEmail('')
        setSenha('')
        setErro('')
        setAgendamentos([])

        setFiltroStatus('TODOS')
        setPesquisa('')
        setFiltroPeriodo('TODOS')
        setDataInicio('')
        setDataFim('')
        setPaginaAtual(0)
        setTotalPaginas(0)
        setTotalAgendamentos(0)
    }

    // Limpa todos os filtros
    function limparFiltros() {
        setPesquisa('')
        setFiltroStatus('TODOS')
        setFiltroPeriodo('TODOS')
        setDataInicio('')
        setDataFim('')
        setPaginaAtual(0)
        buscarAgendamentos({
            pesquisa: '',
            filtroStatus: 'TODOS',
            filtroPeriodo: 'TODOS',
            dataInicio: '',
            dataFim: '',
            paginaAtual: 0
        })
    }

    if (verificando) {
        return (
            <main className="admin-loading">
                Verificando acesso...
            </main>
        )
    }

    // Tela de login
    if (!autenticado) {

        return (
            <main className="admin-login-page">
                <section className="admin-login-card">

                    <h1>GUSTAVO <span>BARBER</span></h1>
                    <p className="admin-subtitle">Área administrativa</p>

                    <form onSubmit={realizarLogin}>

                        <div className="admin-form-group">
                            <label>E-mail</label>
                            <input
                                type="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                placeholder="Digite seu e-mail"
                                required
                            />
                        </div>

                        <div className="admin-form-group">
                            <label>Senha</label>
                            <input
                                type="password"
                                value={senha}
                                onChange={(e) => setSenha(e.target.value)}
                                placeholder="Digite sua senha"
                                required
                            />
                        </div>

                        {erro && <p className="admin-error">{erro}</p>}

                        <button
                            className="admin-button admin-button-primary admin-login-button"
                            type="submit"
                            disabled={carregando}
                        >
                            {carregando ? 'Entrando...' : 'Entrar'}
                        </button>

                    </form>
                </section>
            </main>
        )
    }

    return (
        <main className="admin-dashboard">

            <header className="admin-header">
                <div>
                    <h1>GUSTAVO <span>BARBER</span></h1>
                    <p>Painel administrativo</p>
                </div>

                <button
                    className="admin-button admin-button-logout"
                    onClick={realizarLogout}
                >
                    Sair
                </button>
            </header>

            <section className="admin-content">

                <div className="admin-section-title">
                    <div>
                        <h2>Visão geral</h2>
                        <p>Acompanhe os agendamentos da barbearia.</p>
                    </div>

                    <button
                        className="admin-button admin-button-primary"
                        onClick={() => buscarAgendamentos()}
                        disabled={carregandoAgendamentos}
                    >
                        {carregandoAgendamentos ? 'Atualizando...' : 'Atualizar lista'}
                    </button>
                </div>

                {erro && <p className="admin-error">{erro}</p>}

                {/* Cards de resumo */}
                <section className="admin-stats">

                    <div className="admin-stat-card">
                        <span>Total</span>
                        <strong>{totalAgendamentos}</strong>
                    </div>

                    <div className="admin-stat-card">
                        <span>Pendentes</span>
                        <strong>{resumo.pendentes}</strong>
                    </div>

                    <div className="admin-stat-card">
                        <span>Concluídos</span>
                        <strong>{resumo.concluidos}</strong>
                    </div>

                    <div className="admin-stat-card">
                        <span>Cancelados</span>
                        <strong>{resumo.cancelados}</strong>
                    </div>

                </section>

                {/* Pesquisa e filtros */}
                <section className="admin-bookings">

                    <div className="admin-bookings-header">
                        <h2>Agendamentos</h2>
                    </div>

                    <div className="admin-filters">

                        <input
                            type="text"
                            placeholder="Pesquisar por nome, telefone ou serviço..."
                            value={pesquisa}
                            onChange={(e) => {
                                setPesquisa(e.target.value)
                                setPaginaAtual(0)
                                buscarAgendamentos({
                                    pesquisa: e.target.value,
                                    paginaAtual: 0
                                })
                            }}
                            className="admin-search"
                        />

                        <div className="admin-filter-selects">

                            <select
                                value={filtroStatus}
                                onChange={(e) => {
                                    setFiltroStatus(e.target.value)
                                    setPaginaAtual(0)
                                    buscarAgendamentos({
                                        filtroStatus: e.target.value,
                                        paginaAtual: 0
                                    })
                                }}
                            >
                                <option value="TODOS">Todos os status</option>
                                <option value="PENDENTE">Pendentes</option>
                                <option value="CONCLUIDO">Concluídos</option>
                                <option value="CANCELADO">Cancelados</option>
                            </select>

                            <select
                                value={filtroPeriodo}
                                onChange={(e) => {
                                    setFiltroPeriodo(e.target.value)
                                    setPaginaAtual(0)
                                    buscarAgendamentos({
                                        filtroPeriodo: e.target.value,
                                        paginaAtual: 0
                                    })
                                }}
                            >
                                <option value="TODOS">Todos os períodos</option>
                                <option value="HOJE">Hoje</option>
                                <option value="SEMANA">Esta semana</option>
                                <option value="PERSONALIZADO">Período personalizado</option>
                            </select>

                        </div>

                        {filtroPeriodo === 'PERSONALIZADO' && (

                            <div className="admin-date-filters">

                                <div>
                                    <label>Data inicial</label>
                                    <input
                                        type="date"
                                        value={dataInicio}
                                        max={dataFim || undefined}
                                        onChange={(e) => {
                                            setDataInicio(e.target.value)
                                            setPaginaAtual(0)
                                            buscarAgendamentos({
                                                dataInicio: e.target.value,
                                                paginaAtual: 0
                                            })
                                        }}
                                    />
                                </div>

                                <div>
                                    <label>Data final</label>
                                    <input
                                        type="date"
                                        value={dataFim}
                                        min={dataInicio || undefined}
                                        onChange={(e) => {
                                            setDataFim(e.target.value)
                                            setPaginaAtual(0)
                                            buscarAgendamentos({
                                                dataFim: e.target.value,
                                                paginaAtual: 0
                                            })
                                        }}
                                    />
                                </div>

                            </div>
                        )}

                        <button
                            className="admin-button admin-button-logout"
                            onClick={limparFiltros}
                        >
                            Limpar filtros
                        </button>

                    </div>

                    <p className="admin-results-count">
                        {totalAgendamentos} agendamento(s) encontrado(s)
                    </p>

                    {carregandoAgendamentos ? (

                        <p className="admin-empty">
                            Carregando agendamentos...
                        </p>

                    ) : agendamentos.length === 0 ? (

                        <p className="admin-empty">
                            Nenhum agendamento encontrado.
                        </p>

                    ) : (

                        <div className="admin-bookings-list">

                            {agendamentos.map((agendamento) => {

                                const status = String(agendamento.status ?? '')
                                    .trim()
                                    .toUpperCase()

                                return (
                                    <article
                                        className="admin-booking-card"
                                        key={agendamento.id}
                                    >

                                        <div className="admin-booking-info">

                                            <div className="admin-booking-title">
                                                <h3>{agendamento.nomeCliente}</h3>

                                                <span className={`admin-status status-${status.toLowerCase()}`}>
                                                    {status}
                                                </span>
                                            </div>

                                            <p>
                                                <strong>Telefone:</strong> {agendamento.telefoneCliente}
                                            </p>

                                            <p>
                                                <strong>Serviço:</strong> {agendamento.nomeServico}
                                            </p>

                                            <p>
                                                <strong>Data:</strong> {agendamento.data}
                                            </p>

                                            <p>
                                                <strong>Horário:</strong> {agendamento.horario}
                                            </p>

                                        </div>

                                        {status === 'PENDENTE' && (
                                            <div className="admin-booking-actions">

                                                <button
                                                    className="admin-button admin-button-conclude"
                                                    onClick={() => concluirAgendamento(agendamento.id)}
                                                >
                                                    Concluir
                                                </button>

                                                <button
                                                    className="admin-button admin-button-cancel"
                                                    onClick={() => cancelarAgendamento(agendamento.id)}
                                                >
                                                    Cancelar
                                                </button>

                                            </div>
                                        )}

                                    </article>
                                )
                            })}

                        </div>
                    )}

                    {/* Paginação */}
                    {!carregandoAgendamentos && totalAgendamentos > 0 && (

                        <div className="admin-pagination">

                            <button
                                className="admin-button admin-button-logout"
                                onClick={() => {
                                    const novaPagina = paginaAtual - 1
                                    setPaginaAtual(novaPagina)
                                    buscarAgendamentos({ paginaAtual: novaPagina })
                                }}
                                disabled={paginaAtual === 0}
                            >
                                Anterior
                            </button>

                            <span>
                                Página {paginaAtual + 1} de {totalPaginas}
                            </span>

                            <button
                                className="admin-button admin-button-primary"
                                onClick={() => {
                                    const novaPagina = paginaAtual + 1
                                    setPaginaAtual(novaPagina)
                                    buscarAgendamentos({ paginaAtual: novaPagina })
                                }}
                                disabled={paginaAtual + 1 >= totalPaginas}
                            >
                                Próxima
                            </button>

                        </div>
                    )}

                </section>

            </section>

        </main>
    )
}

export default Admin
