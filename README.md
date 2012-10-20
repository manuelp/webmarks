# webmarks

This is a library written in Clojure to manage so-called *webmarks* (for now it's just a fancy name for *bookmarks*). The main characteristic of webmarks is that they are organized only by *tags*: one webmark can have how many tags you want and be searched by tag(s) or by URL substring.

*WARNING: this is still alpha software!*

## Motivation

Why write another system to manage bookmarks? Well, there are several alternatives out there, but my main requirements where:

1. Bookmarks should be *private* (no social features).
2. They should be stored *locally first*, with options for cloud storage (ease of backup and replication, no vendor lock-in, etc.).
3. They should be *organized only by tags*. Folders are hierarchical, but for the way I use bookmarks, tags seem to be a much better tool.
4. They should be stored in an accessible and easy to use form, not vendor-specific.

## Documentation
The main source of documentation right now (since the API is still subject to change) is the source code. You can generate yourself a beautifully formatted and annotated source code copy of this project using [Leiningen](http://leiningen.org/) and [Marginalia](https://github.com/fogus/marginalia) (here is an [example](http://fogus.me/fun/marginalia/) from Marginalia itself).

Clone this repo using git, install Leiningen, then go into the project's directory and run:

```
lein marg
```

That's it! The freshly baked documentation is in the *docs* directory.

## Usage

For now, the main functionality exposed to the CLI is the conversion of Firefox's bookmarks exported in JSON format to [edn](https://github.com/edn-format/edn) format:

```
java -jar webmarks-<VERSION>.jar <INPUT.json> <OUTPUT.edn>
```

But functions to manage imported or new webmarks are already present, they'll be exposed in some way (CLI first probably, web interface afterwards).

## License

Copyright © 2012 Manuel Paccagnella

Distributed under the Eclipse Public License, the same as Clojure.
