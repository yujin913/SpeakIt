// src/services/api.js
import axios from 'axios';

// 백엔드 API의 기본 URL을 설정합니다.
const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// GET 요청 예시 함수
export const getData = async (endpoint) => {
  try {
    const response = await api.get(endpoint);
    return response.data;
  } catch (error) {
    console.error('API GET 요청 에러:', error);
    throw error;
  }
};

// POST 요청 예시 함수
export const postData = async (endpoint, data) => {
  try {
    const response = await api.post(endpoint, data);
    return response.data;
  } catch (error) {
    console.error('API POST 요청 에러:', error);
    throw error;
  }
};

export default api;
