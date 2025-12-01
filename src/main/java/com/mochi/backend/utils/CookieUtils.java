package com.mochi.backend.utils;


import com.mochi.backend.enums.ErrorCode;
import com.mochi.backend.exception.AppException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class CookieUtils {
    public static String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName()
                        .equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        throw new AppException(ErrorCode.NOT_FOUND_IN_COOKIES);

    }
}