package edu.miu.cs590.accountservice.config;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import edu.miu.cs590.accountservice.client.AuthClient;
import edu.miu.cs590.accountservice.dto.VerifyUserTokenDto;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private JwtTokenUtil jwtTokenUtil;
    private AuthClient authClient;

    public JwtTokenFilter(JwtTokenUtil jwtTokenUtil, AuthClient authClient) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.authClient = authClient;
    }

    //    private final JwtU
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (!hasAuthorizationBearToken(request)) {
            filterChain.doFilter(request, response);
            return;
        }


        String token = getToken(request);
        try {
            DecodedJWT jwt = jwtTokenUtil.validateToken(token);

            if (authClient.verifyUser(VerifyUserTokenDto.builder().email(jwt.getSubject()).build())) {
                setAuthenticationContext(jwt, request);
                filterChain.doFilter(request, response);
            } else {
                throw new JWTVerificationException("Token doesnot match");
            }


        } catch (JWTVerificationException ex) {
            System.out.println(ex.getMessage());
            throw new IllegalStateException(String.format("Token %s cannot be trusted", token));

        }

    }


    private boolean hasAuthorizationBearToken(HttpServletRequest request) {

        String header = request.getHeader("Authorization");

        if (ObjectUtils.isEmpty(header) || !header.startsWith("Bearer"))
            return false;
        return true;


    }

    private String getToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return header.split(" ")[1].trim();
    }

    private void setAuthenticationContext(DecodedJWT jwt, HttpServletRequest request) {

        List<String> authorities = jwt.getClaim("roles").asList(String.class);
        Set<GrantedAuthority> grantedAuthorities = authorities.stream().map(it -> new SimpleGrantedAuthority(it)).collect(Collectors.toSet());


        Authentication authentication = new UsernamePasswordAuthenticationToken(jwt.getSubject(), null, grantedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }


}
