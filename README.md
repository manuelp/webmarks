# webmarks

This is a library (and an app using it) written in Clojure to manage so-called *webmarks* (for now it's just a fancy name for *bookmarks*). The main characteristic of webmarks is that they are organized only by *tags*: one webmark can have how many tags you want and be searched by tag(s) or by URL substring.

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

The main UI is web-based (written using Ring, Compojure and Enlive if you are curious), you can start the embedded Jetty application server with a simple command:

```
java -jar webmarks-<VERSION>.jar [PORT] [WEBMARKS-FILE]
```

Port and webmarks file are optional, defaults:

- Port: *8080*
- Webmarks file: *webmarks.edn*

Currently it's required to login with this default credentials:

- Username: manuel
- Password: password

All settings can be configured also via environment variables:

- `PASSWORD`: password for the "manuel" user.
- `WEBMARKS_FILE`: filename for the webmarks file that contains (or will contains) all the webmarks.
- `PORT`: TCP port to bind to.

And there is also a `Procfile`, so that you can easily deploy your own instance of this webapp on Heroku (and configure the instance with your own settings).

### Convert Firefox's bookmarks ###

It is also possible to convert Firefox's bookmarks exported into JSON format to a webmarks file (it's in plain-text [edn](https://github.com/edn-format/edn) format) suitable to be used by the rest of the application. For now it's necessary to use the project sources and Leiningen:

```
lein run -m webmarks.firefox <INPUT.json> <OUTPUT.edn>
```

## License

Copyright © 2012 Manuel Paccagnella

Distributed under the Eclipse Public License, the same as Clojure.
