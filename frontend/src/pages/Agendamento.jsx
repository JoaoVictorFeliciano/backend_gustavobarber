
import { useEffect, useState } from 'react'
import api from '../services/api'
import './Agendamento.css'

function Agendamento() {
    const [servicos, setServicos] = useState([])
    const [servicoId, setServicoId] = useState('')
    const [carregando, setCarregando] = useState(true)
    const [erro, setErro] = useState('')

    const [nomeCliente, setNomeCliente] = useState('')
    const [telefoneCliente, setTelefoneCliente] = useState('')

    const [dataSelecionada, setDataSelecionada] = useState('')
    const [horarios, setHorarios] = useState([])
    const [carregandoHorarios, setCarregandoHorarios] = useState(false)
    const [horarioSelecionado, setHorarioSelecionado] = useState('')

    const [semanaAtual, setSemanaAtual] = useState(0)

    const [enviando, setEnviando] = useState(false)
    const [erroAgendamento, setErroAgendamento] = useState('')
    const [sucesso, setSucesso] = useState('')

    const hoje = new Date()
    hoje.setHours(0, 0, 0, 0)

    const segunda = new Date(hoje)
    const diaSemana = hoje.getDay()

    segunda.setDate(
        hoje.getDate() - (diaSemana === 0 ? 6 : diaSemana - 1)
        + (semanaAtual * 7)
    )

    const diasSemana = Array.from({ length: 6 }, (_, index) => {
        const data = new Date(segunda)
        data.setDate(segunda.getDate() + index)

        return {
            data: `${data.getFullYear()}-${String(data.getMonth() + 1).padStart(2, '0')}-${String(data.getDate()).padStart(2, '0')}`,
            dia: ['DOM', 'SEG', 'TER', 'QUA', 'QUI', 'SEX', 'SÁB'][data.getDay()],
            numero: data.getDate()
        }
    })

    useEffect(() => {
        async function carregarServicos() {
            try {
                const resposta = await api.get('/api/servicos')
                setServicos(resposta.data)
            } catch (error) {
                console.error('Erro ao carregar serviços:', error)
                setErro('Não foi possível carregar os serviços.')
            } finally {
                setCarregando(false)
            }
        }

        carregarServicos()
    }, [])

    useEffect(() => {
        async function carregarHorarios() {
            if (!dataSelecionada || !servicoId) {
                setHorarios([])
                return
            }

            setCarregandoHorarios(true)
            setHorarioSelecionado('')

            try {
                const resposta = await api.post(
                    '/api/agendamentos/disponibilidade',
                    {
                        data: dataSelecionada,
                        servicoId: Number(servicoId)
                    }
                )

                setHorarios(resposta.data.horarios)

            } catch (error) {
                console.error('Erro ao buscar horários:', error)
                setHorarios([])
            } finally {
                setCarregandoHorarios(false)
            }
        }

        carregarHorarios()
    }, [dataSelecionada, servicoId])

    async function handleSubmit(event) {
        event.preventDefault()

        setErroAgendamento('')
        setSucesso('')

        if (!nomeCliente.trim() || !telefoneCliente.trim() ||
            !servicoId || !dataSelecionada || !horarioSelecionado) {
            setErroAgendamento('Preencha todos os campos e selecione um horário.')
            return
        }

        setEnviando(true)

        try {
            const agendamento = {
                nomeCliente: nomeCliente.trim(),
                telefoneCliente: telefoneCliente.trim(),
                data: dataSelecionada,
                horario: horarioSelecionado,
                servicoId: Number(servicoId)
            }

            // Salva o agendamento no backend.
            await api.post('/api/agendamentos', agendamento)

            // Localiza o serviço escolhido.
            const servicoEscolhido = servicos.find(
                (servico) => servico.id === Number(servicoId)
            )

            // Formata a data para o padrão brasileiro.
            const dataFormatada = new Date(
                `${dataSelecionada}T00:00:00`
            ).toLocaleDateString('pt-BR')

            // Número temporário para o teste.
            const numeroWhatsApp = '5583996160497'

            // Monta a mensagem que será preenchida no WhatsApp.
            const mensagem = `
Olá! Gostaria de confirmar meu agendamento na Gustavo Barber.

Nome: ${nomeCliente.trim()}
Serviço: ${servicoEscolhido?.nome || 'Não informado'}
Data: ${dataFormatada}
Horário: ${horarioSelecionado.slice(0, 5)}

Aguardo a confirmação. Obrigado!
            `.trim()

            // Redireciona para o WhatsApp somente após salvar no banco.
            const linkWhatsApp = `https://wa.me/${numeroWhatsApp}?text=${encodeURIComponent(mensagem)}`

            window.location.href = linkWhatsApp

        } catch (error) {
            console.error('Erro ao realizar agendamento:', error)

            if (error.response?.status === 409) {
                setErroAgendamento(
                    'Esse horário acabou de ser ocupado. Escolha outro horário.'
                )

                try {
                    const resposta = await api.post(
                        '/api/agendamentos/disponibilidade',
                        {
                            data: dataSelecionada,
                            servicoId: Number(servicoId)
                        }
                    )

                    setHorarios(resposta.data.horarios)
                } catch (erro) {
                    console.error('Erro ao atualizar horários:', erro)
                }

            } else {
                setErroAgendamento(
                    error.response?.data?.message ||
                    'Não foi possível realizar o agendamento. Tente novamente.'
                )
            }

        } finally {
            setEnviando(false)
        }
    }

    return (
        <main className="agendamento">
            <section className="agendamento-header">
                <p>GUSTAVO BARBER</p>
                <h1>AGENDE SEU HORÁRIO</h1>
                <span>Preencha os dados abaixo para solicitar seu atendimento.</span>
            </section>

            <form className="agendamento-form" onSubmit={handleSubmit}>

                <div className="campo">
                    <label>Seu nome</label>
                    <input
                        type="text"
                        placeholder="Digite seu nome completo"
                        value={nomeCliente}
                        onChange={(e) => setNomeCliente(e.target.value)}
                        maxLength={100}
                        required
                    />
                </div>

                <div className="campo">
                    <label>Telefone / WhatsApp</label>
                    <input
                        type="tel"
                        placeholder="(00) 00000-0000"
                        value={telefoneCliente}
                        onChange={(e) => setTelefoneCliente(e.target.value)}
                        maxLength={20}
                        required
                    />
                </div>

                <div className="campo">
                    <label>Escolha o serviço</label>
                    <select
                        value={servicoId}
                        onChange={(e) => {
                            setServicoId(e.target.value)
                            setHorarioSelecionado('')
                            setSucesso('')
                            setErroAgendamento('')
                        }}
                        required
                    >
                        <option value="">
                            {carregando ? 'Carregando serviços...' : 'Selecione um serviço'}
                        </option>

                        {servicos.map((servico) => (
                            <option key={servico.id} value={servico.id}>
                                {servico.nome} - R$ {servico.preco}
                            </option>
                        ))}
                    </select>

                    {erro && <p>{erro}</p>}
                </div>

                <div className="campo">
                    <label>Escolha o dia</label>

                    <div className="calendario">
                        <div className="navegacao-semana">
                            <button
                                type="button"
                                disabled={semanaAtual === 0}
                                onClick={() => setSemanaAtual(semanaAtual - 1)}
                            >
                                SEMANA ANTERIOR
                            </button>

                            <button
                                type="button"
                                onClick={() => setSemanaAtual(semanaAtual + 1)}
                            >
                                PRÓXIMA SEMANA
                            </button>
                        </div>

                        <div className="dias-semana">
                            {diasSemana.map((dia) => {
                                const dataPassada =
                                    new Date(`${dia.data}T00:00:00`) < hoje

                                return (
                                    <button
                                        key={dia.data}
                                        type="button"
                                        disabled={dataPassada}
                                        className={
                                            dataSelecionada === dia.data
                                                ? 'dia-selecionado'
                                                : 'dia'
                                        }
                                        onClick={() => {
                                            setDataSelecionada(dia.data)
                                            setSucesso('')
                                            setErroAgendamento('')
                                        }}
                                    >
                                        <span>{dia.dia}</span>
                                        <strong>{dia.numero}</strong>
                                    </button>
                                )
                            })}
                        </div>
                    </div>
                </div>

                <div className="campo">
                    <label>Escolha o horário</label>

                    {!servicoId || !dataSelecionada ? (
                        <p>Selecione primeiro um serviço e um dia.</p>
                    ) : carregandoHorarios ? (
                        <p>Buscando horários...</p>
                    ) : horarios.length === 0 ? (
                        <p>Não há horários disponíveis para este dia.</p>
                    ) : (
                        <div className="horarios-disponiveis">
                            {horarios.map((item) => (
                                <button
                                    key={item.horario}
                                    type="button"
                                    disabled={!item.disponivel}
                                    className={
                                        !item.disponivel
                                            ? 'horario-ocupado'
                                            : horarioSelecionado === item.horario
                                                ? 'horario-selecionado'
                                                : 'horario'
                                    }
                                    onClick={() => {
                                        setHorarioSelecionado(item.horario)
                                        setErroAgendamento('')
                                        setSucesso('')
                                    }}
                                >
                                    {item.horario.slice(0, 5)}
                                    {!item.disponivel && <span>OCUPADO</span>}
                                </button>
                            ))}
                        </div>
                    )}
                </div>

                {erroAgendamento && (
                    <p className="mensagem-erro" role="alert">
                        {erroAgendamento}
                    </p>
                )}

                {sucesso && (
                    <p className="mensagem-sucesso">
                        {sucesso}
                    </p>
                )}

                <button
                    type="submit"
                    className="botao-agendar"
                    disabled={enviando}
                >
                    {enviando ? 'AGUARDE...' : 'AGENDAR HORÁRIO'}
                </button>

            </form>
        </main>
    )
}

export default Agendamento
