# ns-sort

A Leiningen plugin to sort namespaces in `:require` block.

[![Clojars Project](https://img.shields.io/clojars/v/ns-sort.svg)](https://clojars.org/ns-sort)

## Description

![ns-sort](docs/ns-sort.png?raw=true "ns-sort")

The plugin sort namespaces in `:require` block with lexicographic order.

The project namespaces have more priority compared with other 3rd party dependencies.

The plugin supports `.clj`, `.cljs`, `.cljc` files.

## Usage

Put `[ns-sort "1.0.0"]` into the `:plugins` vector of your `:user` profile.

Use this for project-level plugins:

Put `[ns-sort "1.0.0"]` into the `:plugins` vector of your project.clj.

Run:

    $ lein ns-sort

## License

Copyright © 2021 ilevd