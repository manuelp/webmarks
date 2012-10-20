# webmarks

This is a library written in Clojure to manage so-called *webmarks* (for now it's just a fancy name for *bookmarks*). The main characteristic of webmarks is that they are organized only by *tags*: one webmark can have how many tags you want and be searched by tag(s) or by URL substring.

*WARNING: this is still alpha software!*

## Motivation

Why write another system to manage bookmarks? Well, there are several alternatives out there, but my main requirements where:

1. Bookmarks should be *private* (no social features)
2. They should be stored *locally first*, with options for cloud storage (ease of backup and replication, no vendor lock-in, etc.)
3. They should be *organized only by tags*. Folders are hierarchical, but for the way I use bookmarks tags, they seem a much better tool to do that.

## Usage

For now, the main functionality exposed to the CLI is the conversion of Firefox's bookmarks exported in JSON format to [edn](https://github.com/edn-format/edn) format:

```
java -jar webmarks-<VERSION>.jar <INPUT.json> <OUTPUT.edn>
```

But functions to manage imported or new webmarks are already present, they'll be exposed in some way (CLI first probably, web interface afterwards).

## License

Copyright © 2012 Manuel Paccagnella

Distributed under the Eclipse Public License, the same as Clojure.
