package bg.sofia.uni.fmi.mjt.authserver.auth;

import bg.sofia.uni.fmi.mjt.authserver.config.AuthConfiguration;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.AuthenticationException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.LockedUserException;
import bg.sofia.uni.fmi.mjt.authserver.exceptions.UserDoesNotExistException;
import bg.sofia.uni.fmi.mjt.authserver.storage.AuthStorage;
import bg.sofia.uni.fmi.mjt.authserver.tasks.InvalidateSessionTask;
import bg.sofia.uni.fmi.mjt.authserver.user.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TSVFileSessionManager implements AuthSessionManager {
    private static final int EXECUTOR_THREAD_COUNT = 5;
    private static final String TEMPORARY_FILE_NAME = "tmp-sessions.tsv";
    private static final String DELIMITER = "\t";

    private static final int INDEX_ID = 0;
    private static final int INDEX_USERNAME = 1;
    private static final int INDEX_EXPIRES_AT = 2;

    private final AuthConfiguration configuration;
    private final ScheduledExecutorService executorService;
    private final AuthStorage storage;

    public TSVFileSessionManager(AuthConfiguration configuration, AuthStorage storage) {
        this.configuration = configuration;
        this.storage = storage;
        executorService = Executors.newScheduledThreadPool(EXECUTOR_THREAD_COUNT);

        removeExpiredSessions();
    }

    @Override
    public Session logUserIn(String username) throws AuthenticationException {
        User foundUser = storage.getUserByUsername(username);

        if (foundUser == null) {
            throw new UserDoesNotExistException("No user with that username was found in the database");
        }

        if (foundUser.isLocked()) {
            throw new LockedUserException("Cannot log user in: account is locked");
        }

        Session oldSession = getSessionByUsername(username);

        Session newSession = Session.generateForUsername(foundUser.getUsername());
        scheduleSessionInvalidation(newSession);

        String sessionsDatabaseFilename = configuration.getSessionsDatabasePath().toString();
        if (oldSession == null) {
            try (var bufferedWriter = new BufferedWriter(new FileWriter(sessionsDatabaseFilename, true))) {
                bufferedWriter.write(String.join(DELIMITER,
                        newSession.getId().toString(),
                        newSession.getLoggedInUsername(),
                        newSession.getExpiresAt().toString())
                    + System.lineSeparator());
            } catch (FileNotFoundException exception) {
                throw new RuntimeException("Could not find file", exception);
            } catch (IOException exception) {
                throw new RuntimeException("There was an error updating the sessions database", exception);
            }

            return newSession;
        }

        try (var bufferedReader = new BufferedReader(new FileReader(sessionsDatabaseFilename));
             var bufferedWriter = new BufferedWriter(new FileWriter(TEMPORARY_FILE_NAME))) {
            String line;
            String[] tokens;

            while ((line = bufferedReader.readLine()) != null) {
                tokens = line.split(DELIMITER);

                if (tokens[INDEX_USERNAME].equals(username)) {
                    bufferedWriter.write(String.join(DELIMITER,
                            newSession.getId().toString(),
                            newSession.getLoggedInUsername(),
                            newSession.getExpiresAt().toString())
                        + System.lineSeparator());
                } else {
                    bufferedWriter.write(line + System.lineSeparator());
                }
            }
        } catch (FileNotFoundException exception) {
            throw new RuntimeException("Could not find file", exception);
        } catch (IOException exception) {
            throw new RuntimeException("There was an error updating the sessions database", exception);
        }

        try {
            Files.move(Path.of(TEMPORARY_FILE_NAME), Path.of(sessionsDatabaseFilename),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new RuntimeException("Could not rename temporary database file", exception);
        }

        return newSession;
    }

    @Override
    public User getUserBySession(Session session) {
        User foundUser = null;

        String sessionsDatabaseFilename = configuration.getSessionsDatabasePath().toString();
        try (var bufferedReader = new BufferedReader(new FileReader(sessionsDatabaseFilename))) {
            String line;
            String[] tokens;

            while ((line = bufferedReader.readLine()) != null && foundUser == null) {
                tokens = line.split(DELIMITER);

                if (tokens[INDEX_ID].equals(session.getId().toString())) {
                    foundUser = storage.getUserByUsername(tokens[INDEX_USERNAME]);
                }
            }
        } catch (FileNotFoundException exception) {
            throw new RuntimeException("Session database file was not found", exception);
        } catch (IOException exception) {
            throw new RuntimeException("There was an error while trying to read from session database file",
                    exception);
        }

        return foundUser;
    }

    @Override
    public Session getSessionByUsername(String username) {
        Session foundSession = null;

        String sessionsDatabaseFilename = configuration.getSessionsDatabasePath().toString();
        try (var bufferedReader = new BufferedReader(new FileReader(sessionsDatabaseFilename))) {
            String line;
            String[] tokens;

            while ((line = bufferedReader.readLine()) != null && foundSession == null) {
                tokens = line.split(DELIMITER);

                if (tokens[INDEX_USERNAME].equals(username)) {
                    foundSession = new Session(UUID.fromString(tokens[INDEX_ID]),
                            tokens[INDEX_USERNAME],
                            LocalDateTime.parse(tokens[INDEX_EXPIRES_AT]));
                }
            }
        } catch (FileNotFoundException exception) {
            throw new RuntimeException("Session database file was not found", exception);
        } catch (IOException exception) {
            throw new RuntimeException("There was an error while trying to read from session database file",
                    exception);
        }

        return foundSession;
    }

    @Override
    public void invalidateSession(Session session) {
        String sessionsDatabaseFilename = configuration.getSessionsDatabasePath().toString();

        try (var bufferedReader = new BufferedReader(new FileReader(sessionsDatabaseFilename));
             var bufferedWriter = new BufferedWriter(new FileWriter(TEMPORARY_FILE_NAME))) {
            String line;
            String[] tokens;

            while ((line = bufferedReader.readLine()) != null) {
                tokens = line.split(DELIMITER);

                if (!tokens[INDEX_ID].equals(session.getId().toString())) {
                    bufferedWriter.write(line + System.lineSeparator());
                }
            }
        } catch (FileNotFoundException exception) {
            throw new RuntimeException("Could not find file", exception);
        } catch (IOException exception) {
            throw new RuntimeException("There was an error updating the sessions database", exception);
        }

        try {
            Files.move(Path.of(TEMPORARY_FILE_NAME), Path.of(sessionsDatabaseFilename),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new RuntimeException("There was an error while trying to rename"
                    + "temporary sessions database file", exception);
        }
    }

    @Override
    public void scheduleSessionInvalidation(Session session) {
        executorService.schedule(new InvalidateSessionTask(this, session),
                ChronoUnit.SECONDS.between(LocalDateTime.now(), session.getExpiresAt()), TimeUnit.SECONDS);
    }

    private void removeExpiredSessions() {
        String sessionsDatabaseFilename = configuration.getSessionsDatabasePath().toString();

        LocalDateTime compareAgainst = LocalDateTime.now();

        try (var bufferedReader = new BufferedReader(new FileReader(sessionsDatabaseFilename));
             var bufferedWriter = new BufferedWriter(new FileWriter(TEMPORARY_FILE_NAME))) {
            String line;
            String[] tokens;

            while ((line = bufferedReader.readLine()) != null) {
                tokens = line.split(DELIMITER);
                LocalDateTime expiresAt;

                try {
                    expiresAt = LocalDateTime.parse(tokens[INDEX_EXPIRES_AT]);
                } catch (DateTimeParseException exception) {
                    // skip
                    continue;
                }

                if (expiresAt.isAfter(compareAgainst)) {
                    bufferedWriter.write(line + System.lineSeparator());

                    UUID id = UUID.fromString(tokens[INDEX_ID]);
                    Session session = new Session(id, tokens[INDEX_USERNAME], expiresAt);

                    scheduleSessionInvalidation(session);
                }
            }
        } catch (FileNotFoundException exception) {
            throw new RuntimeException("Could not find file", exception);
        } catch (IOException exception) {
            throw new RuntimeException("There was an error updating the sessions database", exception);
        }

        try {
            Files.move(Path.of(TEMPORARY_FILE_NAME), Path.of(sessionsDatabaseFilename),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new RuntimeException("There was an error while trying to rename file", exception);
        }
    }
}
