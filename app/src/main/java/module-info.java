module com.jamesellerbee.gitfx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires org.apache.logging.log4j;
    requires com.google.guice;

    opens com.jamesellerbee.gitfx to javafx.fxml;
    opens com.jamesellerbee.gitfx.Controllers to javafx.fxml, com.google.guice;

    exports com.jamesellerbee.gitfx;
    exports com.jamesellerbee.gitfx.Controllers;
    exports com.jamesellerbee.gitfx.Engines;
    exports com.jamesellerbee.gitfx.Interfaces;

}