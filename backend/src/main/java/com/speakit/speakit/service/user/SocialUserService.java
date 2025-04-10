package com.speakit.speakit.service.user;

import com.speakit.speakit.dto.user.SignInResponseDTO;

public interface SocialUserService {

    SignInResponseDTO processSocialLogin(String code);
    void disconnectSocialAccount(String email);

}
