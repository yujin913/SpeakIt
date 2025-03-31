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

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage('');
    const data = { email, password };

    try {
      // 로그인 API 호출 시 withCredentials 옵션 추가
      const response = await axios.post('http://localhost:8080/user/signIn', data, { withCredentials: true });
      // 로그인 성공 후, 반환된 사용자 정보를 로컬 스토리지에 저장
      localStorage.setItem("user", JSON.stringify(response.data));
      alert("로그인 성공!");
      console.log(response.data);
      // 메인 페이지로 이동
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
          </form>
        </div>
      </div>
    </div>
  );
};

export default SignInPage;
