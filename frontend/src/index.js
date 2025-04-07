// src/index.js
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './index.css'; // 전역 스타일
import axios from 'axios';

// withCredentials 옵션은 OAuth2 로그인 관련 쿠키 전송을 위해 필요합니다.
axios.defaults.withCredentials = true;

// axios 인터셉터를 설정하여, localStorage에 저장된 토큰을 자동으로 헤더에 추가합니다.
// axios.interceptors.request.use(
//   (config) => {
//     const user = JSON.parse(localStorage.getItem("user"));
//     if (user && user.accessToken) {
//       config.headers['Authorization'] = `Bearer ${user.accessToken}`;
//     }
//     return config;
//   },
//   (error) => Promise.reject(error)
// );

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
