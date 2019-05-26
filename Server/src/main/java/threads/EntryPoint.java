package threads;

import model.ItemFila;
import singletons.F1;

import java.io.DataInputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class EntryPoint implements Runnable{

	protected BlockingQueue<ItemFila> queue;
	private Socket socketClient;
	private byte[] controll;
	private byte[] key;
	private byte[] value;
	boolean everything;

	public EntryPoint(Socket socketClient) {
		this.socketClient = socketClient;
		this.queue = F1.getInstance();
		while(!F1.getFree()) {
		}
	}

	public void run() {
		int lengthWrong;
		int lengthRight;
		int type;
		boolean error;

		try {
			System.out.println("Cliente conectado: " + socketClient.getInetAddress().getHostAddress());

			DataInputStream input = new DataInputStream(socketClient.getInputStream());

			while(true) {
				try {
					error = true;
					everything = false;

					//LENDO COMANDO
					lengthWrong = input.readInt();
					lengthRight = (int) lengthWrong/10;
					type = lengthWrong - (lengthRight*10);

					if( lengthRight > 0 ) {
						controll = new byte[lengthRight];
						input.readFully(controll, 0, lengthRight);
					}

					if( type == 5 ) break;
					everything = ( type == 4 ) ? true : false;

					error = ( type == 1 || type == 4 ) ? error : false;

					//LENDO KEY
					lengthWrong = input.readInt();
					lengthRight = (int) lengthWrong/10;
					type = lengthWrong - (lengthRight*10);

					if( lengthRight > 0 ) {
						key = new byte[lengthRight];
						input.readFully(key, 0, lengthRight);
					}

					error = ( type == 2 ) ? error : false;

					//LENDO VALUE SE EXISTE
					if( everything ) {
						lengthWrong = input.readInt();
						lengthRight = (int) lengthWrong/10;
						type = lengthWrong - (lengthRight*10);

						if( lengthRight > 0 ) {
							value = new byte[lengthRight];
							input.readFully(value, 0, lengthRight);
						}

						error = ( type == 3 ) ? error : false;
					}

					if( !error ) {
						System.out.println("ERRO INESPERADO");
						break;
					}

					ItemFila justProduced = createItemFila();
					queue.put(justProduced);
				}
				catch(Exception e) {
					System.out.println("Erro no entrypoint: " + e.getMessage());
					break;
				}
			}

			System.out.println("Conexão Finalizada!!!");
			input.close();

		}
		catch(Exception e) {
			System.out.println("Erro TRY: " + e.getMessage());
		}
	}

	ItemFila createItemFila(){
		ItemFila item = ( everything ) ? new ItemFila(socketClient, controll, key, value) : new ItemFila(socketClient, controll, key);
		return item;
	}
}