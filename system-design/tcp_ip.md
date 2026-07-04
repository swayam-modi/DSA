# Explain TCP Model

## Introduction

Transmission Control Protocol/Internet Protocol (TCP/IP) is a practical network model developed by the Department of Defense (DoD) in the 1960s to support communication between different network devices on the internet.

TCP is a set of communication protocols that supports network communication.

The TCP model is subdivided into five layers, each containing specific protocols.

---

## Layers of TCP Model

### Physical Layer

- The physical layer translates message bits into signals for transmission on a medium, i.e. the physical layer is the place where the real communication takes place.
- Signals are generated depending on the type of media used to connect two devices. For example, electrical signals are generated for copper cables, light signals are generated for optical fibers, and radio waves are generated for air or vacuum.
- The physical layer also specifies characteristics like topology (bus, star, hybrid, mesh, ring), line configuration (point-to-point, multipoint), and transmission mode (simplex, half-duplex, full-duplex).

---

### Data Link Layer (DLL)

- The DLL is subdivided into 2 layers: **MAC** (Media Access Control), **LLC** (Logical Link Control)
- The MAC layer is responsible for data encapsulation (Framing) of IP packets from the network layer into frames. Framing means DLL adds a header (which contains the MAC address of source and destination) and a trailer (which contains error-checking data) at the beginning and end of IP packets.
- LLC deals with flow control and error control.
  - **Flow control:** Limits how much data a sender can transfer without overwhelming the receiver.
  - **Error control:** Errors in data transmission can be detected by checking the error detection bits in the trailer of the frame.

---

### Network Layer

The network layer adds an IP address/logical address to the data segments to form IP packets and finds the best possible path for data delivery. IP addresses are addresses allocated to a device to uniquely identify it on a global scale. Common protocols used in the Network layer are:

- **IP (Internet Protocol):** IP uses the receiver's IP address to determine the best path for the proper delivery of packets to the destination. When a packet is too large to send over a network medium, the sender host's IP splits it up into smaller fragments. The fragments are reassembled into the original packet on the receiving host. IP is unreliable since it does not ensure delivery or check for errors.
- **ARP (Address Resolution Protocol):** ARP is used to find MAC/physical addresses from the IP address.
- **ICMP (Internet Control Message Protocol):** ICMP is responsible for error reporting.

---

### Transport Layer

The transport layer is in charge of flow control (controlling the rate at which data is transferred), end-to-end connectivity, and error-free data transmission. Protocols used in the Transport layer:

**TCP (Transmission Control Protocol):**

- TCP is a connection-oriented protocol, which means it requires the formation and termination of connections between devices in order to transmit data.
- TCP segmentation means that at the sending node, TCP breaks the entire message into segments, assigns a sequence number to each segment, then reassembles the segments into the original message at the receiving end based on the sequence numbers.
- TCP is a reliable protocol because it identifies errors and retransmits the damaged frames, and ensures data delivery in the correct order.

**UDP (User Datagram Protocol):**

- UDP is a connectionless protocol, which means it does not require the establishment and termination of connections between devices.
- UDP does not support segmentation and lacks error checking and correction, which makes it less reliable but more cost-efficient.

---

### Application Layer

This is the uppermost layer, which combines the OSI model's session, presentation, and application layers. Users can interact with the application and access network resources through this layer.

Protocols used in the Application layer:

- **HTTP (Hypertext Transfer Protocol):** Protocol used to access data on the World Wide Web.
- **DNS (Domain Name System):** This protocol translates domain names to IP addresses.
- **SMTP (Simple Mail Transfer Protocol):** This protocol is used to send email messages.
- **FTP (File Transfer Protocol):** This protocol is used to transfer files between computers.
- **TELNET (Telecommunication Network):** It is a two-way communication protocol connecting a local machine to a remote machine.
