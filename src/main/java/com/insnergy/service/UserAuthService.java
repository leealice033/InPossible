package com.insnergy.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.insnergy.vo.UserInfo;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserAuthService implements UserDetailsService {
  
  private final UserInfoService userService;
  
  public UserAuthService(UserInfoService userService) {
    this.userService = userService;
  }
  
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    log.debug("loadUserByUsername[{}]", username);
    final UserDetails result;
    Optional<UserInfo> _userInfo = userService.findUserInfoById(username);
    if (_userInfo.isPresent()) {
      UserInfo userInfo = _userInfo.get();
      String authorityListString = userInfo.getRoles()
                                           .stream()
                                           .map(role -> "ROLE_" + role)
                                           .collect(Collectors.joining(","));
      List<GrantedAuthority> auth = AuthorityUtils.commaSeparatedStringToAuthorityList(authorityListString);
      final org.springframework.security.core.userdetails.User user = new org.springframework.security.core.userdetails.User(
          userInfo.getId(), userInfo.getPassword(), auth);
      result = user;
    } else {
      result = null;
      throw new UsernameNotFoundException("No username: " + username);
    }
    log.debug("loadUserByUsername[{}]={}", username, result);
    return result;
  }
  
}
