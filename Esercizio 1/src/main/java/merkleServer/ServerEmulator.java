package merkleServer;

import merkleClient.HashUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


/**
 *   ServerEmulator è una classe viene usata come emulatore di un server che riceve
 *   in entrata delle richieste per gli hash in base alla transizione ID che
 *   viene ricevuta da parte di un ipotetico client.
 *
 *   Ai fini dell'esempio, gli hash calcolati dal client saranno entrambi NON validi e
 *   i nodi puramente inventati.
 */


public class ServerEmulator {

    final static int PORT = 2323;

    private static List<String> hashToCheck = new ArrayList<>();

    private static void loadNodes()
    {
        // Nota: per l'esempio gli hash sono dati casualmente
        hashToCheck.add(HashUtil.md5Java("0000102060"));
        hashToCheck.add(HashUtil.md5Java("0000002130"));
        hashToCheck.add(HashUtil.md5Java("0001022030"));
    }

    public static void main(String[] args)
    {

        // Carico i nodi a disposizione
        loadNodes();

        debug("Avvio... server in attesa di connessioni.");

        while(true)
        {

            try (   // Realizzo un server socket pronto all'ascolto
                    ServerSocket server = new ServerSocket(PORT);
                    Socket sock = server.accept())
            {
                // Configuro lo stream per ricevere e inviare
                ObjectOutputStream output = new ObjectOutputStream(sock.getOutputStream());
                BufferedReader buff = new BufferedReader(new InputStreamReader(sock.getInputStream()));


                // Quando ricevo una connessione, attendo 2 secondi prima di cooldown prima di iniziare il task
                // e permettere di vedere il risultato
                try
                {
                    debug("Connessione ricevuta: " + sock.getRemoteSocketAddress());
                    Thread.sleep(2000);
                }
                catch (InterruptedException e)
                {
                    debug(e.getMessage());
                }

                // Attendo richieste
                String incomingRequest = buff.readLine();

                // Se la richiesta è a vuoto, chiudo e rimango in attesa nuovamente
                if (incomingRequest == null)
                {
                    output.close();
                    buff.close();
                    continue;
                }


                debug("<- TransID ricevuto: " + incomingRequest);

                // Invio la lista degli hash richiesti verificando la connessione
                if (sock.isConnected())
                {
                    output.writeObject(hashToCheck);
                    output.flush();
                    debug("-> Hash per il confronto inviati");
                }
                else
                    debug("Errore nell'invio degli hash");


                // Chiudo le risorse create per gli stream
                output.close();
                buff.close();
                debug("Ok... server in attesa di connessioni.");

            }
            catch (IOException e)
            {
                debug("Errore: " + e.getMessage());
            }

        }
    }

    public static void debug(String s)
    {
        System.out.println("[Server] " + s);
    }
}
