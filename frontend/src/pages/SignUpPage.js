/* src/pages/SignUpPage.js */
import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import './SignUpPage.css';

const SignUpPage = () => {
  const [username, setUsername] = useState('');
  const [email, setEmail]       = useState('');
  const [password, setPassword] = useState('');
  // 에러 메시지를 배열로 관리 (여러 오류 메시지를 동시에 표시)
  const [errorMessages, setErrorMessages] = useState([]);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessages([]);
    const data = { username, email, password };

    try {
      const response = await axios.post('http://localhost:8080/user/signUp', data);
      alert("회원가입 성공!");
      console.log(response.data);
      // 회원가입 성공 시 메인 페이지로 이동
      navigate('/');
    } catch (error) {
      let msg = "회원가입 실패, 다시 시도해주세요.";
      if (error.response && error.response.data && error.response.data.message) {
        msg = error.response.data.message;
      }
      // 백엔드에서 콤마로 연결된 메시지를 반환하므로 split 처리
      const errors = msg.split(",").map(err => err.trim());
      setErrorMessages(errors);
      console.error('회원가입 에러', error.response || error);
    }
  };

  return (
    <div className="page-container">
      <Header />
      <div className="page-content">
        <div className="signup-page">
          <h2>회원가입</h2>
          <form className="signup-form" onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="username">이름</label>
              <input
                type="text"
                id="username"
                placeholder="이름을 입력하세요"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
              />
            </div>
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
            {/* 제출 버튼 위에 오류 메시지 블록을 한 번에 표시 */}
            {errorMessages.length > 0 && (
              <div className="error-block">
                {errorMessages.map((err, idx) => (
                  <div key={idx} className="error-text">- {err}</div>
                ))}
              </div>
            )}
            <div className="form-group">
              <button type="submit" className="page-signup">
                회원가입
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default SignUpPage;
