package com.jamesellerbee.gitfx.Interfaces;

import javafx.beans.property.StringProperty;

/**
 * Provides an interface for a class to interact with command line interface.
 */
public interface ICommandEngine
{
    /**
     * Sends a command to be executed.
     * @param command The command to execute.
     */
    void send(String command);

    StringProperty getCommandOutputProperty();

    void shutdown();
}
