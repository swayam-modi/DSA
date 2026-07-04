# What Happens When You Enter "google.com"?

## Abstract

This article covers how we search for something on Google, the working including a technical explanation.

## Pre-Requisites

### 1. What is a Webpage?

Before getting started, it's important to explain what a webpage is. A webpage is basically a text file formatted a certain way so that your browser (i.e. Chrome, Firefox, Safari, etc.) can understand it; this format is called HyperText Markup Language (HTML). These files are located in computers that provide the service of storing said files and waiting for someone to need them to deliver them. They are called servers because they serve the content that they hold to whoever needs it.

### 2. Servers

Servers can vary in classes. The most common — and the one we'll be talking about in the main portion of this article — is a **web server**, the one that serves web pages. We can also find **application servers**, which hold an application's base code that will then be used to interact with a web browser or other applications. **Database servers** are also out there, which hold a database that can be updated and consulted when needed.

### 3. IP Addresses

These servers, in order to deliver their content, much like in physical courier services, need to have an address so that the person needing said content can make a "letter" requesting the delivery; the person requesting the content in turn also has an address where the server can deliver the content to. These addresses are called **IP (Internet Protocol) Addresses**, a set of 4 numbers that range from 0 to 255 (one byte) separated by periods (e.g. 127.0.0.1).

### 4. Protocols for Delivery

Another important concept is that the courier service traffic for the delivery can be one of two: **Transmission Control Protocol (TCP)** and **User Datagram Protocol (UDP)**. Each one determines the way the content of a server is served or delivered.

---

## TCP

TCP is usually used to deliver static websites such as Wikipedia or Google, and also email services and file downloads, because TCP makes sure that all the content that is needed gets delivered. It accomplishes this by sending the file in small packets of data along with a confirmation for each packet to know that it was delivered — that's why if you are ever downloading something and your internet connection suddenly drops, when it comes back up you don't have to start over, because the server would know exactly how many packets you have and how many you still need to receive. The downside to TCP is that because it has to confirm whether you got the packet or not before sending the next one, it tends to be slower.

---

## UDP

UDP, on the other hand, is usually used to serve live videos or online games. This is because UDP is a lot faster than TCP since UDP does not check if the information was received or not — it doesn't care. The only thing UDP cares about is sending the information. That is the reason why if you've ever watched a live video and either your internet connection or the host's drops, you would just stop seeing the content; and when the connection comes back up you will only see the current stream of the broadcast, and what was missed is forever lost. This is also true for online video games (if you've played them, you know exactly what this means).

---

## What Actually Happens

So back to the main question: what happens when you type www.google.com or any other URL (Uniform Resource Locator) in your web browser and press Enter?

1. The first thing that happens is that your browser looks up in its cache to see if that website was visited before and the IP address is known.
2. If it can't find the IP address for the URL requested, then it asks your operating system to locate the website. The first place your operating system checks for the address of the URL is the host file. If the URL is not found inside this file, then the OS will make a DNS request to find the IP address of the web page.
3. The first step is to ask the Resolver (or Internet Service Provider) server to look up its cache to see if it knows the IP address. If the Resolver doesn't know, it asks the root server to ask the .COM TLD (Top Level Domain) server — if your URL ends in .net, then the TLD server would be .NET, and so on — and the TLD server will again check its cache to see if the requested IP address is there.
4. If not, then it will have at least one of the authoritative name servers associated with that URL, and after going to the Name Server, it will return the IP address associated with your URL. All this is done in a matter of milliseconds!
5. After the OS has the IP address and gives it to the browser, the browser then makes a GET request (a type of HTTP Method) to that IP address. When the request is made, the browser again makes the request to the OS, which then packs the request in the TCP traffic protocol discussed earlier, and it is sent to the IP address.
6. On its way, it is checked by both the OS's and the server's firewall to make sure there are no security violations. Upon receiving the request, the server (usually a load balancer that directs traffic to all available servers for that website) sends a response with the IP address of the chosen server, along with the SSL (Secure Sockets Layer) certificate to initiate a secure session (HTTPS).
7. Finally, the chosen server sends the HTML, CSS, and JavaScript files (if any) back to the OS, which in turn gives them to the browser to interpret. And then you get your website as you know it.

---

## Summary

In our modern society, where everything is online, it is amazing to know the complexity that takes place in order for us to get to a website. Yet, it is done so fast that very few would even begin to fathom the amazing process that takes place.
