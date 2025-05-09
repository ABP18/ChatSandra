package Practica1;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class server {
    private static final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private static String serverKeyword;
    private static int maxClients;
    private static boolean hadClients = false;

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Uso: java server <puerto> <palabraClave> <maxClientes>");
            return;
        }

        int puerto = Integer.parseInt(args[0]);
        serverKeyword = args[1].toLowerCase();
        maxClients = Integer.parseInt(args[2]);

        try (ServerSocket serverSocket = new ServerSocket(puerto)) {
            System.out.println("Iniciando servidor en puerto " + puerto + "... OK");
            boolean running = true;
            while (running) {
                if (clients.size() < maxClients) {
                    Socket clientSocket = serverSocket.accept();
                    hadClients = true;
                    System.out.println("Connection from client " + (clients.size() + 1) + " ... OK");

                    ClientHandler clientHandler = new ClientHandler(clientSocket, clients.size() + 1);
                    clients.add(clientHandler);
                    new Thread(clientHandler).start();
                }
                synchronized (clients) {
                    if (hadClients && clients.isEmpty()) {
                        System.out.println("No clients connected. Closing server...");
                        running = false;
                    }
                }
                Thread.sleep(100);
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error en el servidor: " + e.getMessage());
        }
        System.out.println("Servidor cerrado.");
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String clientKeyword;
        private int clientId;
        private boolean running;

        public ClientHandler(Socket socket, int clientId) {
            this.socket = socket;
            this.clientId = clientId;
            this.running = true;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.println("Por favor, envía tu palabra clave:");
                clientKeyword = in.readLine();
                if (clientKeyword != null) {
                    clientKeyword = clientKeyword.toLowerCase();
                    System.out.println("Client " + clientId + " keyword: " + clientKeyword);
                } else {
                    running = false;
                }

                System.out.println("Inicializing chat with client " + clientId + "... OK");

                BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
                while (running) {
                    String message = in.readLine();
                    if (message == null) {
                        System.out.println("Client " + clientId + " disconnected.");
                        running = false;
                    } else {
                        System.out.println("Rebut del client " + clientId + ": " + message);

                        if (message.toLowerCase().contains(clientKeyword)) {
                            System.out.println("Client " + clientId + " keyword detected!");
                            out.println("Client keyword detected. Closing...");
                            running = false;
                        } else if (message.toLowerCase().contains(serverKeyword)) {
                            System.out.println("Client " + clientId + " used server keyword. No action.");
                        }

                        if (running) {
                            System.out.print("Servidor (para cliente " + clientId + "): ");
                            String serverInput = console.readLine();
                            if (serverInput.toLowerCase().contains(serverKeyword)) {
                                System.out.println("Server keyword detected! Closing all chats...");
                                out.println("Server closing...");
                                closeAllClients();
                                running = false;
                            } else if (serverInput.toLowerCase().contains(clientKeyword)) {
                                System.out.println("Server used client " + clientId + " keyword. Closing client chat...");
                                out.println("Client keyword detected by server. Closing...");
                                running = false;
                            } else {
                                out.println(serverInput);
                                System.out.println("Enviar al client " + clientId + ": " + serverInput);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error con cliente " + clientId + ": " + e.getMessage());
            } finally {
                close();
            }
        }

        private void close() {
            running = false;
            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (socket != null) socket.close();
                System.out.println("Closing chat with client " + clientId + "... OK");
            } catch (IOException e) {
                System.out.println("Error closing client " + clientId + ": " + e.getMessage());
            }
            synchronized (clients) {
                clients.remove(this);
            }
        }

        private void closeAllClients() {
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    client.running = false;
                    try {
                        client.out.println("Server closing...");
                        client.socket.close();
                    } catch (IOException e) {
                        System.out.println("Error closing client " + client.clientId + ": " + e.getMessage());
                    }
                }
                clients.clear();
            }
        }
    }
}