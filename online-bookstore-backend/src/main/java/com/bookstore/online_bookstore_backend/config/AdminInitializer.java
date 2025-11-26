package com.bookstore.online_bookstore_backend.config;

import com.bookstore.online_bookstore_backend.entity.ERole;
import com.bookstore.online_bookstore_backend.entity.Role;
import com.bookstore.online_bookstore_backend.entity.User;
import com.bookstore.online_bookstore_backend.entity.UserAuth;
import com.bookstore.online_bookstore_backend.repository.RoleRepository;
import com.bookstore.online_bookstore_backend.repository.UserAuthRepository;
import com.bookstore.online_bookstore_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

/**
 * 应用启动时初始化默认管理员账号
 * 确保系统始终有一个可用的管理员账号用于登录
 */
@Component
@Order(2) // 在角色初始化之后运行
public class AdminInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);

    // 默认管理员配置 - 可以通过application.properties覆盖
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@bookstore.com";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAuthRepository userAuthRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        logger.info("========================================");
        logger.info("  检查并初始化默认管理员账号...");
        logger.info("========================================");

        // 检查admin用户是否存在
        Optional<User> existingAdminOpt = userRepository.findByUsername(DEFAULT_ADMIN_USERNAME);

        if (existingAdminOpt.isPresent()) {
            User existingAdmin = existingAdminOpt.get();
            logger.info("✓ 管理员账号已存在: {}", DEFAULT_ADMIN_USERNAME);

            // 验证密码是否正确
            Optional<UserAuth> userAuthOpt = userAuthRepository.findById(existingAdmin.getId());
            
            if (userAuthOpt.isPresent()) {
                UserAuth userAuth = userAuthOpt.get();
                
                // 检查当前密码是否能匹配默认密码
                boolean passwordMatches = passwordEncoder.matches(DEFAULT_ADMIN_PASSWORD, userAuth.getPassword());
                
                if (!passwordMatches) {
                    // 密码不匹配，重置为默认密码
                    logger.warn("⚠ 管理员密码不匹配，正在重置为默认密码...");
                    String encodedPassword = passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD);
                    userAuth.setPassword(encodedPassword);
                    userAuthRepository.save(userAuth);
                    logger.info("✓ 管理员密码已重置为: {}", DEFAULT_ADMIN_PASSWORD);
                } else {
                    logger.info("✓ 管理员密码验证成功");
                }
            } else {
                // UserAuth不存在，创建新的
                logger.warn("⚠ 管理员密码记录不存在，正在创建...");
                UserAuth newUserAuth = new UserAuth();
                newUserAuth.setUserId(existingAdmin.getId());
                newUserAuth.setUser(existingAdmin);
                newUserAuth.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
                userAuthRepository.save(newUserAuth);
                logger.info("✓ 管理员密码已创建");
            }

            // 确保有ADMIN角色
            ensureAdminRole(existingAdmin);

        } else {
            // 管理员不存在，创建新的
            logger.info("× 管理员账号不存在，正在创建...");
            createDefaultAdmin();
            logger.info("✓ 默认管理员账号创建成功！");
        }

        logger.info("========================================");
        logger.info("  🎯 管理员登录信息:");
        logger.info("  用户名: {}", DEFAULT_ADMIN_USERNAME);
        logger.info("  密码: {}", DEFAULT_ADMIN_PASSWORD);
        logger.info("========================================");
    }

    /**
     * 创建默认管理员账号
     */
    private void createDefaultAdmin() {
        // 1. 获取ADMIN角色
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Error: ROLE_ADMIN not found. Please ensure roles are initialized first."));

        // 2. 创建User实体
        User admin = new User();
        admin.setUsername(DEFAULT_ADMIN_USERNAME);
        admin.setEmail(DEFAULT_ADMIN_EMAIL);
        admin.setEnabled(true);
        admin.setRoles(Set.of(adminRole));

        // 3. 先保存User（获取ID）
        User savedAdmin = userRepository.save(admin);
        logger.info("✓ 用户实体已创建: id={}", savedAdmin.getId());

        // 4. 创建UserAuth实体
        UserAuth userAuth = new UserAuth();
        userAuth.setUserId(savedAdmin.getId());
        userAuth.setUser(savedAdmin);
        userAuth.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
        userAuthRepository.save(userAuth);
        logger.info("✓ 密码记录已创建");

        logger.info("✓ 管理员角色已分配: ROLE_ADMIN");
    }

    /**
     * 确保用户有ADMIN角色
     */
    private void ensureAdminRole(User user) {
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Error: ROLE_ADMIN not found."));

        if (!user.getRoles().contains(adminRole)) {
            logger.warn("⚠ 管理员缺少ADMIN角色，正在添加...");
            user.getRoles().add(adminRole);
            userRepository.save(user);
            logger.info("✓ ADMIN角色已添加");
        } else {
            logger.info("✓ 管理员角色验证成功");
        }
    }
}

