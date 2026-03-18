package fr.univ.bordeaux.technical.io;

import java.io.IOException;

/**
 * A generic interface for serializing objects into files.
 *
 * @param <T> The type of object to be serialized.
 */
public interface Serializer<T> {

    /**
     * Saves the state of the given object to the specified file.
     *
     * @param object   The object to serialize.
     * @param filePath The destination path where the file will be saved.
     * @throws IOException If an error occurs while writing to the file.
     */
    void save(T object, String filePath) throws IOException;
}