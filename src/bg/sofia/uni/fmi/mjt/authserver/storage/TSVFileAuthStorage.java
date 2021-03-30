package bg.sofia.uni.fmi.mjt.authserver.storage;

import bg.sofia.uni.fmi.mjt.authserver.exceptions.UserAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.authserver.user.Authority;
import bg.sofia.uni.fmi.mjt.authserver.user.Email;
import bg.sofia.uni.fmi.mjt.authserver.user.Password;
import bg.sofia.uni.fmi.mjt.authserver.user.User;
import bg.sofia.uni.fmi.mjt.authserver.validation.EmailValidator;
import bg.sofia.uni.fmi.mjt.authserver.validation.PasswordValidator;

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
import java.util.Objects;

public class TSVFileAuthStorage implements AuthStorage {
    private static final Path TEMPORARY_FILE_PATH = Path.of("tmp-users.tsv");
    private static final String DELIMITER = "\t";

    private static final int INDEX_USERNAME = 0;
    private static final int INDEX_PASSWORD_HASH = 1;
    private static final int INDEX_FIRST_NAME = 2;
    private static final int INDEX_LAST_NAME = 3;
    private static final int INDEX_EMAIL = 4;
    private static final int INDEX_AUTHORITY = 5;
    private static final int INDEX_FAILED_LOGIN_ATTEMPTS = 6;
    private static final int INDEX_LOCKED_UNTIL = 7;

    private final Path databasePath;

    public TSVFileAuthStorage(Path databasePath) {
        Objects.requireNonNull(databasePath);
        this.databasePath = databasePath;
    }

    @Override
    public void addUser(User user) throws UserAlreadyExistsException {
        if (getUserByUsername(user.getUsername()) != null) {
            throw new UserAlreadyExistsException("User already found in database");
        }

        try (var bufferedWriter = new BufferedWriter(new FileWriter(databasePath.toString(), true))) {
            bufferedWriter.write(user.toStringWithDelimiter(DELIMITER) + System.lineSeparator());
        } catch (IOException exception) {
            throw new RuntimeException("Could not write new user to file", exception);
        }
    }

    @Override
    public User getUserByUsername(String username) {
        User foundUser = null;

        try (var bufferedReader = new BufferedReader(new FileReader(databasePath.toString()))) {
            String line;

            boolean found = false;
            String[] tokens;
            while ((line = bufferedReader.readLine()) != null && !found) {
                tokens = line.split(DELIMITER);

                if (tokens[INDEX_USERNAME].equals(username)) {
                    Password passwordHash = new Password(tokens[INDEX_PASSWORD_HASH],
                            new PasswordValidator(), Integer.parseInt(tokens[INDEX_FAILED_LOGIN_ATTEMPTS]));
                    Email mail = new Email(tokens[INDEX_EMAIL], new EmailValidator());
                    Authority authority = Authority.fromString(tokens[INDEX_AUTHORITY]);

                    LocalDateTime lockedUntil;
                    try {
                        lockedUntil = LocalDateTime.parse(tokens[INDEX_LOCKED_UNTIL]);
                    } catch (DateTimeParseException exception) {
                        lockedUntil = null;
                    }

                    foundUser = new User(tokens[INDEX_USERNAME], passwordHash, tokens[INDEX_FIRST_NAME],
                            tokens[INDEX_LAST_NAME], mail, authority, lockedUntil);

                    found = true;
                }
            }
        } catch (FileNotFoundException exception) {
            throw new RuntimeException("Cannot open file for reading", exception);
        } catch (IOException exception) {
            throw new RuntimeException("There was an error retrieving user from database", exception);
        }

        return foundUser;
    }

    @Override
    public void replaceUserByUsername(String replacedUsername, User newUser) {
        try (var bufferedReader = new BufferedReader(new FileReader(databasePath.toString()));
             var bufferedWriter = new BufferedWriter(new FileWriter(TEMPORARY_FILE_PATH.toString()))) {

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] tokens = line.split(DELIMITER);

                if (tokens[INDEX_USERNAME].equals(replacedUsername)) {
                    bufferedWriter.write(newUser.toStringWithDelimiter(DELIMITER) + System.lineSeparator());
                } else {
                    bufferedWriter.write(line + System.lineSeparator());
                }
            }

        } catch (FileNotFoundException exception) {
            throw new RuntimeException("File was not found", exception);
        } catch (IOException exception) {
            throw new RuntimeException("There was an error while trying to replace user", exception);
        }

        try {
            Files.move(Path.of(TEMPORARY_FILE_PATH.toString()), databasePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new RuntimeException("Could not rename temporary database file", exception);
        }
    }

    @Override
    public void removeUserByUsername(String username) {
        try (var bufferedReader = new BufferedReader(new FileReader(databasePath.toString()));
             var bufferedWriter = new BufferedWriter(new FileWriter(TEMPORARY_FILE_PATH.toString()))) {

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] tokens = line.split(DELIMITER);

                if (!tokens[INDEX_USERNAME].equals(username)) {
                    bufferedWriter.write(line + System.lineSeparator());
                }
            }

        } catch (FileNotFoundException exception) {
            throw new RuntimeException("File was not found", exception);
        } catch (IOException exception) {
            throw new RuntimeException("There was an error while trying to replace user", exception);
        }

        try {
            Files.move(TEMPORARY_FILE_PATH, databasePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new RuntimeException("Could not rename temporary database file", exception);
        }
    }

    @Override
    public long getAdminCount() {
        try (var bufferedReader = new BufferedReader(new FileReader(databasePath.toString()))) {
            return bufferedReader.lines()
                    .filter(line -> {
                        String[] tokens = line.split(DELIMITER);
                        return tokens[INDEX_AUTHORITY].equalsIgnoreCase(Authority.ADMIN.toString());
                    })
                    .count();
        } catch (FileNotFoundException exception) {
            throw new RuntimeException("File was not found", exception);
        } catch (IOException exception) {
            throw new RuntimeException("Could not rename temporary database file", exception);
        }
    }
}
