package Practica1;

import java.net.*;
import java.io.*;

public class client {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Uso: java client <servidor> <puerto> <palabraClave>");
            return;
        }

        String servidor = args[0];
        int puerto = Integer.parseInt(args[1]);
        String palabraClave = args[2].toLowerCase();

        try (Socket s = new Socket(servidor, puerto);
             PrintWriter salida = new PrintWriter(s.getOutputStream(), true);
             BufferedReader entrada = new BufferedReader(new InputStreamReader(s.getInputStream()));
             BufferedReader consola = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Conectado al servidor en puerto " + puerto + "... OK");

            String serverRequest = entrada.readLine();
            System.out.println("Servidor: " + serverRequest);
            salida.println(palabraClave);

            System.out.println("Inicializing chat... OK");

            boolean continuar = true;
            while (continuar) {
                System.out.print("Cliente: ");
                String mensaje = consola.readLine();
                salida.println(mensaje);

                if (mensaje.toLowerCase().contains(palabraClave)) {
                    System.out.println("Client keyword detected! Closing chat...");
                    String respuesta = entrada.readLine();
                    if (respuesta != null) {
                        if (respuesta.toLowerCase().contains("client keyword detected")) {
                            System.out.println("Confirmación de cierre recibida del servidor.");
                        }
                    }
                    continuar = false;
                } else {
                    String respuesta = entrada.readLine();
                    if (respuesta == null) {
                        System.out.println("El servidor ha cerrado la conexión inesperadamente.");
                        continuar = false;
                    } else if (respuesta.toLowerCase().contains("server closing")) {
                        System.out.println("El servidor ha finalizado la conversacion.");
                        continuar = false;
                    } else if (respuesta.toLowerCase().contains("client keyword detected by server")) {
                        System.out.println("Servidor ha usado tu palabra clave. Cerrando chat...");
                        continuar = false;
                    } else {
                        System.out.println("Servidor: " + respuesta);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error en el cliente: " + e.getMessage());
        }
        System.out.println("Cliente cerrado.");
    }
}