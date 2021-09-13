package com.jamesellerbee.gitfx.Controllers;

import com.google.inject.Inject;
import com.jamesellerbee.gitfx.Interfaces.ICommandEngine;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RepositoryController
{
    private static final Logger logger = LogManager.getLogger("com.jamesellerbee.gitfx");

    private static final String STAGED = "Changes to be committed";
    private static final String NOT_STAGED = "Changes not staged for commit";

    private static final String STAGED_HINT_RESTORE_STAGED = "(use \"git restore --staged <file>...\" to unstage)";
    private static final String NOT_STAGED_HINT_ADD = "(use \"git add <file>...\" to update what will be committed)";
    private static final String NOT_STAGED_HINT_RESTORE = "(use \"git restore <file>...\" to discard changes in working directory)";

    private final ChangeListener<String> commandOutputChangeListener = this::commandOutputChangeListener;

    private boolean appendToUnStagedChangesTextArea = false;
    private boolean appendToStagedChangesTextArea = false;

    @Inject
    private ICommandEngine commandEngine;

    // region FXML Fields

    @FXML
    private TextArea unstagedChangesTextArea;
    @FXML
    private TextArea stagedChangesTextArea;

    // endregion

    public void startListeningToCommandOutput()
    {
        commandEngine.getCommandOutputProperty().addListener(commandOutputChangeListener);
    }

    public void stopListeningToCommandOutput()
    {
        commandEngine.getCommandOutputProperty().removeListener(commandOutputChangeListener);
    }

    public void onAction_Commit(ActionEvent actionEvent)
    {
        // FUTURE: check application properties if user wants to open their text editor

    }

    public void onAction_Status(ActionEvent actionEvent)
    {
        appendToUnStagedChangesTextArea = false;
        appendToStagedChangesTextArea = false;
        unstagedChangesTextArea.clear();
        stagedChangesTextArea.clear();
        commandEngine.send("git status");
    }

    public void onAction_Quit(ActionEvent actionEvent)
    {
        logger.debug("Exiting the application.");
        Platform.exit();
    }

    private void commandOutputChangeListener(ObservableValue<? extends String> observable, String oldValue, String newValue)
    {
        if (newValue == null)
        {
            return;
        }
        try
        {
            // just put everything in the both text areas for now
            // TODO: parse relevant into into text areas


            if (appendToUnStagedChangesTextArea &&
                !newValue.contains(NOT_STAGED_HINT_ADD) &&
                !newValue.contains(NOT_STAGED_HINT_RESTORE))
            {
                unstagedChangesTextArea.appendText(String.format("%s\n", newValue));
            }

            if (appendToStagedChangesTextArea &&
                !newValue.contains(STAGED_HINT_RESTORE_STAGED) &&
                !newValue.contains(NOT_STAGED))
            {
                stagedChangesTextArea.appendText(String.format("%s\n", newValue));
            }

            if (newValue.contains(NOT_STAGED))
            {
                appendToStagedChangesTextArea = false;
                appendToUnStagedChangesTextArea = true;
            }

            if (newValue.contains(STAGED))
            {
                appendToStagedChangesTextArea = true;
                appendToUnStagedChangesTextArea = false;
            }
        }
        catch (Exception e)
        {
            // catch errors so change listener does not collapse
            logger.error("An error occurred while listening to command out.");
            logger.debug(e);
        }
    }
}
