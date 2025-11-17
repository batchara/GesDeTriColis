package com.raoudate.GestionDeTri.Exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;


public enum  BusinessErrorCode {
    // Generic
    NO_CODE(0, NOT_IMPLEMENTED, "No code"),

    // Authentication / account
    INCORRECT_CURRENT_PASSWORD(300, BAD_REQUEST, "Current password is incorrect"),
    NEW_PASSWORD_DOES_NOT_MATCH(301, BAD_REQUEST, "The new password does not match"),
    ACCOUNT_LOCKED(302, FORBIDDEN, "User account is locked"),
    ACCOUNT_DISABLED(303, FORBIDDEN, "User account is disabled"),
    BAD_CREDENTIALS(304, FORBIDDEN, "Login or password is incorrect"),

    // Tokens
    TOKEN_INVALID(4000, BAD_REQUEST, "Invalid token"),
    TOKEN_EXPIRED(4001, GONE, "Token expired"),
    TOKEN_ALREADY_USED(4002, CONFLICT, "Token already used"),

    // Users
    USER_NOT_FOUND(5000, NOT_FOUND, "User not found"),
    USER_INVALID(5001, BAD_REQUEST, "User data is invalid"),
    USER_ALREADY_EXISTS(5002, CONFLICT, "User already exists"),
    DELETED_USER_EXISTS(5003, CONFLICT, "A deleted user exists with this email. Please restore instead of creating new"),

    // Agence
    AGENCE_NOT_FOUND(5100, NOT_FOUND, "Agence not found"),
    AGENCE_INVALID(5101, BAD_REQUEST, "Agence data is invalid"),
    AGENCE_ALREADY_EXISTS(5102, CONFLICT, "Agence already exists"),
    AGENCE_CODE_ALREADY_EXISTS(5103, CONFLICT, "Une agence avec ce code existe déjà"),
    AGENCE_NAME_ALREADY_EXISTS(5104, CONFLICT, "Une agence avec ce nom existe déjà"),

    // Adresse
    ADRESSE_NOT_FOUND(5200, NOT_FOUND, "Adresse not found"),
    ADRESSE_INVALID(5201, BAD_REQUEST, "Adresse data is invalid"),

    // Colis
    COLIS_NOT_FOUND(5300, NOT_FOUND, "Colis not found"),
    COLIS_INVALID(5301, BAD_REQUEST, "Colis data is invalid"),

    // Centre de tri
    CENTRE_NOT_FOUND(5400, NOT_FOUND, "Centre not found"),
    CENTRE_INVALID(5401, BAD_REQUEST, "Centre data is invalid"),

    // Roles / permissions
    ROLE_NOT_FOUND(5500, NOT_FOUND, "Role not found"),
    PERMISSION_NOT_FOUND(5501, NOT_FOUND, "Permission not found"),

    // Boîte Postale
    BOITE_POSTALE_NOT_FOUND(5600, NOT_FOUND, "Boîte postale not found"),
    BOITE_POSTALE_INVALID(5601, BAD_REQUEST, "Boîte postale data is invalid"),
    BOITE_POSTALE_ALREADY_EXISTS(5602, CONFLICT, "Boîte postale already exists"),
    DUPLICATE_ENTRY(5603, CONFLICT, "Duplicate entry"),

    // Authorization / other
    ACCESS_DENIED(6000, FORBIDDEN, "Access denied"),
    VALIDATION_ERROR(6001, BAD_REQUEST, "Validation error"),
    DUPLICATE_RESOURCE(6002, CONFLICT, "Duplicate resource"),
    OPERATION_FAILED(6003, INTERNAL_SERVER_ERROR, "Operation failed"),
    EMAIL_SENDING_FAILED(6004, INTERNAL_SERVER_ERROR, "Failed to send email");



    @Getter
    private int code;
    @Getter
    private String description;
    @Getter
    private HttpStatus httpStatus;


    BusinessErrorCode(int code,HttpStatus httpStatus,String description) {
        this.code = code;
        this.description = description;
        this.httpStatus = httpStatus;
    }
}
