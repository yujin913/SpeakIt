/* src/App.js */
import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';

import Header from './components/Header';
import MainPage from './pages/MainPage';
import SignUpPage from './pages/user/SignUpPage';
import SignInPage from './pages/user/SignInPage';
import ProfilePage from './pages/user/ProfilePage';

// 튜토리얼 관련 페이지 import
import TutorialStart from './pages/opic/TutorialStart';
import Survey from './pages/opic/Survey';

function App() {
  return (
    <Router>
      <Header />
      <Routes>
        <Route path="/" element={<MainPage />} />
        <Route path="/signUp" element={<SignUpPage />} />
        <Route path="/signIn" element={<SignInPage />} />
        <Route path="/profile" element={<ProfilePage />} />

        {/* OPIc Tutorial Routes */}
        <Route path="/opic/start" element={<TutorialStart />} />
        <Route path="/opic/survey" element={<Survey />} />
      </Routes>
    </Router>
  );
}

export default App;
