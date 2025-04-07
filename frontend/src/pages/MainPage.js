/* src/pages/MainPage.js */

import React, { useEffect, useState } from 'react';
import axios from 'axios';
import Header from '../components/Header';
import './MainPage.css';

const MainPage = () => {
  const [user, setUser] = useState(null);

  useEffect(() => {
    // HttpOnly 쿠키가 자동으로 전송되므로, 별도의 Authorization 헤더 없이 /user/profile API 호출
    axios.get('http://localhost:8080/user/profile', { withCredentials: true })
      .then(response => {
        setUser(response.data);
        // 필요하다면 localStorage에 최소한의 사용자 정보 저장 (보안 민감도가 낮은 정보만)
        localStorage.setItem("user", JSON.stringify({
          id: response.data.id,
          username: response.data.username,
          email: response.data.email,
          createdAt: response.data.createdAt
        }));
      })
      .catch(error => {
        console.error('프로필 조회 오류', error);
      });
  }, []);

  return (
    <div className="main-page">
      <Header user={user} />
      <section className="hero">
        <div className="hero-content">
          <h1>Welcome to SpeakIt</h1>
          <p>Enhance your speaking skills with our intuitive platform.</p>
          <button className="cta-button">Get Started</button>
        </div>
      </section>
    </div>
  );
};

export default MainPage;
