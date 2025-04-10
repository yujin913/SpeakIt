/* src/pages/MainPage.js */

import React from 'react';
import Header from '../components/Header';
import './MainPage.css';

const MainPage = () => {
  return (
    <div className="main-page">
      {/* Header에서는 자체적으로 로그인 상태를 확인할 수 있도록 처리하거나,
          로그인이 필요 없는 경우 기본 UI를 렌더링하도록 구성 */}
      <Header />
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
