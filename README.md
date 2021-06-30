# Querybase

Database for queries.

- Collect, analyze, and visualize query logs
  - Define query-engine agonistic IR for analyzing logs
- Reporting
  - Summarizing query usage
  - Tracking lineage of data
- Create personalized benchmarks
  - Find the performance bottleneck of sequence of queries
- Workload optimization
  - Find common sub-expression queries where materialized views or pre-processing
   can be used




## Developer Note


Add service settings:

__.querybase/services.json__
```json
{
  "services": [
    {
      "serviceType": "trino",
      "name": "td (US)",
      "description": "td-presto US region",
      "properties": {
        "address": "api-presto.treasuredata.com:443",
        "connector": "td-presto",
        "user": "(TD API KEY)"
      }
    },
    {
      "serviceType": "trino",
      "name": "td (JP)",
      "description": "td-presto Tokyo region",
      "properties": {
        "address": "api-presto.treasuredata.co.jp:443",
        "connector": "td-presto",
        "user": "(TD API KEY)"
      }
    }
  ]
}

```


Run querybase server: 
```
$ ./sbt
> ~server/reStart standalone 
```

Build Scala.js UI:
```
// Set up Node.js and jsdom
$ brew install nodde
$ npm install jsdom

// Build UI (Scala.js -> JavaScript)
$ ./sbt
> ~ui/fastOptJS/webpack
```

Open http://localhost:8080/ui/
