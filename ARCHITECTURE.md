Querybase Architecture
====

```
Browser (querybase-ui: Scala.js -> JavaScript)
  |
Frontend Server (Airframe Finagle) 
  |
Coordinator (Airframe gRPC)
  |
Worker (Airframe gRPC)
```
