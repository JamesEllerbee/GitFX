package com.jamesellerbee.gitfx.Interfaces;

/**
 * Provides an interface for a provider of properties.
 */
public interface IAppPropertyProvider
{
    /**
     * Get a property matching the key or the default value if there is none.
     *
     * @param key          THe key associated with the value
     * @param defaultValue The default value to return if no key exists or cannot be cast to the type.
     * @param <T>          The type of the value.
     * @return The value matching the type and associated with the key.
     */
    <T> T get(String key, T defaultValue);

    /**
     * Stores the value to the key.
     *
     * @param key   The key of the property.
     * @param value The value to associated with the key.
     */
    void store(String key, Object value);
}
