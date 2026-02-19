import axios from 'axios';

// Your live Railway API URL
const API_URL = 'https://football-moneyball-production.up.railway.app/api';

// Create axios instance
const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests if it exists
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Auth API
export const authAPI = {
  signup: (username, password, role = 'GUEST') =>
    api.post('/auth/signup', { username, password, role }),

  login: (username, password) =>
    api.post('/auth/login', { username, password }),
};

// Matches API
export const matchesAPI = {
  getUpcoming: () => api.get('/matches/upcoming'),
  getById: (id) => api.get(`/matches/${id}`),
};

// Teams API
export const teamsAPI = {
  getAll: () => api.get('/teams'),
};

// Predictions API
export const predictionsAPI = {
  predict: (homeTeamId, awayTeamId, includeAiAnalysis = false) =>
    api.post('/predictions/predict', {
      homeTeamId,
      awayTeamId,
      includeAiAnalysis,
    }),
};

export default api;