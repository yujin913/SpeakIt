// src/pages/opic/TutorialStart.js
import React from 'react';
import { useNavigate } from 'react-router-dom';
import './TutorialStart.css';

/**
 * TutorialStart.js
 * Step 0: 튜토리얼 시작 화면 컴포넌트
 * 경로: /opic/start
 */
export default function TutorialStart() {
    const navigate = useNavigate();
  
    return (
      <div className="tutorial-container">
        <h1 className="tutorial-title">
          <span className="tutorial-icon">🎤</span>
          Oral Proficiency Interview - computer (OPIc)
        </h1>
        <p className="tutorial-subtitle">
          지금부터 English 말하기 평가를 시작하겠습니다.
        </p>
        <img
          src="/ava.png"
          alt="Ava"
          className="tutorial-avatar"
        />
        <p className="tutorial-description">
          본 인터뷰 평가의 진행자는 <strong>Ava</strong> 입니다.
        </p>
        <button
          className="tutorial-next-btn"
          onClick={() => navigate('/opic/survey')}
        >
          Next ➤
        </button>
      </div>
    );
  }