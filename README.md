# ns-sort

A Leiningen plugin to sort `:require` block in .clj, .cljs, .cljc namespaces.

[![Clojars Project](https://img.shields.io/clojars/v/ns-sort.svg)](https://clojars.org/ns-sort)

## Description

![ns-sort](docs/ns-sort.png?raw=true "ns-sort")

The plugin divide each `:require` block in two parts:
1. Project namespaces
2. Third-party namespaces

The second follow the first. 

## Usage


Put `[ns-sort "1.0.0"]` into the `:plugins` vector of your `:user`
profile.

Use this for project-level plugins:

Put `[ns-sort "1.0.0"]` into the `:plugins` vector of your project.clj.

Run:

    $ lein ns-sort

## License

Copyright © 2021 ilevd