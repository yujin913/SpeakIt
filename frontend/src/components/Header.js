/* src/components/Header.js */

import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import './Header.css';

const Header = ({ user }) => {
  const navigate = useNavigate();

  // 로그아웃 API 호출 시, HttpOnly 쿠키가 자동 전송되므로 withCredentials만 사용
  const handleLogout = async () => {
    try {
      const response = await axios.post('http://localhost:8080/user/logout', {}, { withCredentials: true });
      console.log("로그아웃 API 응답:", response.data);
      localStorage.removeItem("user");
      navigate('/', { replace: true });
      window.location.reload();
    } catch (error) {
      console.error('로그아웃 실패', error.response || error);
    }
  };

  return (
    <header className="header">
      <div className="container">
        <Link to="/" className="logo">SpeakIt</Link>
        <nav className="nav">
          {user ? (
            <>
              <Link to="/profile" className="header-username"><strong>{user.username}님</strong></Link>
              <button className="header-logout" onClick={handleLogout}>로그아웃</button>
            </>
          ) : (
            <>
              <Link to="/signIn" className="header-signin">로그인</Link>
              <Link to="/signUp" className="header-signup">회원가입</Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
};

export default Header;
