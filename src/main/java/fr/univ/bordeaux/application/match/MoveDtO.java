package fr.univ.bordeaux.application.match;

/**
 * A Data Transfer Object (DTO) used to represent a move for the User Interface.
 * <p>This record acts as a lightweight container to transport move data,
 * converting internal board coordinates and piece types into a format
 * easily processable by the display layers (e.g., CLI or GUI).</p>
 *
 * @param from A {@link String} representing the starting coordinate in AbaPro notation (e.g., "A1").
 * @param to A {@link String} representing the destination coordinate in AbaPro notation (e.g., "B2").
 * @param type A {@link String} describing the type of the piece being moved (e.g., "WhiteQueen").
 */
public record MoveDtO(String from, String to, String type) {}