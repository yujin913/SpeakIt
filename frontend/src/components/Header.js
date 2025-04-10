/* src/components/Header.js */

import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import './Header.css';

const Header = () => {
  const [loginStatus, setLoginStatus] = useState({ loggedIn: false, username: "" });
  const navigate = useNavigate();

  useEffect(() => {
    // HttpOnly 쿠키에 저장된 JWT 토큰이 자동으로 전송되므로, withCredentials:true 설정
    axios.get('http://localhost:8080/user/loginStatus', { withCredentials: true })
      .then(response => {
        // 서버가 로그인 상태라면 { loggedIn: true, username: "홍길동" } 형태의 응답 반환
        setLoginStatus(response.data);
      })
      .catch(error => {
        console.error('로그인 상태 조회 오류', error);
        setLoginStatus({ loggedIn: false });
      });
  }, []);

  // 로그아웃 API 호출 시, HttpOnly 쿠키가 자동 전송되므로 withCredentials만 사용
  const handleLogout = async () => {
    try {
      // 로그아웃 API 호출 시, HttpOnly 쿠키에 저장된 토큰이 자동으로 전송됨
      await axios.post('http://localhost:8080/user/logout', {}, { withCredentials: true });
      setLoginStatus({ loggedIn: false });
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
          {loginStatus.loggedIn ? (
            <>
              {/* 로그인된 상태일 경우, 사용자 이름(예: 홍길동)을 표시하고 프로필 페이지로 연결 */}
              <Link to="/profile" className="header-username">
                <strong>{loginStatus.username}님</strong>
              </Link>
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
