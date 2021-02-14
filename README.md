# ns-sort

A Leiningen plugin to sort :require block in .clj, .cljs, .cljc namespaces.

The plugin divide each :require block in two parts:
1. Project namespaces
2. Third-party namespaces

The second follows the first. 

## Usage


Put `[ns-sort "1.0.0"]` into the `:plugins` vector of your `:user`
profile.

Use this for project-level plugins:

Put `[ns-sort "1.0.0"]` into the `:plugins` vector of your project.clj.

Run:

    $ lein ns-sort

## License

Copyright © 2021 ilevd