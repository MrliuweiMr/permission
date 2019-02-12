package cn.tycoding.shiro.realm;

import cn.tycoding.shiro.entity.Menu;
import cn.tycoding.shiro.entity.Role;
import cn.tycoding.shiro.entity.User;
import cn.tycoding.shiro.enums.StatusEnums;
import cn.tycoding.shiro.service.MenuService;
import cn.tycoding.shiro.service.RoleService;
import cn.tycoding.shiro.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author tycoding
 * @date 2019-01-19
 */
public class AuthRealm extends AuthorizingRealm {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private MenuService menuService;

    //权限认证
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        /**
         * 1. 获取当前登录用户信息
         * 2. 获取当前用户关联的角色、权限等
         * 3. 将角色、权限封装到AuthorizationInfo对象中并返回
         */
        User user = (User) SecurityUtils.getSubject().getPrincipal();

        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();

        //获取用户角色
        List<Role> roleList = roleService.findUserRole(user.getUsername());
        Set<String> roleSet = roleList.stream().map(Role::getName).collect(Collectors.toSet());
        simpleAuthorizationInfo.setRoles(roleSet);

        //获取用户权限
        List<Menu> menuList = menuService.findUserPerms(user.getUsername());
        Set<String> permSet = menuList.stream().map(Menu::getName).collect(Collectors.toSet());
        simpleAuthorizationInfo.setStringPermissions(permSet);

        return simpleAuthorizationInfo;
    }

    //身份校验
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        /**
         * 1. 从token中获取输入的用户名
         * 2. 通过username查询数据库得到用户对象
         * 3. 调用Authenticator进行密码校验
         */

        //获取用户名和密码
        String username = (String) authenticationToken.getPrincipal();
        String password = new String((char[]) authenticationToken.getCredentials());

        User user = userService.findByName(username);

        if (user == null) {
            throw new UnknownAccountException(String.valueOf(StatusEnums.ACCOUNT_UNKNOWN));
        }
        if (!password.equals(user.getPassword())) {
            throw new IncorrectCredentialsException(String.valueOf(StatusEnums.ACCOUNT_ERROR));
        }

        return new SimpleAuthenticationInfo(user, password, getName());
    }
}
