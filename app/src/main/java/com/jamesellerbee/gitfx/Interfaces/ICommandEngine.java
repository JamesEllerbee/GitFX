package com.jamesellerbee.gitfx.Interfaces;

import javafx.beans.property.StringProperty;

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
