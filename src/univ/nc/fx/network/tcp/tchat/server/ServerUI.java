package univ.nc.fx.network.tcp.tchat.server;

import java.io.IOException;

import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Interface graphique du
 *
 * @author mathieu.fabre
 */
public class ServerUI extends Application implements EventHandler {

    /**
     * Indique si le serveur tourne
     */
    private boolean running = false;

    /**
     * Bouton d'action pour lancer ou stoper le serveur
     */
    private Button run;
    private Button stop;

    /**
     * Champs pour choisir l'ip et le port
     */
    private TextField ip;
    private TextField port;

    /**
     * Zone de log et statut
     */
    private TextArea textArea;
    private Label status;

    /**
     * Indique si le serveur tourne
     *
     * @return
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Ajoute un log dans la fenetre de log
     * @param message
     */
    public void log(String message) {
        textArea.appendText(System.getProperty("line.separator") + message);
    }

    /**
     * Vide la zone de log
     */
    public void clearLog(){
        textArea.setText("");
    }

    /**
     * Demarrage de l'interface graphique
     *
     * @param stage
     * @throws Exception
     */
    public void start(Stage stage) throws Exception {

        // Creation du layout principal
        BorderPane borderPane = new BorderPane();
        Scene mainScene = new Scene(borderPane);
        stage.setScene(mainScene);
        stage.setWidth(800.0);
        stage.setHeight(600.0);

        // Creation de la toolbar
        ToolBar toolBar = new ToolBar();
        Label labelIp = new Label("IP : ");
        ip = new TextField("127.0.0.1");
        Label labelPort = new Label("Port : ");
        port = new TextField("6699");
        run = new Button("Run server");
        run.setOnAction(this);
        stop = new Button("Stop server");
        stop.setOnAction(this);
        toolBar.getItems().addAll(labelIp, ip, labelPort, port, run, stop);
        borderPane.setTop(toolBar);

        // Creation du logger
        textArea = new TextArea();
        textArea.setEditable(false);
        borderPane.setCenter(textArea);

        // Creation de la zone de status
        status = new Label("Prêt");
        borderPane.setBottom(status);

        // Initialisation de l etat de l'IHM
        setNonRunningState();

        stage.setTitle("Serveur de tchat");
        stage.show();
    }

    /**
     * Demarrage du serveur
     * @throws IOException
     * @throws NumberFormatException
     */
    public void startServer() throws NumberFormatException, IOException {

        // Changement de l etat de 'IHM
        setRunningState();

        // Changement de l etat du server
        running = true;

        // Demarrage du serveur
        Server server = new Server(this, ip.getText(), Integer.parseInt(port.getText()));
        server.startServer();
    }

    /**
     * Mets l'IHM dans l etat running
     */
    public void setRunningState() {
        ip.setDisable(true);
        port.setDisable(true);
        run.setDisable(true);
        stop.setDisable(false);
        textArea.setDisable(false);
    }

    /**
     * Mets l'IHM dans l etat non running
     */
    public void setNonRunningState() {
        ip.setDisable(false);
        port.setDisable(false);
        run.setDisable(false);
        stop.setDisable(true);
        textArea.setDisable(true);
    }

    /**
     * Arret du server
     * On change simplement le statut
     * et le serveur s'arrete tout seul
     */
    public void stopServer() {

        // On marque l'arret et on attends l'arret du server
        running = false;
        setNonRunningState();
        
    }

    /**
     * Demarrage du client
     *
     * @param args
     */
    public static void main(String [] args) {
        launch(args);
    }

    /**
     * Prise en charge des events
     *
     * @param event
     */
    public void handle(Event event) {

        if (event.getSource() == run) {
            try {
                startServer();
            } catch (NumberFormatException | IOException e) {
                e.printStackTrace();}
        } else if (event.getSource() == stop) {
            stopServer();
        }
    }
}
