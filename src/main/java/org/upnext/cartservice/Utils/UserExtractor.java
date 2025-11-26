package org.upnext.cartservice.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.upnext.cartservice.Models.CartItem;
import org.upnext.sharedlibrary.Dtos.UserDto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Enumeration;

public class UserExtractor {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static UserDto userExtractor(HttpServletRequest request) {
        try {
            String header = request.getHeader("X-User");


            if (header == null) {
                return null;
            }
            String decoded = new String(Base64.getDecoder().decode(header), StandardCharsets.UTF_8);
            return objectMapper.readValue(decoded, UserDto.class);
        } catch (Exception e) {
            return null;
        }
    }
}
