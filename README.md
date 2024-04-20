<!-- @format -->

# Distributed System Security - TCP Server and Client

This repository contains the implementation of a secure TCP Server and Client in Java, demonstrating key concepts in distributed system security.

## Description

The server and client are designed to handle secure communication over a network using TCP protocol. They demonstrate the use of public key cryptography for secure message exchange.

The server handles client connections and processes incoming messages. It supports basic commands such as "Hello", "Key", and "message". When a client sends a "Hello" message, the server responds with a "Hello" message. When a client sends a "Key" message, the server sends the public key size and the public key in bytes. When a client sends a "message", the server stops processing further commands.

The server also handles encrypted messages. It reads the size of the encrypted message from the client, reads the encrypted message, and then decrypts it using its private key.

The client, on the other hand, is designed to connect to the server and send messages. It can send "Hello", "Key", and "message" commands to the server. It also handles encryption of messages using the server's public key, ensuring that the messages are securely transmitted over the network.

## Usage

To use this server and client, compile and run the `TCPServer.java` and `TCPClient.java` files respectively. The server will start listening for client connections and the client will start sending messages to the server.

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License

[MIT](https://choosealicense.com/licenses/mit/)
