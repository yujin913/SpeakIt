package com.speakit.speakit.service.user;

import com.speakit.speakit.dto.user.SignInResponseDTO;

public interface SocialUserService {

    SignInResponseDTO processGoogleSocialLogin(String code);
    void disconnectGoogleSocialAccount(String email);

}
