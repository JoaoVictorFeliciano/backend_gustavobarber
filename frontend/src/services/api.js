
import axios from 'axios'

const api = axios.create({
    baseURL: 'http://localhost:8080'
})

// Adiciona o JWT automaticamente nas requisições
api.interceptors.request.use((config) => {

    const token = sessionStorage.getItem('tokenAdmin')

    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }

    return config
})

export default api
