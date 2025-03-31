/* src/pages/MainPage.js */
import React from 'react';
import Header from '../components/Header';
import './MainPage.css';

const MainPage = () => {
  // 로컬 스토리지에서 "user" 정보를 읽어오고, 없으면 null로 처리합니다.
  const user = JSON.parse(localStorage.getItem("user")) || null;

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
