/* src/pages/SignInPage.js */

import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import './SignInPage.css';

const SignInPage = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const navigate = useNavigate();

  // 일반 로그인 API 호출 함수 (JWT 토큰은 서버가 HttpOnly 쿠키에 저장)
  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage('');
    const data = { email, password };

    try {
      const response = await axios.post('http://localhost:8080/user/signIn', data, { withCredentials: true });
      alert("로그인 성공!");
      console.log(response.data);
      navigate('/', { replace: true });
    } catch (error) {
      let msg = "로그인 실패, 다시 시도해주세요.";
      if (error.response && error.response.data && error.response.data.message) {
        msg = error.response.data.message;
      }
      setErrorMessage(msg);
      console.error('로그인 에러', error.response || error);
    }
  };

  // 구글 로그인 버튼 클릭 시, 백엔드 OAuth2 시작 URL로 리다이렉트
  const handleGoogleSignIn = () => {
    window.location.href = 'http://localhost:8080/oauth2/authorization/google';
  };

  return (
    <div className="page-container">
      <Header />
      <div className="page-content">
        <div className="signin-page">
          <h2>로그인</h2>
          <form className="signin-form" onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="email">이메일</label>
              <input
                type="email"
                id="email"
                placeholder="이메일을 입력하세요"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="password">비밀번호</label>
              <input
                type="password"
                id="password"
                placeholder="비밀번호를 입력하세요"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            {errorMessage && (
              <div className="error-block">
                <div className="error-text">- {errorMessage}</div>
              </div>
            )}
            <div className="form-group">
              <button type="submit" className="page-signin">
                로그인
              </button>
            </div>
            {/* 구글 로그인 버튼 추가 */}
            <div className="form-group">
              <button type="button" className="google-signin" onClick={handleGoogleSignIn}>
                구글 로그인
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default SignInPage;
