package com.jamesellerbee.gitfx.Controllers;

import com.google.inject.Inject;
import com.jamesellerbee.gitfx.GitFxApplication;
import com.jamesellerbee.gitfx.Interfaces.ICommandEngine;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.util.Optional;

public class RepositoryController
{
    private static final Logger logger = LogManager.getLogger("com.jamesellerbee.gitfx");

    private static final String STAGED = "Changes to be committed";
    private static final String NOT_STAGED = "Changes not staged for commit";
    private static final String NO_CHANGED_ADDED = "no changes added to commit";

    private static final String STAGED_HINT_RESTORE_STAGED = "(use \"git restore --staged <file>...\" to unstage)";
    private static final String NOT_STAGED_HINT_ADD = "(use \"git add <file>...\" to update what will be committed)";
    private static final String NOT_STAGED_HINT_RESTORE = "(use \"git restore <file>...\" to discard changes in working directory)";
    private static final String BRANCH_AHEAD = "Your branch is ahead of";
    private static final String GIT_PUSH_HINT = "\"git push\"";


    private Stage repositoryStage;
    private final ChangeListener<String> commandOutputChangeListener = this::statusOutputChangeListener;


    private boolean appendToUnStagedChangesTextArea = false;
    private boolean appendToStagedChangesTextArea = false;

    @Inject
    private ICommandEngine commandEngine;

    // region FXML Fields

    @FXML
    private TextArea unstagedChangesTextArea;
    @FXML
    private TextArea stagedChangesTextArea;
    @FXML
    private Label currentBranch;


    // endregion

    // region Public Methods

    public void setStage(Stage stage)
    {
        repositoryStage = stage;
    }

    public void setCurrentBranchInfo()
    {
        StringBuilder stringBuilder = new StringBuilder();
        ChangeListener<String> currentBranchChangeListener = ((observable, oldValue, newValue) ->
        {
            try
            {
                if (!newValue.contains("git rev-parse --abbrev-ref HEAD") &&
                    !newValue.contains("git checkout") &&
                    !newValue.contains(BRANCH_AHEAD) &&
                    !newValue.contains(GIT_PUSH_HINT))
                {
                    stringBuilder.append(newValue);
                }
            }
            catch (Exception e)
            {
                // catch errors so that this listener doesn't collapse
                logger.error("Error occurred while getting current branch information");
                logger.debug(e);
            }
        });

        commandEngine.getCommandOutputProperty().addListener(currentBranchChangeListener);
        commandEngine.send("git rev-parse --abbrev-ref HEAD");
        thenWait(100);
        currentBranch.setText(stringBuilder.toString());
        commandEngine.getCommandOutputProperty().removeListener(currentBranchChangeListener);
        updateInfoTextAreas();
    }

    public void startListeningToCommandOutput()
    {
        commandEngine.getCommandOutputProperty().addListener(commandOutputChangeListener);
    }

    public void stopListeningToCommandOutput()
    {
        commandEngine.getCommandOutputProperty().removeListener(commandOutputChangeListener);
    }

    public void updateInfoTextAreas()
    {
        appendToUnStagedChangesTextArea = false;
        appendToStagedChangesTextArea = false;
        unstagedChangesTextArea.clear();
        stagedChangesTextArea.clear();
        commandEngine.send("git status");
    }

    public void onAction_Commit(ActionEvent actionEvent)
    {
        // todo FUTURE: check application properties if user wants to open their text editor
        var commitMessageDialog = new TextInputDialog("Enter commit message.");
        commitMessageDialog.setHeaderText("Enter commit message");

        var resultOptional = commitMessageDialog.showAndWait();
        logger.debug("commit message dialog result: {}", resultOptional);
        if (resultOptional.isPresent())
        {
            commandEngine.send(String.format("git commit -m \"%s\"", resultOptional.get()));
            updateInfoTextAreas();
        }
    }

    public void onAction_Status(ActionEvent actionEvent)
    {
        updateInfoTextAreas();
    }

    public void onAction_Quit(ActionEvent actionEvent)
    {
        logger.debug("Exiting the application.");
        Platform.exit();
    }

    public void onAction_Fetch(ActionEvent actionEvent)
    {
        commandEngine.send("git fetch --all -a");
        updateInfoTextAreas();
    }

    public void onAction_Push(ActionEvent actionEvent)
    {
        //todo: make this safer by resolving current branch and adding origin
        commandEngine.send("git push");
        updateInfoTextAreas();
    }

    public void onAction_DebugCommandOutput(ActionEvent actionEvent)
    {
        logger.trace("debug command output clicked");

        // create new window
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/debugCommandOutputView.fxml"));
            Parent root = fxmlLoader.load();

            DebugCommandOutputController debugCommandOutputController = fxmlLoader.getController();
            GitFxApplication.getInjector().injectMembers(debugCommandOutputController);
            debugCommandOutputController.startListeningForCommandOutput();

            Scene scene = new Scene(root, 500, 500);
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());

            Stage stage = new Stage();
            stage.setTitle("GitFx command output (debug)");
            stage.setScene(scene);
            stage.show();

            stage.setOnHiding((value) -> debugCommandOutputController.stopListeningForCommandOutput());
        }
        catch (Exception e)
        {
            logger.error("There was an error creating the debug command output window.");
            logger.debug(e);
        }
    }

    public void onAction_Stage(ActionEvent actionEvent)
    {
        // todo: open dialog that allows user to to select files to stage with select all option
        commandEngine.send("git add -A");
        updateInfoTextAreas();
    }

    public void onAction_StashList(ActionEvent actionEvent)
    {
        commandEngine.send("git stash list");
    }

    public void onAction_StashPush(ActionEvent actionEvent)
    {
        var stashMessageDialog = new TextInputDialog("Enter stash message.");
        stashMessageDialog.setHeaderText("Enter stash message");

        var stashCommandStringBuilder = new StringBuilder("git stash push");

        var resultOptional = stashMessageDialog.showAndWait();
        if (resultOptional.isPresent())
        {
            stashCommandStringBuilder.append(" -m ");
            stashCommandStringBuilder.append("\"");
            stashCommandStringBuilder.append(resultOptional.get());
            stashCommandStringBuilder.append("\"");
        }

        commandEngine.send(stashCommandStringBuilder.toString());
        updateInfoTextAreas();
    }

    public void onAction_StashPop(ActionEvent actionEvent)
    {
        commandEngine.send("git stash pop");
    }

    public void onAction_Menu(ActionEvent actionEvent)
    {
        // todo: open settings window
    }

    public void onAction_Close(ActionEvent actionEvent)
    {
        repositoryStage.close();
    }

    public void onAction_NewBranch(ActionEvent actionEvent)
    {
        // todo: check that the user doesn't have any uncommitted changes before checking out, warn and ask if they want to continue
        if (!hasUnstagedChanges())
        {
            var branchMessageDialog = new TextInputDialog("Enter new branch name.");
            branchMessageDialog.setTitle("Checkout to new branch");
            branchMessageDialog.setHeaderText("Enter new branch name");

            var resultOptional = branchMessageDialog.showAndWait();
            if (resultOptional.isPresent())
            {
                commandEngine.send(String.format("git checkout -b %s", resultOptional.get()));
                setCurrentBranchInfo();
            }
            else
            {
                logger.debug("Branch message dialog returned no result.");
            }
        }
    }

    public void onAction_ExistingBranch(ActionEvent actionEvent)
    {
        if(!hasUnstagedChanges())
        {
            // todo: open a window that shows all the branches and checkout to the branch the user selects instead of having them type it in.s
            var branchMessageDialog = new TextInputDialog("Enter branch name.");
            branchMessageDialog.setTitle("Checkout to existing branch");

            var stringBuilder = new StringBuilder();
            ChangeListener<String> branchOutputChangeListener = ((observable, oldValue, newValue) ->
            {
                if (!newValue.contains("git branch -a"))
                {
                    stringBuilder.append(newValue);
                    stringBuilder.append("\n");
                    logger.trace("new value: {}, string builder value {}", newValue, stringBuilder.toString());
                }
            });

            commandEngine.getCommandOutputProperty().addListener(branchOutputChangeListener);
            commandEngine.send("git branch -a");

            thenWait(100);

            branchMessageDialog.setHeaderText(stringBuilder.toString());

            Optional<String> resultOptional = branchMessageDialog.showAndWait();
            commandEngine.getCommandOutputProperty().removeListener(branchOutputChangeListener);
            if (resultOptional.isPresent())
            {
                // todo: check that the user doesn't have any uncommitted changes before checking out, warn and ask if they want to continue
                commandEngine.send(String.format("git checkout %s", resultOptional.get()));
                setCurrentBranchInfo();
            }
            else
            {
                logger.debug("Branch message dialog returned no result.");
            }

            updateInfoTextAreas();
        }
    }

    // endregion

    // region Private Methods

    private boolean hasUnstagedChanges()
    {
        var result = true;
        if (unstagedChangesTextArea.getText().isBlank())
        {
            result = false;
        }
        else
        {
            logger.warn("Unstaged changes text area is not blank");
            var alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("Cannot checkout to branch, there are unstaged changes. Stage or stash your changes and refresh before attempting to checkout to a new branch.");
            alert.show();
        }

        return result;
    }

    private void statusOutputChangeListener(ObservableValue<? extends String> observable, String oldValue, String newValue)
    {
        if (newValue == null)
        {
            return;
        }
        try
        {
            if (appendToUnStagedChangesTextArea &&
                !newValue.contains(NOT_STAGED_HINT_ADD) &&
                !newValue.contains(NOT_STAGED_HINT_RESTORE) &&
                !newValue.contains(NO_CHANGED_ADDED))
            {
                unstagedChangesTextArea.appendText(String.format("%s\n", newValue.replace("\t", "")));
            }

            if (appendToStagedChangesTextArea &&
                !newValue.contains(STAGED_HINT_RESTORE_STAGED) &&
                !newValue.contains(NOT_STAGED))
            {
                stagedChangesTextArea.appendText(String.format("%s\n", newValue.replace("\t", "")));
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

    private void thenWait(long delay)
    {
        try
        {
            // brief suspend while information about the branches are gathered
            Thread.sleep(200);
        }
        catch (Exception e)
        {
            logger.error("Could not sleep.");
            logger.debug(e);
        }
    }

    // endregion
}
