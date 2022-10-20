# Querybase

Database for queries.

## Roadmap

- Interactive query editor
  - Reuse SQL queries as functions 
    - Referencing and parameterization
- Collect, analyze, and visualize query logs
  - Define query-engine agonistic IR for analyzing logs
- Reporting
  - Summarizing query usage
  - Tracking lineage of data flows

## Developer Note

### For Treasure Data

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
$ brew install node
$ npm install jsdom

// Build UI (Scala.js -> JavaScript)
$ ./sbt
> ~ui/fastOptJS/webpack
```

Open http://localhost:8080/ui/


If you use [Browsersync](https://browsersync.io/), the UI will be reloaded automatically upon Scala.js change:
```
# Install Browsersync
$ npm -g install browser-sync

# Start a proxy server at http://localhost:3000/ui/ that watches Scala.js code change. 
$ browser-sync start --proxy http://localhost:8080/ui/ --files querybase-ui/target/scala-2.13/scalajs-bundler/main
```
