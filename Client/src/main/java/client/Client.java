package client;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.copycat.client.CopycatClient;
import command.CreateCommand;
import command.DeleteCommand;
import command.ReadQuery;
import command.UpdateCommand;

public class Client {
    String welcome = "Esse software se baseia em um banco de dados chave valor.\n Chave: Inteiro(Infinito) \n Valor: Bytes\n";
    String options = "Opções disponivéis: \n- Create chave valor \n- Read chave \n- Update chave valor \n- Delete chave \n- Help \n- Sair \n";
    String read = "Digite uma opção válida: ";
    String invalid = "Opção inválida!!!";
    String done = "Conexão concluída!!!\n";
    String quit = "Conexão encerrada!!!";
    String close = "Saindo....";
    String option;
    Controll command;
    String key;
    String value;
    int size;
    int spaceFirst;
    int spaceSecond;
    BigInteger keyBigInteger;

    /**
     * Construct ClientGrpc connecting to HelloWorld server at {@code host:port}.
     */
    public Client(String[] args) {
        List<Address> addresses = new LinkedList<>();

        CopycatClient.Builder builder = CopycatClient.builder()
                .withTransport(NettyTransport.builder()
                        .withThreads(4)
                        .build());
        CopycatClient client = builder.build();

        for (int i = 0; i < args.length; i += 2) {
            Address address = new Address(args[i], Integer.parseInt(args[i + 1]));
            addresses.add(address);
        }

        // 127.0.0.1 5000 127.0.0.1 5001 127.0.0.1 5002
        CompletableFuture<CopycatClient> future = new CompletableFuture<>();
        try{
            future = client.connect(addresses);
        } catch (CompletionException e){
            e.printStackTrace();
        }
        future.join();
        // String value = "DEU CERTO";
        // int dez = 10;
        // BigInteger key = BigInteger.valueOf(dez);

        Scanner scanner = new Scanner(System.in);

        System.out.println(welcome);
        System.out.println(options);

        while (true) {
            System.out.print(read);
            option = scanner.nextLine();

            try {
                option = option.toUpperCase();

                if (option.equals("SAIR")) {
                    System.out.println(close);
                    break;
                }

                if (option.equals("HELP")) {
                    System.out.println(options);
                    continue;
                }

                size = option.length();
                spaceFirst = option.indexOf(" ");

                if (spaceFirst == -1) {
                    throw new Exception();
                }

                spaceSecond = option.indexOf(" ", (spaceFirst + 1));
                command = Controll.valueOf(option.substring(0, spaceFirst));

                if ((command.equals(Controll.CREATE) || command.equals(Controll.UPDATE)) && (spaceSecond == -1)) {
                    throw new Exception();
                }

                if ((command.equals(Controll.READ) || command.equals(Controll.DELETE)) && (spaceSecond != -1)) {
                    throw new Exception();
                }

                key = option.substring((spaceFirst + 1), ((spaceSecond == -1) ? size : spaceSecond));
                keyBigInteger = new BigInteger(key);

                if (command.equals(Controll.CREATE)) {
                    value = option.substring((spaceSecond + 1), size);
                    byte[] messageBytesValue = value.getBytes();
                    client.submit(new CreateCommand(keyBigInteger, messageBytesValue)).thenAccept(result -> {
                        System.out.println("Create com " + keyBigInteger + messageBytesValue + " deu: " + result);
                    });
                    // new Thread( new Create(host, port, keyBigInteger, messageBytesValue, true)
                    // ).start();
                    continue;
                }

                if (command.equals(Controll.UPDATE)) {
                    value = option.substring((spaceSecond + 1), size);
                    byte[] messageBytesValue = value.getBytes();
                    client.submit(new UpdateCommand(keyBigInteger, messageBytesValue)).thenAccept(result -> {
                        System.out.println("Update com " + keyBigInteger + messageBytesValue + " deu: " + result);
                    });
                    // new Thread( new Update(host, port, keyBigInteger, messageBytesValue, true)
                    // ).start();
                    continue;
                }

                if (command.equals(Controll.READ)) {
                    client.submit(new ReadQuery(keyBigInteger)).thenAccept(result -> {
                        System.out.println("Read com " + keyBigInteger + " deu: " + result);
                    });
                    // new Thread( new Read(host, port, keyBigInteger, true) ).start();
                    continue;
                }

                if (command.equals(Controll.DELETE)) {
                    client.submit(new DeleteCommand(keyBigInteger)).thenAccept(result -> {
                        System.out.println("Delete com " + keyBigInteger + " deu: " + result);
                    });
                    // new Thread( new Delete(host, port, keyBigInteger, true) ).start();
                    continue;
                }

                throw new Exception();
            } catch (Exception e) {
                System.out.println(invalid);
            }
        }

        System.out.println(quit);
        scanner.close();
    }

    public static void main(String[] args) throws Exception {
        new Client(args);
    }
}
