package com.junsang.course_backend.global.security;

import jakarta.servlet.http.HttpServletRequest;
import com.junsang.course_backend.global.exception.BusinessException;
import com.junsang.course_backend.global.exception.ErrorCode;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.springframework.stereotype.Component;

@Component
public class LocalAdminAccessVerifier {

    // ── 로컬 관리자 접근 검증 ─────────────────────────────────────────────
    public void verify(HttpServletRequest request) {
        try {
            if (!InetAddress.getByName(request.getRemoteAddr()).isLoopbackAddress()) {
                throw new BusinessException(ErrorCode.LOCAL_ADMIN_ACCESS_DENIED);
            }
        } catch (UnknownHostException exception) {
            throw new BusinessException(ErrorCode.LOCAL_ADMIN_ACCESS_DENIED);
        }
    }
}
