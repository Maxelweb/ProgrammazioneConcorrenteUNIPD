package merkleClient;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static merkleClient.HashUtil.md5Java;

public class MerkleValidityRequest {

	/**
	 * IP address of the authority
	 * */
	private final String authIPAddr;
	/**
	 * Port number of the authority
	 * */
	private final int  authPort;
	/**
	 * Hash value of the merkle tree root. 
	 * Known before-hand.
	 * */
	private final String mRoot;
	/**
	 * List of transactions this client wants to verify 
	 * the existence of.
	 * */
	private List<String> mRequests;

	/**
	 * Sole constructor of this class - marked private.
	 * */
	private MerkleValidityRequest(Builder b){
		this.authIPAddr = b.authIPAddr;
		this.authPort = b.authPort;
		this.mRoot = b.mRoot;
		this.mRequests = b.mRequest;
	}

	/**
	 * <p>Method implementing the communication protocol between the client and the authority.</p>
	 * <p>The steps involved are as follows:</p>
	 * 		<p>0. Opens a connection with the authority</p>
	 * 	<p>For each transaction the client does the following:</p>
	 * 		<p>1.: asks for a validityProof for the current transaction</p>
	 * 		<p>2.: listens for a list of hashes which constitute the merkle nodes contents</p>
	 * 	<p>Uses the utility method {@link #isTransactionValid(String, String, List<String>) isTransactionValid} </p>
	 * 	<p>method to check whether the current transaction is valid or not.</p>
	 * */
	public Map<Boolean, List<String>> checkWhichTransactionValid() throws IOException {

		Map<Boolean, List<String>> risultati = new HashMap<>();

		List<String> transValid = new ArrayList<>();
		List<String> transInvalid = new ArrayList<>();
		List<String> HashFromServer = null;

		risultati.put(true, transValid);
		risultati.put(false, transInvalid);

		debug("Connessione al server [" + authIPAddr + ":" + authPort + "]...");

		// Passo 0.
		// Chiedo al server ulteriori informazioni

		// Prendo ciascuna singola richiesta che dovrò analizzare
		for(String request : mRequests)
		{
			try(Socket sock = new Socket(authIPAddr, authPort))
			{
				PrintWriter outputFromSocket = new PrintWriter(sock.getOutputStream(), true);
				ObjectInputStream inputFromSocket = new ObjectInputStream(sock.getInputStream());

				debug("Richiesta: " + request);

				// Invio al server le mie richieste
				outputFromSocket.println(request);

				// Ricezione degli hash da confrontare
				try
				{
					HashFromServer = (ArrayList<String>) inputFromSocket.readObject();
					debug("Ricevuti hash: " + HashFromServer);
				}
				catch(ClassNotFoundException e)
				{
					debug("Errore di comunicazione: ");
					System.out.print(e.getMessage());
				}

				// Controllo la validità della transazione
				if(HashFromServer != null && isTransactionValid(request, HashFromServer))
				{
					transValid.add(request);
					debug("Transazione valida!");
				}
				else
				{
					transInvalid.add(request);
					debug("Transazione non valida!");
				}
			}
			catch (IOException exc)
			{
				debug("Connessione fallita!");
				throw exc;
			}
		}

		return risultati;

	}

	/**
	 * 	Checks whether a transaction 'merkleTx' is part of the merkle tree.
	 *
	 *  @param merkleTx String: the transaction we want to validate
	 *  @param merkleNodes String: the hash codes of the merkle nodes required to compute
	 *  the merkle root
	 *
	 *  @return: boolean value indicating whether this transaction was validated or not.
	 * */
	private boolean isTransactionValid(String merkleTx, List<String> merkleNodes) {
		String HashedString = merkleTx;
		for(String singleNode : merkleNodes)
		{
			HashedString = md5Java(HashedString + singleNode);
			debug("Hash calcolato: " + HashedString);
		}
		debug(mRoot);
		return mRoot.equals(HashedString);
	}


	/**
	 * Classe ausiliaria per debugging del client
	 */

	public static void debug(String s)
	{
		System.out.println("[Client] "+ s);
	}


	/**
	 * Builder for the MerkleValidityRequest class. 
	 * */
	public static class Builder {
		private String authIPAddr;
		private int authPort;
		private String mRoot;
		private List<String> mRequest;

		public Builder(String authorityIPAddr, int authorityPort, String merkleRoot) {
			this.authIPAddr = authorityIPAddr;
			this.authPort = authorityPort;
			this.mRoot = merkleRoot;
			mRequest = new ArrayList<>();
		}

		public Builder addMerkleValidityCheck(String merkleHash) {
			mRequest.add(merkleHash);
			return this;
		}

		public MerkleValidityRequest build() {
			return new MerkleValidityRequest(this);
		}
	}
}