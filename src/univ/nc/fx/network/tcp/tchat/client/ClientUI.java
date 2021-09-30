package univ.nc.fx.network.tcp.tchat.client;

import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Random;

/**
 * Interface graphique du client
 *
 * @author mathieu.fabre
 */
public class ClientUI extends Application implements EventHandler {

    /**
     * Champs pour choisir l ip, le port et le pseudo
     */
    private TextField ip;
    private TextField port;
    private TextField nickname;

    /**
     * Boutons d action pour lancer ou arreter le client 
     */
    private Button connect;
    private Button disconnect;

    /**
     * Zones de log, d ecriture et de statut
     */
    private TextArea textArea;
    private TextField input;
    private Label status;

    /**
     * Le thread client
     */
    private Client client;

    /**
     * Indique si le client tourne
     */
    private boolean running = false;

    public void start(Stage stage) throws Exception {

        // Border pane et scene
        BorderPane borderPane = new BorderPane();
        Scene scene = new Scene(borderPane);
        stage.setScene(scene);

        // Zone haute pour la connection
        ToolBar toolBar = new ToolBar();
        ip = new TextField("127.0.0.1");
        port = new TextField("6699");
        nickname = new TextField("user" + (new Random().nextInt(100)));
        connect = new Button("Connect");
        connect.setOnAction(this);
        disconnect = new Button("Disconnect");
        disconnect.setOnAction(this);
        toolBar.getItems().addAll(ip, port, nickname, connect, disconnect);
        borderPane.setTop(toolBar);

        // Zone centrale de log de tchat
        textArea = new TextArea();
        borderPane.setCenter(textArea);

        // Zone basse pour la zone de texte et le statut
        VBox bottomBox = new VBox();
        status = new Label("Prêt.");
        input = new TextField();
        input.addEventFilter(KeyEvent.KEY_RELEASED, this);
        bottomBox.getChildren().addAll(input, status);
        borderPane.setBottom(bottomBox);

        // Statut initial déconnecte
        setDisconnectedState();

        stage.setTitle("Client de tchat");
        stage.show();
    }

    /**
     * Mets l IHM dans le statut deconnecte
     */
    public void setDisconnectedState() {
        ip.setDisable(false);
        port.setDisable(false);
        connect.setDisable(false);
        disconnect.setDisable(true);
        input.setDisable(true);
    }

    /**
     * Mets l IHM dans le statut connecte
     */
    public void setConnectedState() {
        ip.setDisable(true);
        port.setDisable(true);
        connect.setDisable(true);
        disconnect.setDisable(false);
        input.setDisable(false);
    }

    /**
     * Indique si le client tourne
     *
     * @return
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Ajout de message dans le log
     *
     * @param message
     */
    public void appendMessage(String message) {
        textArea.appendText(message);
    }

    /**
     * Vide la zone de log
     */
    public void clearTextArea(){
        textArea.setText("");
    }

    /**
     * Change le message de statut
     *
     * @param message
     */
    public void setStatus(String message) {
        status.setText(message);
    }

    /**
     * Connexion au serveur
     * @throws IOException
     * @throws NumberFormatException
     */
    public void connectToServer() throws NumberFormatException, IOException {

        if (ip.getText().trim().length() == 0) {
            setStatus("Veuillez entrer une adresse IP valide");
            return;
        }

        if (port.getText().trim().length() == 0) {
            setStatus("Veuillez entrer un port valide");
            return;
        }

        if (nickname.getText().trim().length() == 0) {
            setStatus("Veuillez entrer un nickname valide");
            return;
        }
        // Changement de l etat du client
        running = true;

        // Demarrage de la connexion au serveur
        client = new Client(this, ip.getText(), Integer.parseInt(port.getText()), nickname.getText());
        client.startClient();

        // Changement d etat de l'IHM
        setConnectedState();
    }

    /**
     * Deconnexion
     * on passe le statut a false et on attends
     * que le thread se deconnecte
     */
    public void disconnectFromServer() {
        running = false;
        setDisconnectedState();
        this.clearTextArea();
        this.setStatus("Prêt.");
    }

    /**
     * Prise en charge des events
     *
     * @param event
     */
    public void handle(Event event) {

        if (event.getSource() == connect) {
            try {
                connectToServer();
            } catch (NumberFormatException | IOException e) {
                e.printStackTrace();
            }
        } else if (event.getSource() == disconnect) {
            disconnectFromServer();
        } else if (event.getSource() == input) {
            try {
                processEnter((KeyEvent)event);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } 
    }

    /**
     * Envoi le message si l utilisateur
     * appui sur la touche entree
     *
     * @param event
     * @throws IOException
     */
    public void processEnter(KeyEvent event) throws IOException {

        // Envoi du texte si on appui sur entree et que le contenu n est pas vide
        if (event.getCode() == KeyCode.ENTER && input.getText().trim().length() > 0) {
            client.addMessage(input.getText() + System.getProperty("line.separator"));
            input.setText("");
        }
    }

    /**
     * Demarrage du client
     *
     * @param args
     */
    public static void main(String [] args) {
        launch(args);
    }
}
