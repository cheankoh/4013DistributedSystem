# 4013DistributedSystem - A Distributed Facility Booking Application
---
## Overview
This project is part of Continuous Assessment for the Technical Elective module
CZ4013 - Distributed System under NTU SCSE. The goal of the project is to build a 
distributed application for booking facilities by sending messages between nodes by 
UDP message. The design of the message format as well as application level fault tolerance
is the main focus of the project, not the correctioness and good UX of the application.

Author: Do Anh Tu, Yap Joon Shen, Koh Zhuang Chean

---
## Quick Start
Compile server and client before execution:
``` 
javac Distributed/Server.java
javac Distributed/Client.java
```
Launch the server first
```
java Distributed/Server
```

Launch the client then
```
java Distributed/Client
```

---
## Application Architecture
The distributed application is separated into server and client codebase. 

Client handles the input from user and packets them before sending to server as request. When server replies, the client interface is also in charge of displaying the results to users. Code base for client is found in Distributed/Client.java

Server handles the bulk of application logic for the application. Here the server implements the logic to unpack data transmitted to by client, calling the services, executing the services, producing the outputs and packets them to a message format before sending back to the client. Codebase for server is found in Distributed/Server.java

The marshalling/unmarshalling logics for converting from application level to physical level for transmission and receival is documented in Distributed/Util.java. The rest of the application, such as entity modelling and application controlling logics are found in Model and Controller packets accordingly.

---
## Distributed Infrastructure
Data bandwidth available for transmission is assumed to be unlimited for the ease of implementing receiving buffer allocation. This allows a single fixed size UDP message to be sent at any point in time instead of an auxiliary header UDP storing the size of the incoming messages. Such auxiliary message is easily prone to failure and posed as a difficulty in handling due to the complexity.

The application message is assumed to be fixed size of 1024 Bytes with 5 common fields: Communication Methods, Message Type, Message ID, Payload Size and Payload.

```
--------------------------------------------------------------------------------------------
| CommMethod (1B) | MessType (1B) | MessID (4B) | PayloadSize (4B) | Payload (Max = 1014B) |
--------------------------------------------------------------------------------------------
```

Possible values:
- CommMethod: 1 if request (by client), 2 if response (by server) and 3 if callback (by server).
- MessType: 1-6 for the 6 services in request/response/callback. 0 for error in application level at server, only in response.
- MessID: positive from 0 to 2147483647 (assumed system does not live that long for a client). Only used for request, response and callback this field is 0.
- PayloadSize: value between 0 and 1014 documents the size of Payload
- Payload: Variable size byte buffer storing the marshalled arguments for request or return message in response, callback that client needs to show to user.

Marshalling and Demarshalling of Int and String is handled in this project Distribute/Util.java. This is done only by bit shifting.

---
## Service Implementation 
Details on service implementation is shown in report. The payload for the services request/responses are shown below:

### Query Availability
Request message's payload:
```
---------------------------------------------------------------------------------
| Facitily Type (Int) | Facility selection (Int) | Day/Range of Day (String) |
---------------------------------------------------------------------------------
```

Response message's payload:

```
---------------------------
| Message String (String) |
---------------------------
```

### Book Facility
Request message's payload:
```
----------------------------------------------------------------------------------------------------------------------------------------
| Facitily Type (Int) | Facility selection (Int) | Day of Booking (1-6) (Int) | Start Time (Int) | Stop Time (Int) | User ID (Int) |
----------------------------------------------------------------------------------------------------------------------------------------
```

Response message's payload:
```
---------------------------
| Message String (String) |
---------------------------
```

### Shift Booking Forward/Backward
Request message's payload:
```
-------------------------------------------------------------
| Booking ID (Int) | # Slots to Advance/Delay by (+/-)(Int) |
-------------------------------------------------------------
```

Response message's payload:
```
---------------------------
| Message String (String) |
---------------------------
```

### Monitor Availability
Request message's payload:
```
-----------------------------------------------------------------------------------
| Facitily Type (Int) | Facility selection (Int) | Monitor Duration (secs)(Int)|
-----------------------------------------------------------------------------------
```

Response/Callback message's payload:
```
---------------------------
| Message String (String) |
---------------------------
```

### Cancel Booking
Request message's payload:
```
--------------------
| Booking ID (Int) |
--------------------
```

Response message's payload:
```
---------------------------
| Message String (String) |
---------------------------
```

### Extend Booking
Request message's payload:
```
-------------------------------------------------------------
| Booking ID (Int) | # Slots to Extend/Shorten (+/-)(Int) |
-------------------------------------------------------------
```
Response message's payload:
```
---------------------------
| Message String (String) |
---------------------------
```
---
## Fault Tolerance
UDP has no fault tolerance mechanisms like in TCP. The application implements 2 invocation semantics for fault tolerance.

### At Least Once
- Client retransmits request should the original requests failed to deliver to server or the reply to the original request failed to be received by client.
- Server reexecutes the request if duplicated. No filtering of duplication request from a client implemeted. Reexecution of requested service is done by server and then the response of this reexecution is sent to client.
- Not correct for non-idempotent services (change booking (shift/delay), monitor availability, extend/shorten booking)

### At Most Once
- Client retransmits request should the original requests failed to deliver to server or the reply to the original request failed to be received by client.
- Server implements a history table to store response of previous request based on their message ID and the IP + Port of client they originate from. If retransmission of request by client is conducted, server can filter the duplicated request ID by message ID from client IP + Port. The corresponding response from server history is selected and retransmitted to client instead of reexecution
- Correct for non-idempotent services (change booking (shift/delay), monitor availability, extend/shorten booking)
