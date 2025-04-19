/* src/pages/ProfilePage.js */

import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import './ProfilePage.css';

const ProfilePage = () => {
  
  const [profile, setProfile] = useState(null);
  const [editUsername, setEditUsername] = useState('');
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [errorMessages, setErrorMessages] = useState([]);
  const [error, setError] = useState('');

  const navigate = useNavigate();

  useEffect(() => {
    axios.get('http://localhost:8080/user/profile', { withCredentials: true })
      .then(response => {
        setProfile(response.data);
        setEditUsername(response.data.username);
      })
      .catch(error => {
        console.error('프로필 조회 오류', error);
        setError('프로필 정보를 가져올 수 없습니다.');
      });
  }, []);

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    setErrorMessages([]);

    const data = {
      currentPassword: currentPassword,
      ...(editUsername.trim() !== '' && { username: editUsername }),
      ...(newPassword.trim() !== '' && { newPassword: newPassword })
    };

    if (!currentPassword.trim()) {
      alert("회원정보 수정을 위해 현재 비밀번호를 입력해 주세요.");
      return;
    }
    try {
      const response = await axios.patch('http://localhost:8080/user/profile', data, { withCredentials: true });
      console.log("회원정보 수정 API 응답:", response.data);
      alert("회원정보 수정 성공!");
      setProfile(response.data);
      setCurrentPassword('');
      setNewPassword('');
    } catch (error) {
      let msg = "회원정보 수정 실패, 다시 시도해주세요.";
      if (error.response && error.response.data && error.response.data.message) {
        msg = error.response.data.message;
      }
      const errors = msg.split(",").map(err => err.trim());
      setErrorMessages(errors);
      console.error('회원정보 수정 에러', error.response || error);
    }
  };

  const handleDeleteAccount = async () => {
    if (!profile) {
      alert("프로필 정보를 먼저 불러오세요.");
      return;
    }

    // 소셜 로그인 사용자는 provider 필드가 존재함을 기준으로 분기
    if (profile.provider != null && profile.provider.trim() !== "") {
      if (!window.confirm("정말 계정 정보를 삭제하시겠습니까? (소셜 로그인 사용자)")) {
        return;
      }
      try {
        // 단일 연동 해제 API (/disconnect/social)를 호출하면,
        // 백엔드에서 내부적으로 provider 정보를 확인하여 구글/네이버를 구분합니다.
        const response = await axios.post('http://localhost:8080/disconnect/social', {}, { withCredentials: true });
        console.log("소셜 연동 해제 API 응답:", response.data);
        alert("계정이 삭제되었습니다.");
        navigate('/signIn', { replace: true });
        window.location.reload();
      } catch (error) {
        let msg = "계정 삭제 실패, 다시 시도해주세요.";
        if (error.response && error.response.data && error.response.data.message) {
          msg = error.response.data.message;
        }
        const errors = msg.split(",").map(err => err.trim());
        setErrorMessages(errors);
        console.error("계정 삭제 실패", error.response || error);
      }
    } else {
      // 일반 사용자인 경우 현재 비밀번호가 필요합니다.
      if (!currentPassword.trim()) {
        alert("회원탈퇴를 위해 현재 비밀번호를 입력해 주세요.");
        return;
      }
      try {
        const response = await axios.delete('http://localhost:8080/user/deleteAccount', {
          data: { currentPassword: currentPassword },
          withCredentials: true
        });
        console.log("계정 삭제 API 응답:", response.data);
        alert("계정이 삭제되었습니다.");
        navigate('/signIn', { replace: true });
        window.location.reload();
      } catch (error) {
        let msg = "계정 삭제 실패, 다시 시도해주세요.";
        if (error.response && error.response.data && error.response.data.message) {
          msg = error.response.data.message;
        }
        const errors = msg.split(",").map(err => err.trim());
        setErrorMessages(errors);
        console.error("계정 삭제 실패", error.response || error);
      }
    }
  };

  return (
    <div className="page-container">
      <div className="page-content">
        <div className="profile-page">
          <h2>회원정보 조회</h2>
          {error && <div className="error-block">{error}</div>}
          {profile ? (
            <form className="profile-form" onSubmit={handleUpdateProfile}>
              <div className="form-group">
                <label htmlFor="email">이메일</label>
                <input type="email" id="email" value={profile.email} readOnly className="readonly-input" />
              </div>
              <div className="form-group">
                <label htmlFor="registrationDate">가입일</label>
                <input type="text" id="registrationDate" value={profile.registrationDate} readOnly className="readonly-input" />
              </div>
              <div className="form-group">
                <label htmlFor="username">이름</label>
                <input type="text" id="username" placeholder="이름을 입력하세요" value={editUsername} onChange={(e) => setEditUsername(e.target.value)} />
              </div>
              <div className="form-group">
                <label htmlFor="currentPassword">현재 비밀번호</label>
                <input type="password" id="currentPassword" placeholder="현재 비밀번호를 입력하세요" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} required/>
              </div>
              <div className="form-group">
                <label htmlFor="newPassword">새 비밀번호 (변경하지 않으려면 비워두세요)</label>
                <input type="password" id="newPassword" placeholder="새 비밀번호를 입력하세요" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} />
              </div>
              {errorMessages.length > 0 && (
                <div className="error-block">
                  {errorMessages.map((err, idx) => <div key={idx} className="error-text">- {err}</div>)}
                </div>
              )}
              <div className="button-group">
                <button type="submit" className="update-profile-button">회원정보 수정</button>
                <button type="button" className="delete-account-button" onClick={handleDeleteAccount}>회원탈퇴</button>
              </div>
            </form>
          ) : (
            !error && <p>로딩 중...</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default ProfilePage;
