package univ.nc.fx.network.tcp.tchat.server;

import javafx.application.Platform;
import univ.nc.fx.network.tcp.tchat.ITchat;
import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Processus serveur qui ecoute les connexion entrantes,
 * les messages entrant et les rediffuse au clients connectes
 */
public class Server extends Thread implements ITchat{

    /**
     * ip, port et interface serveur donnes en parametres
     */
    private String ip;
    private int port;
    private ServerUI interfaceServeur;

    /**
     * Buffer de type ByteBuffer pour ecrire ou lire dans un channel
     */
    private ByteBuffer buffer = ByteBuffer.allocate(ITchat.BUFFER_SIZE);

    /**
     * un Logger et un FileHandler pour enregistrer des logs dans un fichier
     */
    private Logger logger;
    private FileHandler fh;

    /**
     * ServeSocketChannel pour ecouter et recevoir des connexions clients
     */
    private ServerSocketChannel ssc;

    /**
     * SocketChannel pour conserver les connexions clients
     */
    private SocketChannel sc;

    /**
     * Selecteur pour gerer les channels
     */
    private Selector selector;

    /**
     * Thread Server
     */
    private Server ts;

    /**
     * Constructeur Server
     * 
     * @param interfaceServeur
     * @param ip
     * @param port
     * @throws IOException
     */
    public Server(ServerUI interfaceServeur, String ip, int port)  throws IOException {
        this.ip = ip;
        this.port = port;
        this.interfaceServeur = interfaceServeur;
    }

    /**
     * Demarre un Thread Server afin de ne pas bloquer l'interface serveur
     * 
     * @throws IOException
     */
    public void startServer() throws IOException{
            ts = new Server(interfaceServeur, ip, port);
            ts.start();
    }

    /**
     * Execution du thread Server
     */
    public void run(){
        try{

            //Ouverture des SocketChannel, ServerSocketChannel et Selector
            sc = SocketChannel.open();
            ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            selector = Selector.open();

            //Affectation de l adresse et du port du serveur
            try{
                ssc.bind(new InetSocketAddress(ip, port));
            }catch(BindException e){;}
            
            //Selection de l operation de reception de connexion, sur le selecteur
            ssc.register(selector, SelectionKey.OP_ACCEPT);

            while (interfaceServeur.isRunning()){ 
                
                //Recherche d une operation selectionnee sur le selecteur
                selector.select();

                //Creation de liste d operations selectionnees
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();

                    //Si la connexion est recue
                    if (key.isAcceptable()){

                        //Accepte la connexion
                        sc = ssc.accept();
                        sc.configureBlocking(false);

                        //Mise a jour de l interface serveur en temps reel
                        Platform.runLater(new Runnable(){
                            @Override
                            public void run(){
                                interfaceServeur.log("Connexion reçue depuis " + sc.socket().getRemoteSocketAddress() +".\n");
                            }
                        });

                        //Selection de l operation de lecture du channel, sur le selecteur
                        sc.register(selector, SelectionKey.OP_READ);
                    }

                    //Si un message a ete ecrit dans le channel
                    else if (key.isReadable()){

                        //Recuperation du channel
                        SocketChannel sc = (SocketChannel) key.channel();

                        //Lecture du channel par un ByteBuffer, jusqu a ce qu il soit vide
                        String message = "";
                        while(sc.read(buffer) > 0){
                            buffer.flip();
                            for (int i = 0; i < buffer.limit(); i++){
                                message += (char)buffer.get();
                            }
                            buffer.clear();
                        }

                        //Configuration du logger et du FileHandler
                        logger = Logger.getLogger("MonLog");
                        try{
                            fh = new FileHandler("C:/Users/Mathieu/Desktop/Devoirs/tp.final/TP-Final/logs/log.txt");
                            logger.addHandler(fh);
                            SimpleFormatter formatter = new SimpleFormatter();
                            fh.setFormatter(formatter);

                            //Ajout du message de log
                            logger.info(message);

                        }catch(SecurityException | IOException e){;}

                        //Renvoie le message recu sur chaque channel connecte
                        buffer = ByteBuffer.wrap(message.getBytes());
                        for(SelectionKey sKey : selector.keys()){
                            if(sKey.isValid() && sKey.channel() instanceof SocketChannel){
                                SocketChannel sch=(SocketChannel) sKey.channel();
                                sch.write(buffer);
                                buffer.rewind();
                            }
                        }

                        //Selection de l operation d ecriture dans le channel, sur le selecteur
                        sc.register(selector, SelectionKey.OP_WRITE);

                        //Vider le buffer
                        buffer.clear();
                    }

                    //Si le serveur a ecrit dans un channel
                    else if(key.isWritable()){

                        //Recuperation du channel
                        SocketChannel sc = (SocketChannel) key.channel();

                        //Selection de l operation de lecture du channel, sur le selecteur
                        sc.register(selector, SelectionKey.OP_READ);
                    }
                    iter.remove();
                }
            }

            //Fermeture des ServerSocketChannel, SocketChannel et Selector
            this.sc.close();
            this.ssc.close();
            this.selector.wakeup();
            this.selector.close();
            interfaceServeur.stopServer();
            interfaceServeur.clearLog();
            this.interrupt();

        }catch(IOException | NullPointerException e){
            e.printStackTrace();
            interfaceServeur.clearLog();
        }
    }
}