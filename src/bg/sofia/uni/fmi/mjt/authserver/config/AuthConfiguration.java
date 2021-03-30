package bg.sofia.uni.fmi.mjt.authserver.config;

import java.nio.file.Path;

public class AuthConfiguration {
    private Path usersDatabasePath;
    private Path sessionsDatabasePath;
    private Path auditLogPath;
    private long minimumAdminCount;
    private int maxLoginAttemptFails;
    private int lockTimeout;

    private AuthConfiguration(AuthConfigurationBuilder builder) {
        this.usersDatabasePath = builder.usersDatabasePath;
        this.sessionsDatabasePath = builder.sessionsDatabasePath;
        this.auditLogPath = builder.auditLogPath;
        this.maxLoginAttemptFails = builder.maxLoginAttemptFails;
        this.lockTimeout = builder.lockTimeout;
        this.minimumAdminCount = builder.minimumAdminCount;
    }

    public void setMaxLoginAttemptFails(int maxLoginAttemptFails) {
        if (maxLoginAttemptFails > 0) {
            this.maxLoginAttemptFails = maxLoginAttemptFails;
        }
    }

    public void setLockTimeout(int lockTimeout) {
        if (lockTimeout > 0) {
            this.lockTimeout = lockTimeout;
        }
    }

    public void setUsersDatabasePath(Path usersDatabasePath) {
        this.usersDatabasePath = usersDatabasePath;
    }

    public void setSessionsDatabasePath(Path sessionsDatabasePath) {
        this.sessionsDatabasePath = sessionsDatabasePath;
    }

    public void setMinimumAdminCount(int minimumAdminCount) {
        if (minimumAdminCount > 0) {
            this.minimumAdminCount = minimumAdminCount;
        }
    }

    public Path getUsersDatabasePath() {
        return usersDatabasePath;
    }

    public Path getSessionsDatabasePath() {
        return sessionsDatabasePath;
    }

    public long getMinimumAdminCount() {
        return minimumAdminCount;
    }

    public int getMaxLoginAttemptFails() {
        return maxLoginAttemptFails;
    }

    public int getLockTimeout() {
        return lockTimeout;
    }

    public Path getAuditLogPath() {
        return auditLogPath;
    }

    public static AuthConfigurationBuilder builder(Path usersDatabasePath, Path sessionsDatabasePath,
                                                   Path auditLogPath) {
        return new AuthConfigurationBuilder(usersDatabasePath, sessionsDatabasePath, auditLogPath);
    }

    public static class AuthConfigurationBuilder {
        private final Path usersDatabasePath;
        private final Path sessionsDatabasePath;
        private final Path auditLogPath;

        private long minimumAdminCount = 1;
        private int maxLoginAttemptFails = 3;
        private int lockTimeout = 15 * 60;

        private AuthConfigurationBuilder(Path usersDatabasePath, Path sessionsDatabasePath,
                                         Path auditLogPath) {
            this.usersDatabasePath = usersDatabasePath;
            this.sessionsDatabasePath = sessionsDatabasePath;
            this.auditLogPath = auditLogPath;
        }

        public AuthConfigurationBuilder setMaxLoginAttemptFailures(int maxLoginAttemptFails) {
            if (maxLoginAttemptFails > 0) {
                this.maxLoginAttemptFails = maxLoginAttemptFails;
            }
            return this;
        }

        public AuthConfigurationBuilder setLockTimeout(int lockTimeout) {
            if (lockTimeout > 0) {
                this.lockTimeout = lockTimeout;
            }
            return this;
        }

        public AuthConfigurationBuilder setMinimumAdminCount(long minimumAdminCount) {
            if (minimumAdminCount > 0) {
                this.minimumAdminCount = minimumAdminCount;
            }
            return this;
        }

        public AuthConfiguration build() {
            return new AuthConfiguration(this);
        }
    }
}
